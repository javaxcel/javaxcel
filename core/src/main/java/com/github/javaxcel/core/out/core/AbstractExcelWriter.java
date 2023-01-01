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

package com.github.javaxcel.core.out.core;

import com.github.javaxcel.core.exception.WritingExcelException;
import com.github.javaxcel.core.out.context.ExcelWriteContext;
import com.github.javaxcel.core.out.lifecycle.ExcelWriteLifecycle;
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;
import com.github.javaxcel.core.out.strategy.impl.AutoResizedColumns;
import com.github.javaxcel.core.out.strategy.impl.SheetName;
import com.github.javaxcel.core.util.ExcelUtils;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ArrayUtils;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.NumberUtils;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Abstract Excel writer
 *
 * @param <T> type of model
 * @since 0.5.0
 */
public abstract class AbstractExcelWriter<T> implements ExcelWriter<T>, ExcelWriteLifecycle<T> {

    /**
     * Default cell style configuration to save memory.
     */
    protected static final ExcelStyleConfig DEFAULT_STYLE_CONFIG = new NoStyleConfig();

    final ExcelWriteContext<T> context;

    private int[] columnWidths;

    /**
     * Creates a writer for model.
     *
     * @param workbook  Excel workbook
     * @param modelType type of Excel model
     */
    @SuppressWarnings("unchecked")
    protected AbstractExcelWriter(Workbook workbook, Class<T> modelType) {
        Class<? extends ExcelWriter<T>> writerType = (Class<? extends ExcelWriter<T>>) getClass();
        this.context = new ExcelWriteContext<>(workbook, modelType, writerType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ExcelWriter<T> options(ExcelWriteStrategy... strategies) {
        Asserts.that(strategies)
                .describedAs("strategies is not allowed to be null")
                .isNotNull()
                .describedAs("strategies cannot have null element: {0}", ArrayUtils.toString(strategies))
                .doesNotContainNull();

        if (strategies.length == 0) {
            return this;
        }

        // Makes each strategy be unique; removes duplication.
        Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategyMap = Arrays.stream(strategies)
                .distinct().filter(it -> it.isSupported(this.context))
                .collect(toMap(ExcelWriteStrategy::getClass, Function.identity()));
        this.context.setStrategyMap(Collections.unmodifiableMap(strategyMap));

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void write(OutputStream out, List<T> list) {
        // YOU MUST SET ALL THE ATTRIBUTES YOU CAN DO BEFORE ExcelWriteLifecycle.prepare
        // BECAUSE THIS CLASS IS OBLIGATED TO PROVIDE A IMPLEMENTATION WITH THEM.
        this.context.setList(list);

        // Lifecycle method.
        prepare(this.context);

        setupAutoResizeColumns();

        Workbook workbook = this.context.getWorkbook();
        final int maxRows = ExcelUtils.getMaxRows(workbook) - 1; // Subtracts 1 because of header row.
        List<List<T>> chunkedList = CollectionUtils.partitionBySize(list, maxRows);
        final int sheetCount = NumberUtils.toPositive(chunkedList.size());

        // Creates sheet names by this or implementation.
        List<String> sheetNames = createSheetNames(this.context, sheetCount);
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

        for (int i = 0; i < sheetCount; i++) {
            String sheetName = sheetNames.get(i);
            Sheet sheet = workbook.createSheet(sheetName);

            // To write 1 sheet at least, even if the list is empty.
            List<T> chunk = chunkedList.isEmpty() ? Collections.emptyList() : chunkedList.get(i);
            this.context.setChunk(chunk);
            this.context.setSheet(sheet);

            // Lifecycle method.
            preWriteSheet(this.context);

            createHeader(this.context);
            createBody(this.context);

            // Lifecycle method.
            postWriteSheet(this.context);

            // Applies the options.
            applyAutoResizedColumns();
        }

        save(out);

        // Lifecycle method.
        complete(this.context);
    }

    private void setupAutoResizeColumns() {
        ExcelWriteStrategy strategy = this.context.getStrategyMap().get(AutoResizedColumns.class);
        if (strategy == null) {
            return;
        }

        boolean manual = (boolean) strategy.execute(this.context);
        if (!manual) {
            return;
        }

        this.columnWidths = new int[getColumnCount()];
    }

    /**
     * Creates the second row and below as body for each sheet.
     *
     * @param context context with current sheet and chunked models
     */
    protected final void createBody(ExcelWriteContext<T> context) {
        Sheet sheet = context.getSheet();

        List<T> chunk = context.getChunk();
        CellStyle[] bodyStyles = context.getBodyStyles();
        final int chunkSize = chunk.size();
        final int columnCount = getColumnCount();

        for (int i = 0; i < chunkSize; i++) {
            T model = chunk.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < columnCount; j++) {
                Cell cell = row.createCell(j);
                String cellValue = createCellValue(model, j);

                // Stores the max width of each cell.
                storeColumnWidth(cellValue, j);

                // Doesn't write even empty string.
                if (!StringUtils.isNullOrEmpty(cellValue)) {
                    cell.setCellValue(cellValue);
                }

                if (ArrayUtils.isNullOrEmpty(bodyStyles)) {
                    continue;
                }

                // Sets styles to body's cell.
                CellStyle bodyStyle;
                if (bodyStyles.length == 1) {
                    bodyStyle = bodyStyles[0];
                } else {
                    bodyStyle = bodyStyles[j];
                }

                // There is possibility that bodyStyles has null elements, if you set NoStyleConfig.
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

    private void applyAutoResizedColumns() {
        if (!this.context.getStrategyMap().containsKey(AutoResizedColumns.class)) {
            return;
        }

        if (ArrayUtils.isNullOrEmpty(this.columnWidths)) {
            ExcelUtils.autoResizeColumns(this.context.getSheet(), getColumnCount());
            return;
        }

        // 1.14388 is a max character width of the "Serif" font and 256 font units.
        for (int i = 0; i < this.columnWidths.length; i++) {
            int width = ((int) (this.columnWidths[i] * 1.14388F)) * 256;
            this.context.getSheet().setColumnWidth(i, width);
        }
    }

    /**
     * Saves models into an Excel file.
     *
     * @param out output stream
     */
    private void save(OutputStream out) {
        try {
            this.context.getWorkbook().write(out);
        } catch (IOException e) {
            throw new WritingExcelException(e);
        }
    }

    // Overridable -------------------------------------------------------------------------------------

    /**
     * Creates sheet names following with the below instructions.
     *
     * <ul>
     *     <li>Sheet names don't be null or empty.</li>
     *     <li>Sheet names don't have null element.</li>
     *     <li>Sheet names don't have duplicated elements.</li>
     *     <li>The number of sheet names is equal to sheetCount.</li>
     * </ul>
     *
     * @param context    context with current sheet and chunked models.
     * @param sheetCount the number of sheets
     * @return sheet names
     * @throws IllegalArgumentException if sheet names is null or empty
     */
    protected List<String> createSheetNames(ExcelWriteContext<T> context, int sheetCount) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(SheetName.class);
        String sheetName = strategy == null ? "Sheet" : (String) strategy.execute(context);

        if (sheetCount < 2) {
            return Collections.singletonList(sheetName);
        }

        List<String> sheetNames = new ArrayList<>();
        for (int i = 1; i <= sheetCount; i++) {
            sheetNames.add(sheetName + i);
        }

        return Collections.unmodifiableList(sheetNames);
    }

    /**
     * Creates the first row as header for each sheet.
     *
     * @param context context with current sheet and chunked models
     */
    protected abstract void createHeader(ExcelWriteContext<T> context);

    /**
     * Returns the number of columns.
     *
     * @return number of columns
     * @since 0.9.1
     */
    protected abstract int getColumnCount();

    /**
     * Returns the value as cell value.
     *
     * @param model       Excel model
     * @param columnIndex index of the cell
     * @return cell value
     * @since 0.9.1
     */
    @Nullable
    protected abstract String createCellValue(T model, int columnIndex);

}
