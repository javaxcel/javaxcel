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

import com.github.javaxcel.core.analysis.ExcelAnalysis;
import com.github.javaxcel.core.analysis.ExcelAnalyzer;
import com.github.javaxcel.core.analysis.out.ExcelWriteAnalyzer;
import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelModel;
import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.converter.out.ExcelWriteConverter;
import com.github.javaxcel.core.converter.out.support.ExcelWriteConverters;
import com.github.javaxcel.core.exception.NoTargetedFieldException;
import com.github.javaxcel.core.out.context.ExcelWriteContext;
import com.github.javaxcel.core.out.core.AbstractExcelWriter;
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;
import com.github.javaxcel.core.out.strategy.impl.BodyStyles;
import com.github.javaxcel.core.out.strategy.impl.EnumDropdown;
import com.github.javaxcel.core.out.strategy.impl.Filter;
import com.github.javaxcel.core.out.strategy.impl.HeaderNames;
import com.github.javaxcel.core.out.strategy.impl.HeaderStyles;
import com.github.javaxcel.core.util.ExcelUtils;
import com.github.javaxcel.core.util.FieldUtils;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ArrayUtils;
import io.github.imsejin.common.util.ReflectionUtils;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Excel writer for model
 *
 * @param <T> type of model
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ModelWriter<T> extends AbstractExcelWriter<T> {

    /**
     * The fields of type that will be actually written in Excel file.
     */
    private final List<Field> fields;

    private final ExcelTypeHandlerRegistry registry;

    private ExcelWriteConverter converter;

    private Map<Integer, String[]> enumDropdownMap;

    /**
     * Creates a writer for model.
     *
     * @param workbook  Excel workbook
     * @param modelType type of Excel model
     * @param registry  registry of handlers for field type
     */
    public ModelWriter(Workbook workbook, Class<T> modelType, ExcelTypeHandlerRegistry registry) {
        super(workbook, modelType);

        // Finds the targeted fields.
        List<Field> fields = FieldUtils.getTargetedFields(modelType);
        Asserts.that(fields)
                .describedAs("ModelWriter.fields cannot find the targeted fields in the class: {0}", modelType.getName())
                .thrownBy(desc -> new NoTargetedFieldException(modelType, desc))
                .isNotEmpty()
                .describedAs("ModelWriter.fields cannot have null element: {0}", fields)
                .doesNotContainNull();

        // To prevent exception from occurring on multi-threaded environment,
        // Permits access to the fields that are not accessible. (ExcelReadStrategy.Parallel)
        fields.stream().filter(it -> !it.isAccessible()).forEach(it -> it.setAccessible(true));
        this.fields = Collections.unmodifiableList(fields);

        Asserts.that(registry)
                .describedAs("ModelWriter.registry is not allowed to be null")
                .isNotNull();
        this.registry = registry;
    }

    @Override
    public void prepare(ExcelWriteContext<T> context) {
        // Analyzes the fields with arguments.
        ExcelAnalyzer analyzer = new ExcelWriteAnalyzer(this.registry);
        Collection<ExcelWriteStrategy> strategies = context.getStrategyMap().values();
        List<ExcelAnalysis> analyses = analyzer.analyze(this.fields, strategies.toArray());

        // Creates a converter.
        this.converter = new ExcelWriteConverters(analyses, this.registry);

        // Handles the given options.
        resolveEnumDropdown(context);
        resolveHeaderStyles(context);
        resolveBodyStyles(context);
    }

    private void resolveEnumDropdown(ExcelWriteContext<T> context) {
        Map<Integer, String[]> enumDropdownMap = new HashMap<>();

        boolean enabled = context.getStrategyMap().containsKey(EnumDropdown.class);
        if (!enabled) {
            ExcelModel excelModel = context.getModelType().getAnnotation(ExcelModel.class);
            enabled = excelModel != null && excelModel.enumDropdown();
        }

        for (int i = 0; i < this.fields.size(); i++) {
            Field field = this.fields.get(i);
            if (!field.getType().isEnum()) {
                continue;
            }

            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            enabled |= excelColumn != null && excelColumn.enumDropdown();
            if (!enabled) {
                continue;
            }

            String[] dropdowns;
            if (excelColumn != null && excelColumn.dropdownItems().length > 0) {
                // Sets custom dropdown items for enum.
                dropdowns = excelColumn.dropdownItems();

            } else {
                // Sets default dropdown items for enum.
                Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
                dropdowns = (String[]) EnumSet.allOf(enumType).stream()
                        .map(e -> ((Enum) e).name()).toArray(String[]::new);
            }

            enumDropdownMap.put(i, dropdowns);
        }

        // Assigns null if map is empty.
        if (!enumDropdownMap.isEmpty()) {
            this.enumDropdownMap = enumDropdownMap;
        }
    }

    private void resolveHeaderStyles(ExcelWriteContext<T> context) {
        Workbook workbook = context.getWorkbook();
        ExcelWriteStrategy strategy = context.getStrategyMap().get(HeaderStyles.class);

        // Sets configurations for header styles by ExcelStyleConfig.
        if (strategy != null) {
            List<ExcelStyleConfig> headerStyleConfigs = (List<ExcelStyleConfig>) strategy.execute(context);

            // Validates header styles.
            Asserts.that(headerStyleConfigs)
                    .describedAs("headerStyles.size must be 1 or equal to fields.size (headerStyles.size: {0}, fields.size: {1})",
                            headerStyleConfigs.size(), this.fields.size())
                    .is(them -> them.size() == 1 || them.size() == this.fields.size());

            ExcelStyleConfig[] headerConfigs = headerStyleConfigs.toArray(new ExcelStyleConfig[0]);
            CellStyle[] headerStyles = ExcelUtils.toCellStyles(workbook, headerConfigs);
            context.setHeaderStyles(headerStyles);

            return;
        }

        // Sets configurations for header styles by ExcelModel.
        if (context.getModelType().isAnnotationPresent(ExcelModel.class)) {
            ExcelModel excelModel = context.getModelType().getAnnotation(ExcelModel.class);

            // Sets cell style for header.
            ExcelStyleConfig headerConfig = excelModel.headerStyle() == NoStyleConfig.class
                    ? DEFAULT_STYLE_CONFIG : ReflectionUtils.instantiate(excelModel.headerStyle());
            ExcelStyleConfig[] headerConfigs = IntStream.range(0, this.fields.size())
                    .mapToObj(i -> headerConfig).toArray(ExcelStyleConfig[]::new);

            CellStyle[] headerStyles = ExcelUtils.toCellStyles(workbook, headerConfigs);
            context.setHeaderStyles(headerStyles);
        }

        // Unless configure header styles with ExcelModel, creates empty arrays.
        CellStyle[] headerStyles = ArrayUtils.isNullOrEmpty(context.getHeaderStyles())
                ? new CellStyle[this.fields.size()]
                : context.getHeaderStyles();

        for (int i = 0; i < headerStyles.length; i++) {
            Field field = this.fields.get(i);
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);

            // Null means that not overrides style on the column.
            if (excelColumn == null) {
                continue;
            }

            // Replaces header style of ExcelModel with header style of ExcelColumn.
            Class<? extends ExcelStyleConfig> headerConfigType = excelColumn.headerStyle();
            if (headerConfigType == NoStyleConfig.class) {
                continue;
            }

            ExcelStyleConfig headerConfig = ReflectionUtils.instantiate(headerConfigType);
            CellStyle cellStyle = ExcelUtils.toCellStyle(workbook, headerConfig);
            headerStyles[i] = cellStyle;
        }

        context.setHeaderStyles(headerStyles);
    }

    private void resolveBodyStyles(ExcelWriteContext<T> context) {
        Workbook workbook = context.getWorkbook();
        ExcelWriteStrategy strategy = context.getStrategyMap().get(BodyStyles.class);

        // Sets configurations for body styles by ExcelStyleConfig.
        if (strategy != null) {
            List<ExcelStyleConfig> bodyStyleConfigs = (List<ExcelStyleConfig>) strategy.execute(context);

            // Validates body styles.
            Asserts.that(bodyStyleConfigs)
                    .describedAs("bodyStyles.size must be 1 or equal to fields.size (bodyStyles.size: {0}, fields.size: {1})",
                            bodyStyleConfigs.size(), this.fields.size())
                    .is(them -> them.size() == 1 || them.size() == this.fields.size());

            ExcelStyleConfig[] bodyConfigs = bodyStyleConfigs.toArray(new ExcelStyleConfig[0]);
            CellStyle[] bodyStyles = ExcelUtils.toCellStyles(workbook, bodyConfigs);
            context.setBodyStyles(bodyStyles);

            return;
        }

        // Sets configurations for body styles by ExcelModel.
        if (context.getModelType().isAnnotationPresent(ExcelModel.class)) {
            ExcelModel excelModel = context.getModelType().getAnnotation(ExcelModel.class);

            // Sets cell style for body.
            ExcelStyleConfig bodyConfig = excelModel.bodyStyle() == NoStyleConfig.class
                    ? DEFAULT_STYLE_CONFIG : ReflectionUtils.instantiate(excelModel.bodyStyle());
            ExcelStyleConfig[] bodyConfigs = IntStream.range(0, this.fields.size())
                    .mapToObj(i -> bodyConfig).toArray(ExcelStyleConfig[]::new);

            CellStyle[] bodyStyles = ExcelUtils.toCellStyles(workbook, bodyConfigs);
            context.setBodyStyles(bodyStyles);
        }

        // Unless configure body styles with ExcelModel, creates empty arrays.
        CellStyle[] bodyStyles = ArrayUtils.isNullOrEmpty(context.getBodyStyles())
                ? new CellStyle[this.fields.size()]
                : context.getBodyStyles();

        for (int i = 0; i < this.fields.size(); i++) {
            Field field = this.fields.get(i);
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);

            // Null means that not overrides style on the column.
            if (excelColumn == null) {
                continue;
            }

            // Replaces body style of ExcelModel with body style of ExcelColumn.
            Class<? extends ExcelStyleConfig> bodyConfigType = excelColumn.bodyStyle();
            if (bodyConfigType == NoStyleConfig.class) {
                continue;
            }

            ExcelStyleConfig bodyConfig = ReflectionUtils.instantiate(bodyConfigType);
            CellStyle cellStyle = ExcelUtils.toCellStyle(workbook, bodyConfig);
            bodyStyles[i] = cellStyle;
        }

        context.setBodyStyles(bodyStyles);
    }

    @Override
    public void preWriteSheet(ExcelWriteContext<T> context) {
        resolveFilter(context);
    }

    private void resolveFilter(ExcelWriteContext<T> context) {
        if (!context.getStrategyMap().containsKey(Filter.class)) {
            return;
        }

        ExcelWriteStrategy strategy = context.getStrategyMap().get(Filter.class);
        boolean frozenPane = (boolean) strategy.execute(context);

        Sheet sheet = context.getSheet();
        String ref = ExcelUtils.toRangeReference(sheet, 0, 0, this.fields.size() - 1, context.getChunk().size() - 1);
        sheet.setAutoFilter(CellRangeAddress.valueOf(ref));

        if (frozenPane) {
            sheet.createFreezePane(0, 1);
        }
    }

    @Override
    protected void createHeader(ExcelWriteContext<T> context) {
        // Creates the first row that is header.
        Row row = context.getSheet().createRow(0);

        List<String> headerNames = resolveHeaderNames(context);
        Asserts.that(headerNames)
                .describedAs("headerNames is not allowed to be null or empty: {0}", headerNames)
                .isNotNull().isNotEmpty()
                .describedAs("headerNames.size is not equal to the number of targeted fields in the class: {0}",
                        context.getModelType().getName())
                .hasSameSizeAs(this.fields)
                .describedAs("headerNames cannot have null or blank element: {0}", headerNames)
                .noneMatch(StringUtils::isNullOrBlank)
                .describedAs("headerNames cannot have duplicated elements: {0}", headerNames)
                .doesNotHaveDuplicates();

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
            CellStyle headerStyle;
            if (headerStyles.length == 1) {
                headerStyle = headerStyles[0];
            } else {
                headerStyle = headerStyles[i];
            }

            // There is possibility that headerStyles has null elements, if you set NoStyleConfig.
            if (headerStyle != null) {
                cell.setCellStyle(headerStyle);
            }
        }
    }

    private List<String> resolveHeaderNames(ExcelWriteContext<T> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(HeaderNames.class);
        if (strategy != null) {
            return (List<String>) strategy.execute(context);
        }

        return FieldUtils.toHeaderNames(this.fields, false);
    }

    @Override
    protected int getColumnCount() {
        return this.fields.size();
    }

    @Nullable
    @Override
    protected String createCellValue(T model, int columnIndex) {
        Field field = this.fields.get(columnIndex);

        // Converts field value into the string.
        return this.converter.convert(model, field);
    }

    @Override
    public void postWriteSheet(ExcelWriteContext<T> context) {
        // Creates constraint for columns of enum.
        if (this.enumDropdownMap != null) {
            createDropdowns(context.getSheet());
        }
    }

    /**
     * Creates dropdowns for columns of {@link Enum}.
     */
    private void createDropdowns(Sheet sheet) {
        DataValidationHelper helper = sheet.getDataValidationHelper();

        this.enumDropdownMap.forEach((i, them) -> {
            // Creates reference of the column range except first row.
            String ref = ExcelUtils.toColumnRangeReference(sheet, i);

            // Sets validation with the dropdown items at the reference.
            ExcelUtils.setValidation(sheet, helper, ref, them);
        });
    }

}
