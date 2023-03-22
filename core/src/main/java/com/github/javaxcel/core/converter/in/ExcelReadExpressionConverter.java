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

package com.github.javaxcel.core.converter.in;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;

import com.github.javaxcel.core.analysis.ExcelAnalysis;
import com.github.javaxcel.core.analysis.in.ExcelReadAnalyzer;
import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelReadExpression;

/**
 * Converter for reading Excel with expression(SpEL)
 *
 * @since 0.4.0
 */
public class ExcelReadExpressionConverter implements ExcelReadConverter {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final Map<Field, Cache> cacheMap;

    public ExcelReadExpressionConverter(Iterable<ExcelAnalysis> analyses) {
        Asserts.that(analyses)
                .describedAs("ExcelReadExpressionConverter.analyses is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadExpressionConverter.analyses is not allowed to be empty")
                .is(them -> them.iterator().hasNext());

        Map<Field, Cache> cacheMap = new HashMap<>();

        for (ExcelAnalysis analysis : analyses) {
            Field field = analysis.getField();

            // Makes instance of expression a cache.
            Cache cache = new Cache(analysis);
            if (analysis.hasFlag(ExcelReadAnalyzer.EXPRESSION)) {
                // DO NOT CHECK IF @ExcelReadExpression.value IS EMPTY STRING,
                // BECAUSE THE ANNOTATION HAS ONLY ONE MANDATORY ATTRIBUTE AND
                // IT MEANS THAT MUST BE A VALID VALUE IF THE ANNOTATION IS ON A FIELD.
                // THIS CLASS IS RESPONSIBLE FOR INFORMING USER OF FAILURE OF PARSING EXPRESSION.
                ExcelReadExpression annotation = field.getAnnotation(ExcelReadExpression.class);
                Expression expression = EXPRESSION_PARSER.parseExpression(annotation.value());
                cache.setExpression(expression);
            }

            cacheMap.put(field, cache);
        }

        this.cacheMap = Collections.unmodifiableMap(cacheMap);
    }

    @Override
    public boolean supports(Field field) {
        ExcelAnalysis analysis = this.cacheMap.get(field).analysis;
        return analysis.hasFlag(ExcelReadAnalyzer.EXPRESSION);
    }

    /**
     * {@inheritDoc}
     *
     * @see ExcelReadExpression#value()
     * @see ExcelColumn#defaultValue()
     */
    @Nullable
    @Override
    public Object convert(Map<String, String> variables, Field field) {
        Object value = convertInternal(variables, field);

        // Returns the result of expression.
        if (!isNullOrEmptyString(value)) {
            return value;
        }

        // If a result of expression is empty string, replaces it with null.
        Cache cache = this.cacheMap.get(field);
        String defaultValue = cache.getAnalysis().getDefaultMeta().getValue();

        // Returns null if the default value is not specified.
        if (StringUtils.isNullOrEmpty(defaultValue)) {
            return null;
        }

        // Replaces the value with the default value from variables.
        Map<String, String> newVariables = new HashMap<>(variables);
        newVariables.put(field.getName(), defaultValue);

        // Converts the default value through expression again.
        value = convertInternal(newVariables, field);

        // Returns null if the value converted by default is also null or empty string.
        if (isNullOrEmptyString(value)) {
            return null;
        }

        // Returns the result of expression.
        return value;
    }

    private Object convertInternal(Map<String, String> variables, Field field) {
        // To read in parallel, instantiates on each call of this method.
        // Don't set root object to prevent user from assigning value
        // to the field of model with the way we don't intend.
        EvaluationContext context = new StandardEvaluationContext();

        // Enables to use value of the field as "#FIELD_NAME" in @ExcelReadExpression.
        variables.forEach(context::setVariable);

        Cache cache = this.cacheMap.get(field);
        return Objects.requireNonNull(cache.getExpression(), "Never throw").getValue(context, field.getType());
    }

    // -------------------------------------------------------------------------------------------------

    private static boolean isNullOrEmptyString(@Nullable Object object) {
        if (object == null) {
            return true;
        }

        if (object instanceof CharSequence) {
            return ((CharSequence) object).length() == 0;
        }

        return false;
    }

    private static class Cache {
        private final ExcelAnalysis analysis;
        private Expression expression;

        private Cache(ExcelAnalysis analysis) {
            this.analysis = Objects.requireNonNull(analysis,
                    () -> getClass().getSimpleName() + ".analysis cannot be null");
        }

        public ExcelAnalysis getAnalysis() {
            return this.analysis;
        }

        @Nullable
        public Expression getExpression() {
            return this.expression;
        }

        public void setExpression(Expression expression) {
            this.expression = Objects.requireNonNull(expression,
                    () -> getClass().getSimpleName() + ".expression cannot be null");
        }
    }

}
