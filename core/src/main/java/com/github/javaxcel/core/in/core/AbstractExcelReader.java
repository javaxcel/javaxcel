/*
 * Copyright 2021 Javaxcel
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ArrayUtils;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.StringUtils;

import com.github.javaxcel.core.in.context.ExcelReadContext;
import com.github.javaxcel.core.in.lifecycle.ExcelReadLifecycle;
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy;
import com.github.javaxcel.core.in.strategy.impl.KeyNames;
import com.github.javaxcel.core.in.strategy.impl.Limit;
import com.github.javaxcel.core.util.ExcelUtils;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

/**
 * Abstract Excel reader
 *
 * @param <T> type of model
 * @since 0.5.0
 */
public abstract class AbstractExcelReader<T> implements ExcelReader<T>, ExcelReadLifecycle<T> {

    /**
     * Formatter that stringifies the value in a cell with {@link FormulaEvaluator}.
     *
     * @see #readRow(Row)
     */
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /**
     * Evaluator that evaluates the formula in a cell.
     *
     * @see Workbook#getCreationHelper()
     * @see CreationHelper#createFormulaEvaluator()
     * @see #readRow(Row)
     */
    private final FormulaEvaluator formulaEvaluator;

    /**
     * Limitation of reading rows.
     */
    private int limit = -1;

    private final ExcelReadContext<T> context;

    @SuppressWarnings("unchecked")
    protected AbstractExcelReader(Workbook workbook, Class<T> modelType) {
        this.context = new ExcelReadContext<>(workbook, modelType, (Class<? extends ExcelReader<T>>) getClass());
        this.formulaEvaluator = resolveFormulaEvaluator(workbook);
    }

    @TestOnly
    @VisibleForTesting
    @SuppressWarnings("unused")
    AbstractExcelReader(ExcelReadContext<T> context) {
        this.context = context;
        this.formulaEvaluator = resolveFormulaEvaluator(context.getWorkbook());
    }

