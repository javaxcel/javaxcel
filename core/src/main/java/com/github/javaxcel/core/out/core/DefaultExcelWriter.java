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

package com.github.javaxcel.core.out.core;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jspecify.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ArrayUtils;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.NumberUtils;
import io.github.imsejin.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;

import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.exception.NoTargetedFieldException;
import com.github.javaxcel.core.exception.WritingExcelException;
import com.github.javaxcel.core.internal.descriptor.ColumnDescriptor;
import com.github.javaxcel.core.internal.descriptor.MapDescriptorFactory;
import com.github.javaxcel.core.internal.descriptor.ModelDescriptorFactory;
import com.github.javaxcel.core.internal.descriptor.StrategyDedup;
import com.github.javaxcel.core.internal.util.ExcelUtils;
import com.github.javaxcel.core.internal.util.FieldUtils;
import com.github.javaxcel.core.out.context.ExcelWriteContext;
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;
import com.github.javaxcel.core.out.strategy.impl.AutoResizedColumns;
import com.github.javaxcel.core.out.strategy.impl.BodyStyles;
import com.github.javaxcel.core.out.strategy.impl.CloseResource;
import com.github.javaxcel.core.out.strategy.impl.Filter;
import com.github.javaxcel.core.out.strategy.impl.HeaderNames;
import com.github.javaxcel.core.out.strategy.impl.HeaderStyles;
import com.github.javaxcel.core.out.strategy.impl.HiddenExtraColumns;
import com.github.javaxcel.core.out.strategy.impl.HiddenExtraRows;
import com.github.javaxcel.core.out.strategy.impl.SheetName;
import com.github.javaxcel.styler.ExcelStyleConfig;

/**
 * Single Excel writer engine for both {@code Model} and {@code Map} modes.
 *
 * <p>Replaces the {@code AbstractExcelWriter} + {@code ModelWriter} + {@code MapWriter}
 * hierarchy from previous versions. Mode is selected by the static factory:
 * {@link #forModel(Workbook, Class, ExcelTypeHandlerRegistry)} or
 * {@link #forMap(Workbook)}.
 *
 * @param <T> type of model
 * @since 0.x
 */
public class DefaultExcelWriter<T> implements ExcelWriter<T> {

    private final Workbook workbook;

    @Getter(AccessLevel.PACKAGE)
    private final ExcelWriteContext<T> context;

    private final boolean modelMode;

    @Nullable
    private final ExcelTypeHandlerRegistry registry;

    private int @Nullable [] columnWidths;

    @SuppressWarnings("unchecked")
    private DefaultExcelWriter(
            Workbook workbook,
            Class<T> modelType,
            @Nullable ExcelTypeHandlerRegistry registry,
            boolean modelMode
    ) {
        Asserts.that(workbook)
                .describedAs("DefaultExcelWriter.workbook is not allowed to be null")
                .isNotNull();
        Asserts.that(modelType)
                .describedAs("DefaultExcelWriter.modelType is not allowed to be null")
                .isNotNull();

        this.workbook = workbook;
        this.context = new ExcelWriteContext<>(workbook, modelType, (Class<? extends ExcelWriter<T>>) getClass());
        this.modelMode = modelMode;
        this.registry = registry;
    }

    public static <T> DefaultExcelWriter<T> forModel(
            Workbook workbook, Class<T> type, ExcelTypeHandlerRegistry registry) {
        Asserts.that(type)
                .describedAs("DefaultExcelWriter.type is not allowed to be null")
                .isNotNull();
        Asserts.that(registry)
                .describedAs("DefaultExcelWriter.registry is not allowed to be null")
                .isNotNull();
        // Eagerly validate that the model class has writable fields, matching the
        // construction-time validation of the legacy ModelWriter.
        List<Field> fields = FieldUtils.getTargetedFields(type);
        Asserts.that(fields)
                .describedAs("DefaultExcelWriter cannot find the targeted fields in the class: {0}", type.getName())
                .thrownBy(desc -> new NoTargetedFieldException(type, desc))
                .isNotEmpty();
        return new DefaultExcelWriter<>(workbook, type, registry, true);
    }

    @SuppressWarnings("unchecked")
    public static DefaultExcelWriter<Map<String, Object>> forMap(Workbook workbook) {
        Class<Map<String, Object>> mapType = (Class<Map<String, Object>>) (Class<?>) Map.class;
        return new DefaultExcelWriter<>(workbook, mapType, null, false);
    }

