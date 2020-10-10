package com.github.javaxcel.converter.impl;

import com.github.javaxcel.annotation.ExcelReaderExpression;
import com.github.javaxcel.converter.ReadingConverter;
import io.github.imsejin.expression.ExpressionParser;
import io.github.imsejin.expression.spel.standard.SpelExpressionParser;
import io.github.imsejin.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.Map;

public class ExpressiveReadingConverter<T> implements ReadingConverter<T> {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private final StandardEvaluationContext context = new StandardEvaluationContext();

    private final Class<T> type;

    private Map<String, Object> variables;

    public ExpressiveReadingConverter(Class<T> type) {
        this.type = type;
    }

    /**
     * Sets up the variables.
     *
     * @param variables {@link Map} in which key is the model's field name and value is the model's field value
     */
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    /**
     * Parses a expression to be assigned as field value.
     *
     * @param value value
     * @param field targeted field
     * @return converted model
     */
    @Override
    public Object convert(String value, Field field) {
        ExcelReaderExpression annotation = field.getAnnotation(ExcelReaderExpression.class);
        context.setVariables(this.variables);

        return parser.parseExpression(annotation.value()).getValue(context, field.getType());
    }

}
