package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelWriterExpression;
import com.github.javaxcel.converter.impl.BasicWritingConverter;
import com.github.javaxcel.converter.impl.ExpressiveWritingConverter;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.styler.config.ExcelStyleConfig;
import com.github.javaxcel.util.FieldUtils;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Excel writer
 *
 * @param <W> workbook
 * @param <T> the type of model
 */
public final class ModelWriter<W extends Workbook, T> extends AbstractExcelWriter<W, T> {

    private final BasicWritingConverter<T> basicConverter = new BasicWritingConverter<>();

    private final ExpressiveWritingConverter<T> expConverter;

    private final Class<T> type;

    /**
     * The type's fields that will be actually written in excel.
     */
    private final List<Field> fields;

    private ModelWriter(W workbook, Class<T> type) {
        super(workbook);

        if (type == null) throw new IllegalArgumentException("Type cannot be null");
        this.type = type;

        // Finds targeted fields.
        this.fields = FieldUtils.getTargetedFields(type);
        if (this.fields.isEmpty()) throw new NoTargetedFieldException(type);

        // Caches expressions for each field to improve performance.
        this.expConverter = new ExpressiveWritingConverter<>(this.fields);

        // If default value for all fields is not empty string, sets it into converters.
        ExcelModel excelModel = type.getAnnotation(ExcelModel.class);
        if (excelModel != null && !excelModel.defaultValue().equals("")) {
            this.basicConverter.setDefaultValue(excelModel.defaultValue());
            this.expConverter.setDefaultValue(excelModel.defaultValue());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> headerNames(List<String> headerNames) {
        super.headerNames(headerNames);
        if (headerNames.size() != this.fields.size()) {
            throw new IllegalArgumentException(String.format(
                    "The number of header names is not equal to the number of targeted fields in the class '%s'", this.type.getName()));
        }

        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> defaultValue(String defaultValue) {
        super.defaultValue(defaultValue);
        this.basicConverter.setDefaultValue(defaultValue);
        this.expConverter.setDefaultValue(defaultValue);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> sheetName(String sheetName) {
        super.sheetName(sheetName);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> headerStyles(ExcelStyleConfig... configs) {
        super.headerStyles(configs);

        if (this.headerStyles.length != 1 && this.headerStyles.length != this.fields.size()) {
            throw new IllegalArgumentException(String.format(
                    "Number of header styles(%d) must be 1 or equal to number of targeted fields(%d) in the class '%s'",
                    this.headerStyles.length, this.fields.size(), this.type.getName()));
        }

        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> bodyStyles(ExcelStyleConfig... configs) {
        super.bodyStyles(configs);

        if (this.bodyStyles.length != 1 && this.bodyStyles.length != this.fields.size()) {
            throw new IllegalArgumentException(String.format(
                    "Number of body styles(%d) must be 1 or equal to number of targeted fields(%d) in the class '%s'",
                    this.bodyStyles.length, this.fields.size(), this.type.getName()));
        }

        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> disableRolling() {
        super.disableRolling();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> autoResizeCols() {
        super.autoResizeCols();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> hideExtraRows() {
        super.hideExtraRows();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> hideExtraCols() {
        super.hideExtraCols();
        return this;
    }

    //////////////////////////////////////// Hooks ////////////////////////////////////////

    /**
     * {@inheritDoc}
     *
     * <p> If the header names are not set through {@link ExcelWriter#headerNames},
     * this method brings the values from {@link ExcelColumn#name()}.
     *
     * @see FieldUtils#toHeaderNames(List)
     */
    @Override
    protected void ifHeaderNamesAreEmpty(List<String> headerNames) {
        headerNames.addAll(FieldUtils.toHeaderNames(this.fields));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeToSheet(Sheet sheet, List<T> list) {
        final int numOfModels = list.size();
        final int numOfFields = this.fields.size();

        for (int i = 0; i < numOfModels; i++) {
            T model = list.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < numOfFields; j++) {
                Field field = this.fields.get(j);
                Cell cell = row.createCell(j);

                // Converts field value to the string.
                String value;
                ExcelWriterExpression annotation = field.getAnnotation(ExcelWriterExpression.class);
                if (annotation == null) {
                    // When the field is not annotated with @ExcelWriterExpression.
                    value = this.basicConverter.convert(model, field);
                } else {
                    // When the field is annotated with @ExcelWriterExpression.
                    Map<String, Object> simulatedModel = FieldUtils.toMap(model, this.fields);
                    this.expConverter.setVariables(simulatedModel);
                    value = this.expConverter.convert(model, field);
                }

                if (value != null) cell.setCellValue(value);

                if (this.bodyStyles == null) continue;

                // Sets styles to body's cell.
                CellStyle bodyStyle = this.bodyStyles.length == 1
                        ? this.bodyStyles[0] : this.bodyStyles[j];
                cell.setCellStyle(bodyStyle);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNumOfColumns() {
        return this.fields.size();
    }

}