    @Override
    public final ExcelWriter<T> options(ExcelWriteStrategy... strategies) {
        Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> map = StrategyDedup.collect(strategies,
                this.context);
        this.context.setStrategyMap(map);
        return this;
    }

    @Override
    public final void write(OutputStream out, List<T> list) {
        Asserts.that(list)
                .describedAs("DefaultExcelWriter.list is not allowed to be null")
                .isNotNull();

        this.context.setList(list);

        // Build columns based on the mode.
        List<ColumnDescriptor<T>> columns = buildColumns(list);
        Asserts.that(columns)
                .describedAs("DefaultExcelWriter.columns cannot be empty")
                .isNotEmpty();

        // Resolve styles and other strategies.
        CellStyle[] headerStyles = resolveHeaderStyles(columns);
        CellStyle[] bodyStyles = resolveBodyStyles(columns);
        this.context.setHeaderStyles(headerStyles);
        this.context.setBodyStyles(bodyStyles);

        setupAutoResizeColumns(columns.size());

        // Partition the list by max-rows-per-sheet.
        final int maxRows = ExcelUtils.getMaxRows(this.workbook) - 1;
        List<List<T>> chunkedList = CollectionUtils.partitionBySize(list, maxRows);
        final int sheetCount = NumberUtils.toPositive(chunkedList.size());

        List<String> sheetNames = createSheetNames(sheetCount);

        for (int i = 0; i < sheetCount; i++) {
            String sheetName = sheetNames.get(i);
            Sheet sheet = this.workbook.createSheet(sheetName);
            List<T> chunk = chunkedList.isEmpty() ? Collections.emptyList() : chunkedList.get(i);
            this.context.setChunk(chunk);
            this.context.setSheet(sheet);

            applyFilter(sheet, columns.size(), chunk.size());
            createHeader(sheet, columns, headerStyles);
            createBody(sheet, columns, chunk, bodyStyles);
            applyEnumDropdowns(sheet, columns);

            applyAutoResizedColumns(sheet, columns.size());
            applyHiddenExtraRows(sheet, chunk.size());
            applyHiddenExtraColumns(sheet, columns.size());
        }

        save(out);
        closeResource(out);
    }

