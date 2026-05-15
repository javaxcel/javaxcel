/*
 * Copyright 2026 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.core.in.core;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jspecify.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;

import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.exception.NoTargetedFieldException;
import com.github.javaxcel.core.in.context.ExcelReadContext;
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy;
import com.github.javaxcel.core.in.strategy.impl.KeyNames;
import com.github.javaxcel.core.in.strategy.impl.Limit;
import com.github.javaxcel.core.in.strategy.impl.Parallel;
import com.github.javaxcel.core.internal.assembler.ModelAssembler;
import com.github.javaxcel.core.internal.descriptor.ColumnDescriptor;
import com.github.javaxcel.core.internal.descriptor.ModelDescriptorFactory;
import com.github.javaxcel.core.internal.descriptor.StrategyDedup;
import com.github.javaxcel.core.internal.util.ExcelUtils;
import com.github.javaxcel.core.internal.util.FieldUtils;

import static java.util.stream.Collectors.*;

/**
 * Single Excel reader engine for both {@code Model} and {@code Map} modes.
 *
 * <p>Replaces the {@code AbstractExcelReader} + {@code ModelReader} + {@code MapReader}
 * hierarchy from previous versions. Mode is selected by the static factory:
 * {@link #forModel(Workbook, Class, ExcelTypeHandlerRegistry)} or
 * {@link #forMap(Workbook)}.
 *
 * @param <T> type of model
 * @since 0.x
 */
public class DefaultExcelReader<T> implements ExcelReader<T> {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private final Workbook workbook;

    @Getter(AccessLevel.PACKAGE)
    private final ExcelReadContext<T> context;

    private final @Nullable FormulaEvaluator formulaEvaluator;

    private final boolean modelMode;

    private final @Nullable ExcelTypeHandlerRegistry registry;

    @SuppressWarnings("unchecked")
    private DefaultExcelReader(
            Workbook workbook,
            Class<T> modelType,
            @Nullable ExcelTypeHandlerRegistry registry,
            boolean modelMode) {
        Asserts.that(workbook)
                .describedAs("DefaultExcelReader.workbook is not allowed to be null")
                .isNotNull();
        Asserts.that(modelType)
                .describedAs("DefaultExcelReader.modelType is not allowed to be null")
                .isNotNull();

        this.workbook = workbook;
        this.context = new ExcelReadContext<>(workbook, modelType, (Class<? extends ExcelReader<T>>) getClass());
        this.formulaEvaluator = resolveFormulaEvaluator(workbook);
        this.modelMode = modelMode;
        this.registry = registry;
    }

    /**
     * Creates a reader bound to the given model class. Field discovery, analysis,
     * and converter wiring are deferred to the first call to {@link #read()} so
     * that {@code .options(...)} strategies can influence the analysis.
     */
    public static <T> DefaultExcelReader<T> forModel(
            Workbook workbook, Class<T> type, ExcelTypeHandlerRegistry registry) {
        Asserts.that(type)
                .describedAs("DefaultExcelReader.type is not allowed to be null")
                .isNotNull();
        Asserts.that(registry)
                .describedAs("DefaultExcelReader.registry is not allowed to be null")
                .isNotNull();
        // Eagerly validate that the model class has readable fields, matching the
        // construction-time validation of the legacy ModelReader.
        List<Field> fields = FieldUtils.getTargetedFields(type);
        Asserts.that(fields)
                .describedAs("DefaultExcelReader cannot find the targeted fields in the class: {0}", type.getName())
                .thrownBy(desc -> new NoTargetedFieldException(type, desc))
                .isNotEmpty();
        return new DefaultExcelReader<>(workbook, type, registry, true);
    }

    /**
     * Creates a reader that materializes each row as a {@code Map<String, String>}
     * keyed by header name. {@code KeyNames} strategy can override the keys.
     */
    @SuppressWarnings("unchecked")
    public static DefaultExcelReader<Map<String, String>> forMap(Workbook workbook) {
        Class<Map<String, String>> mapType = (Class<Map<String, String>>) (Class<?>) Map.class;
        return new DefaultExcelReader<>(workbook, mapType, null, false);
    }