    @Nullable
    private static FormulaEvaluator resolveFormulaEvaluator(Workbook workbook) {
        try {
            return workbook.getCreationHelper().createFormulaEvaluator();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExcelReader<T> options(ExcelReadStrategy... strategies) {
        Asserts.that(strategies)
                .describedAs("strategies is not allowed to be null")
                .isNotNull()
                .describedAs("strategies cannot have null element: {0}", ArrayUtils.toString(strategies))
                .doesNotContainNull();

        if (strategies.length == 0) {
            return this;
        }

        // Makes each strategy be unique; removes duplication.
        // Equality of ExcelReadStrategy is determined by its class name, not implementing of equals and hashCode.
        Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> strategyMap = Arrays.stream(strategies)
                .filter(it -> it.isSupported(this.context))
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(it -> it.getClass().getName()))),
                        set -> set.stream().collect(toMap(ExcelReadStrategy::getClass, Function.identity()))));

        this.context.setStrategyMap(Collections.unmodifiableMap(strategyMap));

        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p> When an empty row is in the middle of body, computation of
     * the number of models by {@link ExcelUtils#getNumOfModels(Sheet)} is missed.
     * Models are missing as many empty rows.
     */
    @Override
    public final List<T> read() {
        // YOU MUST SET ALL THE ATTRIBUTES YOU CAN DO BEFORE ExcelReadLifecycle.prepare
        // BECAUSE THIS CLASS IS OBLIGATED TO PROVIDE AN IMPLEMENTATION WITH THEM.
        List<T> list = new ArrayList<>();
        this.context.setList(list);

        resolveLimit();
        resolveHeaderNames();

        // Lifecycle method.
        prepare(this.context);

        List<Sheet> sheets = ExcelUtils.getSheets(this.context.getWorkbook());
        for (Sheet sheet : sheets) {
            if (this.context.getReadCount() == this.limit) {
                break;
            }

            this.context.setSheet(sheet);

            // Lifecycle method.
            preReadSheet(this.context);

            // Resolve header names if you don't give the option.
            List<String> headerNames = this.context.getHeaderNames().isEmpty()
                    ? readHeader(this.context) : Collections.emptyList();
            if (CollectionUtils.exists(headerNames)) {
                this.context.setHeaderNames(headerNames);
            }

            List<T> chunk = readBody(this.context);
            this.context.setChunk(chunk);
            list.addAll(chunk);

            // Lifecycle method.
            postReadSheet(this.context);
        }

        // Lifecycle method.
        complete(this.context);

        return list;
    }

    private void resolveLimit() {
        ExcelReadStrategy strategy = this.context.getStrategyMap().get(Limit.class);
        if (strategy == null) {
            return;
        }

        this.limit = (int) strategy.execute(this.context);
    }

    @SuppressWarnings("unchecked")
    private void resolveHeaderNames() {
        ExcelReadStrategy strategy = this.context.getStrategyMap().get(KeyNames.class);
        if (strategy == null) {
            return;
        }

        List<String> headerNames = (List<String>) strategy.execute(this.context);
        this.context.setHeaderNames(headerNames);
    }

    /**
     * Reads the body part of sheet and returns maps.
     *
     * @param sheet Excel sheet
     * @return models read as map
     */
    protected final List<Map<String, String>> readBodyAsMaps(Sheet sheet) {
        List<Map<String, String>> maps = new ArrayList<>();
        for (Row row : sheet) {
            // ExcelReader already read a header, so skip the first row in this method.
            if (row.getRowNum() == 0) {
                continue;
            }

            if (this.context.getReadCount() == this.limit) {
                break;
            }

            Map<String, String> rowMap = readRow(row);
            maps.add(rowMap);
        }

        return Collections.unmodifiableList(maps);
    }

    /**
     * Converts a row to the imitated model.
     *
     * <p> Reads rows to get data. this creates {@link Map} as an imitated model
     * and puts the key({@link Field#getName()}) and the value
     * ({@link DataFormatter#formatCellValue(Cell, FormulaEvaluator)})
     * to the model. The result is the same as the following code.
     *
     * <pre><code>
     *     +------+--------+--------+----------+
     *     | name | height | weight | eyesight |
     *     +------+--------+--------+----------+
     *     | John | 180.5  | 79.2   |          |
     *     +------+--------+--------+----------+
     *
     *     This row will be converted to
     *
     *     { "name": "John", "height": "180.5", "weight": "79.2", "eyesight": null }
     * </code></pre>
     *
     * @param row row in sheet
     * @return imitated model
     */
    private Map<String, String> readRow(Row row) {
        Map<String, String> map = new HashMap<>();

        int columnCount = CollectionUtils.exists(this.context.getHeaderNames())
                ? this.context.getHeaderNames().size()
                : row.getLastCellNum();

        for (int i = 0; i < columnCount; i++) {
            Cell cell = row.getCell(i);

            String cellValue;
            if (cell == null) {
                cellValue = null;
            } else if (this.formulaEvaluator == null) {
                cellValue = cell.getStringCellValue();
            } else {
                // Evaluates the formula and returns a stringified value.
                cellValue = DATA_FORMATTER.formatCellValue(cell, this.formulaEvaluator);
            }

            // Converts empty string to null because when CellType is BLANK,
            // DataFormatter returns empty string.
            String headerName = this.context.getHeaderNames().get(i);
            map.put(headerName, StringUtils.ifNullOrEmpty(cellValue, (String) null));
        }

        // Increases read count of row.
        this.context.increaseReadCount();

        return Collections.unmodifiableMap(map);
    }

    // Overridable -------------------------------------------------------------------------------------

    /**
     * Reads the first row as header for each sheet.
     *
     * @param context context with current sheet
     */
    protected abstract List<String> readHeader(ExcelReadContext<T> context);

    /**
     * Reads the second row and below as body for each sheet.
     *
     * @param context context with current sheet
     */
    protected abstract List<T> readBody(ExcelReadContext<T> context);

}