    private List<ColumnDescriptor<T>> buildColumns(List<T> list) {
        if (this.modelMode) {
            return ModelDescriptorFactory.forWrite(
                    this.context.getModelType(), this.registry, this.context.getStrategyMap());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        List<ColumnDescriptor<T>> columns = (List<ColumnDescriptor<T>>) (List) MapDescriptorFactory.forWrite(
                (List<? extends Map<String, ?>>) list, this.context.getStrategyMap());

        return columns;
    }

    @SuppressWarnings("unchecked")
    private CellStyle[] resolveHeaderStyles(List<ColumnDescriptor<T>> columns) {
        ExcelWriteStrategy strategy = this.context.getStrategyMap().get(HeaderStyles.class);
        if (strategy != null) {
            List<ExcelStyleConfig> styleConfigs = (List<ExcelStyleConfig>) strategy.execute(this.context);
            Asserts.that(styleConfigs)
                    .describedAs(
                            "headerStyles.size must be 1 or equal to columns.size (headerStyles.size: {0}, columns.size: {1})",
                            styleConfigs.size(), columns.size())
                    .is(it -> it.size() == 1 || it.size() == columns.size());
            return ExcelUtils.toCellStyles(this.workbook, styleConfigs.toArray(new ExcelStyleConfig[0]));
        }

        return resolveDescriptorStyles(columns, ColumnDescriptor::headerStyle);
    }

    @SuppressWarnings("unchecked")
    private CellStyle[] resolveBodyStyles(List<ColumnDescriptor<T>> columns) {
        ExcelWriteStrategy strategy = this.context.getStrategyMap().get(BodyStyles.class);
        if (strategy != null) {
            List<ExcelStyleConfig> styleConfigs = (List<ExcelStyleConfig>) strategy.execute(this.context);
            Asserts.that(styleConfigs)
                    .describedAs(
                            "bodyStyles.size must be 1 or equal to columns.size (bodyStyles.size: {0}, columns.size: {1})",
                            styleConfigs.size(), columns.size())
                    .is(it -> it.size() == 1 || it.size() == columns.size());
            return ExcelUtils.toCellStyles(this.workbook, styleConfigs.toArray(new ExcelStyleConfig[0]));
        }

        return resolveDescriptorStyles(columns, ColumnDescriptor::bodyStyle);
    }

    private CellStyle[] resolveDescriptorStyles(
            List<ColumnDescriptor<T>> columns,
            Function<ColumnDescriptor<T>, Optional<ExcelStyleConfig>> styleFn) {
        // Identity-based cache: descriptors that share the SAME ExcelStyleConfig instance
        // (e.g. all columns falling back to a single @ExcelModel-derived config) collapse
        // into one workbook CellStyle. Distinct instances (e.g. @ExcelColumn per-column
        // overrides) produce distinct CellStyles.
        IdentityHashMap<ExcelStyleConfig, CellStyle> cache = new IdentityHashMap<>();
        CellStyle[] cellStyles = new CellStyle[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            ExcelStyleConfig config = styleFn.apply(columns.get(i)).orElse(null);
            if (config == null) {
                continue;
            }
            CellStyle cellStyle = cache.get(config);
            if (cellStyle == null && !cache.containsKey(config)) {
                cellStyle = ExcelUtils.toCellStyle(this.workbook, config);
                cache.put(config, cellStyle);
            }
            cellStyles[i] = cellStyle;
        }
        return cellStyles;
    }

    private void setupAutoResizeColumns(int columnCount) {
        ExcelWriteStrategy strategy = this.context.getStrategyMap().get(AutoResizedColumns.class);
        if (strategy == null) {
            return;
        }
        boolean manual = (boolean) strategy.execute(this.context);
        if (!manual) {
            return;
        }
        this.columnWidths = new int[columnCount];
    }

    private List<String> createSheetNames(int sheetCount) {
        ExcelWriteStrategy strategy = this.context.getStrategyMap().get(SheetName.class);
        String sheetName = strategy == null ? "Sheet" : (String) strategy.execute(this.context);

        List<String> sheetNames;
        if (sheetCount < 2) {
            sheetNames = Collections.singletonList(sheetName);
        } else {
            sheetNames = new ArrayList<>(sheetCount);
            for (int i = 1; i <= sheetCount; i++) {
                sheetNames.add(sheetName + i);
            }
        }
        Asserts.that(sheetNames)
                .describedAs("sheetNames is not allowed to be null or empty: {0}", sheetNames)
                .isNotNull()
                .isNotEmpty()
                .describedAs("sheetNames cannot have null or blank element: {0}", sheetNames)
                .noneMatch(StringUtils::isNullOrBlank)
                .describedAs("sheetNames cannot have duplicated elements: {0}", sheetNames)
                .doesNotHaveDuplicates()
                .asSize()
                .describedAs("sheetNames.size is not equal to sheetCount: (sheetName.size: {0}, sheetCount: {1})",
                        sheetNames.size(), sheetCount)
                .isEqualTo(sheetCount);
        return Collections.unmodifiableList(sheetNames);
    }

    private void applyFilter(Sheet sheet, int columnCount, int rowCount) {
        ExcelWriteStrategy strategy = this.context.getStrategyMap().get(Filter.class);
        if (strategy == null) {
            return;
        }
        boolean frozenPane = (boolean) strategy.execute(this.context);
        String ref = ExcelUtils.toRangeReference(sheet, 0, 0, columnCount - 1, rowCount - 1);
        sheet.setAutoFilter(CellRangeAddress.valueOf(ref));
        if (frozenPane) {
            sheet.createFreezePane(0, 1);
        }
    }

    private void createHeader(Sheet sheet, List<ColumnDescriptor<T>> columns, CellStyle[] headerStyles) {
        Row row = sheet.createRow(0);
        List<String> headerNames = resolveHeaderNames(columns);
        Asserts.that(headerNames)
                .describedAs("headerNames is not allowed to be null or empty: {0}", headerNames)
                .isNotNull()
                .isNotEmpty()
                .describedAs("headerNames.size is not equal to columns.size (headerNames: {0}, columns: {1})",
                        headerNames.size(), columns.size())
                .hasSize(columns.size())
                .describedAs("headerNames cannot have null or blank element: {0}", headerNames)
                .noneMatch(StringUtils::isNullOrBlank)
                .describedAs("headerNames cannot have duplicated elements: {0}", headerNames)
                .doesNotHaveDuplicates();

        CellStyle defaultColumnStyle = null;
        if (this.workbook instanceof HSSFWorkbook
                || this.workbook instanceof XSSFWorkbook
                || this.workbook instanceof SXSSFWorkbook) {
            defaultColumnStyle = this.workbook.createCellStyle();
            defaultColumnStyle.setDataFormat(this.workbook.getCreationHelper().createDataFormat().getFormat("@"));
        }

        for (int i = 0; i < headerNames.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headerNames.get(i));

            if (defaultColumnStyle != null) {
                sheet.setDefaultColumnStyle(i, defaultColumnStyle);
            }

            if (ArrayUtils.isNullOrEmpty(headerStyles)) {
                continue;
            }
            CellStyle headerStyle = headerStyles.length == 1 ? headerStyles[0] : headerStyles[i];
            if (headerStyle != null) {
                cell.setCellStyle(headerStyle);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> resolveHeaderNames(List<ColumnDescriptor<T>> columns) {
        ExcelWriteStrategy strategy = this.context.getStrategyMap().get(HeaderNames.class);
        if (strategy != null) {
            return (List<String>) strategy.execute(this.context);
        }
        List<String> names = new ArrayList<>(columns.size());
        for (ColumnDescriptor<T> column : columns) {
            names.add(column.name());
        }
        return names;
    }

    private void createBody(Sheet sheet, List<ColumnDescriptor<T>> columns, List<T> chunk, CellStyle[] bodyStyles) {
        final int columnCount = columns.size();
        final int chunkSize = chunk.size();

        for (int i = 0; i < chunkSize; i++) {
            T model = chunk.get(i);
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < columnCount; j++) {
                Cell cell = row.createCell(j);
                String cellValue = columns.get(j).writeValue(model);

                if (!StringUtils.isNullOrEmpty(cellValue)) {
                    cell.setCellValue(cellValue);
                    storeColumnWidth(cellValue, j);
                }

                if (ArrayUtils.isNullOrEmpty(bodyStyles)) {
                    continue;
                }
                CellStyle bodyStyle = bodyStyles.length == 1 ? bodyStyles[0] : bodyStyles[j];
                if (bodyStyle != null) {
                    cell.setCellStyle(bodyStyle);
                }
            }
        }
    }

    private void storeColumnWidth(String cellValue, int columnIndex) {
        if (ArrayUtils.isNullOrEmpty(this.columnWidths)) {
            return;
        }
        int width = cellValue == null ? 0 : cellValue.length();
        this.columnWidths[columnIndex] = Math.max(width, this.columnWidths[columnIndex]);
    }

    private void applyEnumDropdowns(Sheet sheet, List<ColumnDescriptor<T>> columns) {
        Map<Integer, String[]> dropdowns = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            int idx = i;
            columns.get(i).dropdownItems().ifPresent(items -> dropdowns.put(idx, items));
        }
        if (dropdowns.isEmpty()) {
            return;
        }
        DataValidationHelper helper = sheet.getDataValidationHelper();
        dropdowns.forEach((columnIndex, items) -> {
            String ref = ExcelUtils.toColumnRangeReference(sheet, columnIndex);
            ExcelUtils.setValidation(sheet, helper, ref, items);
        });
    }

    private void applyAutoResizedColumns(Sheet sheet, int columnCount) {
        if (!this.context.getStrategyMap().containsKey(AutoResizedColumns.class)) {
            return;
        }
        if (ArrayUtils.isNullOrEmpty(this.columnWidths)) {
            ExcelUtils.autoResizeColumns(sheet, columnCount);
            return;
        }
        Row row = sheet.getRow(0);
        for (Cell cell : row) {
            String cellValue = cell.getStringCellValue();
            storeColumnWidth(cellValue, cell.getColumnIndex());
        }
        for (int i = 0; i < this.columnWidths.length; i++) {
            int width = ((int) (this.columnWidths[i] * 1.14388F)) * 256;
            sheet.setColumnWidth(i, width);
        }
    }

    private void applyHiddenExtraRows(Sheet sheet, int chunkSize) {
        if (this.context.getStrategyMap().containsKey(HiddenExtraRows.class)) {
            ExcelUtils.hideExtraRows(sheet, chunkSize + 1);
        }
    }

    private void applyHiddenExtraColumns(Sheet sheet, int columnCount) {
        if (this.context.getStrategyMap().containsKey(HiddenExtraColumns.class)) {
            ExcelUtils.hideExtraColumns(sheet, columnCount);
        }
    }

    private void closeResource(OutputStream out) {
        if (!this.context.getStrategyMap().containsKey(CloseResource.class)) {
            return;
        }
        try {
            if (this.workbook instanceof SXSSFWorkbook sxssfWorkbook) {
                sxssfWorkbook.dispose();
            }
            this.workbook.close();
            out.close();
        } catch (Exception ignored) {
        }
    }

    private void save(OutputStream out) {
        try {
            this.workbook.write(out);
        } catch (IOException e) {
            throw new WritingExcelException(e);
        }
    }

}
