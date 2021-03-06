/*
 * Copyright 2020 Javaxcel
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

package com.github.javaxcel.in;

import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel reader for {@link Map}.
 *
 * @param <W> excel workbook
 * @param <T> {@link Map}
 */
public final class MapReader<W extends Workbook, T extends Map<String, ?>> extends AbstractExcelReader<W, T> {

    private int numOfColumns;

    private final List<String> headerNames = new ArrayList<>();

    private MapReader(W workbook) {
        super(workbook);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapReader}
     */
    @Override
    public MapReader<W, T> limit(int limit) {
        super.limit(limit);
        return this;
    }

    public MapReader<W, T> headerNames(List<String> headerNames) {
        if (CollectionUtils.isNullOrEmpty(headerNames)) {
            throw new IllegalArgumentException("Header names cannot be null or empty");
        }

        // Replaces current header names with the new things.
        if (!this.headerNames.isEmpty()) this.headerNames.clear();

        this.headerNames.addAll(headerNames);

        return this;
    }

    //////////////////////////////////////// Hooks ////////////////////////////////////////

    @Override
    protected List<T> readSheet(Sheet sheet) {
        int numOfRows = ExcelUtils.getNumOfModels(sheet);
        if (this.limit >= 0) numOfRows = Math.min(this.limit, numOfRows);

        Row header = sheet.getRow(0);
        this.numOfColumns = header.getPhysicalNumberOfCells();

        // If header names is empty, sets first row's values to it.
        if (this.headerNames.isEmpty()) {
            for (int i = 0; i < this.numOfColumns; i++) {
                Cell cell = header.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                // If cell value in first row is empty, sets stringified column number.
                String headerName = StringUtils.ifNullOrEmpty(cell.getStringCellValue(), String.valueOf(i));
                this.headerNames.add(headerName);
            }
        }

        // Reads rows.
        List<Map<String, Object>> maps = new ArrayList<>();
        for (int i = 0; i < numOfRows; i++) {
            if (this.numOfRowsRead == this.limit) break;

            // Skips the first row that is header.
            Row row = sheet.getRow(i + 1);

            // Adds a row data of the sheet.
            maps.add(readRow(row));
        }

        return (List<T>) maps;
    }

    @Override
    protected int getNumOfColumns(Row row) {
        return this.numOfColumns;
    }

    @Override
    protected String getColumnName(Cell cell, int columnIndex) {
        return this.headerNames.get(columnIndex);
    }

}
