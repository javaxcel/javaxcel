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

package com.github.javaxcel.core.out.core.impl;

import com.github.javaxcel.core.out.context.ExcelWriteContext;
import com.github.javaxcel.core.out.core.AbstractExcelWriter;
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;
import com.github.javaxcel.core.out.strategy.impl.BodyStyles;
import com.github.javaxcel.core.out.strategy.impl.DefaultValue;
import com.github.javaxcel.core.out.strategy.impl.Filter;
import com.github.javaxcel.core.out.strategy.impl.HeaderStyles;
import com.github.javaxcel.core.out.strategy.impl.KeyNames;
import com.github.javaxcel.core.util.ExcelUtils;
import com.github.javaxcel.styler.ExcelStyleConfig;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ArrayUtils;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Excel writer for {@link Map}
 *
 * @since 0.5.0
 */
@SuppressWarnings("unchecked")
public class MapWriter extends AbstractExcelWriter<Map<String, Object>> {

    private static final Class<Map<String, Object>> MAP_TYPE;

    private List<String> keys;

    private List<String> headerNames;

    /**
     * Default column value when the value is null or empty.
     *
     * @see DefaultValue
     */
    private String defaultValue;

    static {
        try {
            // incompatible types: java.lang.Class<java.util.Map> cannot be converted to java.lang.Class<T>
            MAP_TYPE = (Class<Map<String, Object>>) Class.forName(Map.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Create a writer for {@link Map}.
     *
     * @param workbook Excel workbook
     */
    public MapWriter(Workbook workbook) {
        super(workbook, MAP_TYPE);
    }

    @Override
    public void prepare(ExcelWriteContext<Map<String, Object>> context) {
        setKeys(context);
        changeKeys(context);
        setDefaultValue(context);
        setHeaderStyles(context);
        setBodyStyles(context);
    }

    private void setKeys(ExcelWriteContext<Map<String, Object>> context) {
        List<Map<String, Object>> list = context.getList();

        // Gets the keys of all maps.
        List<String> keys = list.stream().flatMap(it -> it.keySet().stream()).distinct().collect(toList());

        // To write a header, this doesn't allow accepting invalid keys.
        Asserts.that(keys)
                .describedAs("MapWriter.keys is not allowed to be empty")
                .isNotEmpty()
                .describedAs("MapWriter.keys cannot have null or blank element: {0}", keys)
                .noneMatch(StringUtils::isNullOrBlank)
                .describedAs("MapWriter.keys cannot have duplicated elements: {0}", keys)
                .doesNotHaveDuplicates();
        this.keys = keys;
    }

    private void changeKeys(ExcelWriteContext<Map<String, Object>> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(KeyNames.class);
        if (strategy == null) return;

        Map<String, Object> keyMap = (Map<String, Object>) strategy.execute(context);
        Map<String, Integer> orders = (Map<String, Integer>) keyMap.get("orders");

        // Validates the number of ordered keys and their each element.
        Asserts.that(this.keys)
                .describedAs("MapWriter.keys is not equal to keyMap.orders.size (keys.size: {0}, keyMap.orders.size: {1})",
                        this.keys.size(), orders.size())
                .hasSize(orders.size())
                .describedAs("MapWriter.keys is at variance with keyMap.orders.keySet (keys: {0}, keyMap.orders.keySet: {1})",
                        this.keys, orders.keySet())
                .containsOnly(orders.keySet().toArray(new String[0]));

        if (keyMap.containsKey("names")) this.headerNames = (List<String>) keyMap.get("names");

        // Rearranges the keys as you want: it changes order of columns.
        this.keys.sort(comparing(orders::get));
    }

    private void setDefaultValue(ExcelWriteContext<Map<String, Object>> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(DefaultValue.class);
        if (strategy == null) return;

        this.defaultValue = (String) strategy.execute(context);
    }

    private void setHeaderStyles(ExcelWriteContext<Map<String, Object>> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(HeaderStyles.class);
        if (strategy == null) return;

        List<ExcelStyleConfig> headerStyleConfigs = (List<ExcelStyleConfig>) strategy.execute(context);

        // Validates header styles.
        Asserts.that(headerStyleConfigs)
                .describedAs("headerStyles.size must be 1 or equal to keys.size (headerStyles.size: {0}, keys.size: {1})",
                        headerStyleConfigs.size(), this.keys.size())
                .is(them -> them.size() == 1 || them.size() == this.keys.size());

        ExcelStyleConfig[] headerConfigs = headerStyleConfigs.toArray(new ExcelStyleConfig[0]);
        CellStyle[] headerStyles = ExcelUtils.toCellStyles(context.getWorkbook(), headerConfigs);
        context.setHeaderStyles(headerStyles);
    }

    private void setBodyStyles(ExcelWriteContext<Map<String, Object>> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(BodyStyles.class);
        if (strategy == null) return;

        List<ExcelStyleConfig> bodyStyleConfigs = (List<ExcelStyleConfig>) strategy.execute(context);

        // Validates body styles.
        Asserts.that(bodyStyleConfigs)
                .describedAs("bodyStyles.size must be 1 or equal to keys.size (bodyStyles.size: {0}, keys.size: {1})",
                        bodyStyleConfigs.size(), this.keys.size())
                .is(them -> them.size() == 1 || them.size() == this.keys.size());

        ExcelStyleConfig[] bodyConfigs = bodyStyleConfigs.toArray(new ExcelStyleConfig[0]);
        CellStyle[] bodyStyles = ExcelUtils.toCellStyles(context.getWorkbook(), bodyConfigs);
        context.setBodyStyles(bodyStyles);
    }

    @Override
    public void preWriteSheet(ExcelWriteContext<Map<String, Object>> context) {
        if (context.getStrategyMap().containsKey(Filter.class)) {
            ExcelWriteStrategy strategy = context.getStrategyMap().get(Filter.class);
            boolean frozenPane = (boolean) strategy.execute(context);

            Sheet sheet = context.getSheet();
            String ref = ExcelUtils.toRangeReference(sheet, 0, 0, this.keys.size() - 1, context.getChunk().size() - 1);
            sheet.setAutoFilter(CellRangeAddress.valueOf(ref));

            if (frozenPane) sheet.createFreezePane(0, 1);
        }
    }

    @Override
    protected void createHeader(ExcelWriteContext<Map<String, Object>> context) {
        // Creates the first row that is header.
        Row row = context.getSheet().createRow(0);

        List<String> headerNames = this.headerNames == null ? this.keys : this.headerNames;
        CellStyle[] headerStyles = context.getHeaderStyles();

        // Names the header given values.
        final int headerCount = headerNames.size();
        for (int i = 0; i < headerCount; i++) {
            String headerName = headerNames.get(i);

            Cell cell = row.createCell(i);
            cell.setCellValue(headerName);

            if (ArrayUtils.isNullOrEmpty(headerStyles)) {
                continue;
            }

            // Sets common style to all header cells or each style to each header cell.
            CellStyle headerStyle = headerStyles.length == 1
                    ? headerStyles[0] : headerStyles[i];

            // There is possibility that headerStyles has null elements, if you set NoStyleConfig.
            if (headerStyle != null) {
                cell.setCellStyle(headerStyle);
            }
        }
    }

    @Override
    protected int getColumnCount() {
        return this.keys.size();
    }

    @Nullable
    @Override
    protected String createCellValue(Map<String, Object> model, int columnIndex) {
        Object value = model.get(this.keys.get(columnIndex));

        if (value != null) {
            return value.toString();
        }

        if (!StringUtils.isNullOrEmpty(this.defaultValue)) {
            return this.defaultValue;
        }

        return null;
    }

}