    @Nullable
    private static FormulaEvaluator resolveFormulaEvaluator(Workbook workbook) {
        try {
            CreationHelper creationHelper = workbook.getCreationHelper();
            return creationHelper.createFormulaEvaluator();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public ExcelReader<T> options(ExcelReadStrategy... strategies) {
        Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> map = StrategyDedup.collect(strategies, this.context);
        this.context.setStrategyMap(map);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final List<T> read() {
        List<T> all = new ArrayList<>();
        this.context.setList(all);

        int limit = resolveLimit();
        List<String> keyNamesOverride = resolveKeyNamesOverride();

        List<ColumnDescriptor<T>> columns = null;
        ModelAssembler<T> assembler = null;
        if (this.modelMode) {
            columns = ModelDescriptorFactory.forRead(
                    this.context.getModelType(), this.registry, this.context.getStrategyMap());
            assembler = new ModelAssembler<>(this.context.getModelType(), columns);
        }

        boolean parallel = this.context.getStrategyMap().containsKey(Parallel.class);

        for (Sheet sheet : ExcelUtils.getSheets(this.workbook)) {
            if (limit >= 0 && this.context.getReadCount() >= limit) {
                break;
            }
            this.context.setSheet(sheet);

            List<String> headerNames;
            if (this.modelMode) {
                headerNames = columns.stream().map(ColumnDescriptor::name).collect(toList());
            } else if (keyNamesOverride != null) {
                headerNames = keyNamesOverride;
            } else {
                headerNames = readFirstRowAsHeaders(sheet);
            }
            if (CollectionUtils.exists(headerNames)) {
                this.context.setHeaderNames(headerNames);
            }

            List<Map<String, String>> rowMaps = readBodyAsMaps(sheet, headerNames, limit);

            List<T> chunk;
            if (this.modelMode) {
                final ModelAssembler<T> finalAssembler = assembler;
                final List<ColumnDescriptor<T>> finalColumns = columns;
                if (parallel) {
                    chunk = rowMaps.parallelStream()
                            .map(row -> finalAssembler.assemble(row, finalColumns))
                            .collect(toList());
                } else {
                    chunk = new ArrayList<>(rowMaps.size());
                    for (Map<String, String> row : rowMaps) {
                        chunk.add(finalAssembler.assemble(row, finalColumns));
                    }
                }
            } else {
                chunk = (List<T>) (List<?>) rowMaps;
            }

            this.context.setChunk(chunk);
            all.addAll(chunk);
        }

        return all;
    }

    private int resolveLimit() {
        ExcelReadStrategy strategy = this.context.getStrategyMap().get(Limit.class);
        return strategy == null ? -1 : (int) strategy.execute(this.context);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private List<String> resolveKeyNamesOverride() {
        ExcelReadStrategy strategy = this.context.getStrategyMap().get(KeyNames.class);
        return strategy == null ? null : (List<String>) strategy.execute(this.context);
    }

    private List<String> readFirstRowAsHeaders(Sheet sheet) {
        List<String> headerNames = new ArrayList<>();
        for (Row header : sheet) {
            int columnCount = header.getLastCellNum();
            for (int i = 0; i < columnCount; i++) {
                Cell cell = header.getCell(i);
                String cellValue = cell == null ? null : cell.getStringCellValue();
                headerNames.add(StringUtils.ifNullOrEmpty(cellValue, String.valueOf(i)));
            }
            break;
        }
        return headerNames;
    }

    private List<Map<String, String>> readBodyAsMaps(Sheet sheet, List<String> headerNames, int limit) {
        List<Map<String, String>> maps = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }
            if (limit >= 0 && this.context.getReadCount() >= limit) {
                break;
            }
            maps.add(readRow(row, headerNames));
        }
        return Collections.unmodifiableList(maps);
    }

    private Map<String, String> readRow(Row row, List<String> headerNames) {
        Map<String, String> map = new HashMap<>();
        int columnCount = CollectionUtils.exists(headerNames) ? headerNames.size() : row.getLastCellNum();
        for (int i = 0; i < columnCount; i++) {
            Cell cell = row.getCell(i);
            String headerName = headerNames.get(i);
            String cellValue = readCell(cell);
            map.put(headerName, cellValue);
        }
        this.context.increaseReadCount();
        return Collections.unmodifiableMap(map);
    }

    @Nullable
    private String readCell(@Nullable Cell cell) {
        if (cell == null) {
            return null;
        }
        String cellValue;
        switch (cell.getCellType()) {
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case NUMERIC:
                cellValue = BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                cellValue = this.formulaEvaluator != null
                        ? DATA_FORMATTER.formatCellValue(cell, this.formulaEvaluator)
                        : null;
                break;
            default:
                cellValue = null;
        }
        return StringUtils.ifNullOrEmpty(cellValue, (String) null);
    }

}
