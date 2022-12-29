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

package com.github.javaxcel.core.converter.out;

import com.github.javaxcel.core.analysis.ExcelAnalysis;
import com.github.javaxcel.core.analysis.out.ExcelWriteAnalyzer;
import com.github.javaxcel.core.annotation.ExcelWriteExpression;
import com.github.javaxcel.core.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ReflectionUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Converter for writing Excel with expression(SpEL)
 *
 * @since 0.4.0
 */
public class ExcelWriteExpressionConverter implements ExcelWriteConverter {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final List<Field> fields;

    private final Map<Field, Method> getterMap;

    private final Map<Field, Cache> cacheMap;

    public ExcelWriteExpressionConverter(Iterable<ExcelAnalysis> analyses) {
        Asserts.that(analyses)
                .describedAs("ExcelWriteExpressionConverter.analyses is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelWriteExpressionConverter.analyses is not allowed to be empty")
                .is(them -> them.iterator().hasNext());

        List<Field> fields = new ArrayList<>();
        Map<Field, Method> getterMap = new HashMap<>();
        Map<Field, Cache> cacheMap = new HashMap<>();

        for (ExcelAnalysis analysis : analyses) {
            Field field = analysis.getField();

            // Caches an expression for the field.
            Cache cache = new Cache(analysis);
            if (analysis.hasFlag(ExcelWriteAnalyzer.EXPRESSION)) {
                // DO NOT CHECK IF @ExcelWriteExpression.value IS EMPTY STRING,
                // BECAUSE THE ANNOTATION HAS ONLY ONE MANDATORY ATTRIBUTE AND
                // IT MEANS THAT MUST BE A VALID VALUE IF THE ANNOTATION IS ON A FIELD.
                // THIS CLASS IS RESPONSIBLE FOR INFORMING USER OF FAILURE OF PARSING EXPRESSION.
                ExcelWriteExpression annotation = field.getAnnotation(ExcelWriteExpression.class);
                Expression expression = EXPRESSION_PARSER.parseExpression(annotation.value());
                cache.setExpression(expression);
            }

            // Caches a getter for the field.
            if (analysis.hasFlag(ExcelWriteAnalyzer.GETTER)) {
                Method getter = FieldUtils.resolveGetter(field);
                getterMap.put(field, getter);
            }

            fields.add(field);
            cacheMap.put(field, cache);
        }

        this.fields = Collections.unmodifiableList(fields);
        this.getterMap = Collections.unmodifiableMap(getterMap);
        this.cacheMap = Collections.unmodifiableMap(cacheMap);
    }

    @Override
    public boolean supports(Field field) {
        ExcelAnalysis analysis = this.cacheMap.get(field).getAnalysis();
        return analysis.hasFlag(ExcelWriteAnalyzer.EXPRESSION);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String convert(Object model, Field field) {
        // Don't set root object to prevent user from assigning value
        // to the field of model with the way we don't intend.
        EvaluationContext context = new StandardEvaluationContext();

        // Enables to use value of the field as "#FIELD_NAME" in @ExcelWriteExpression.
        Map<String, Object> variables = getVariablesOf(model, field);
        variables.forEach(context::setVariable);

        Cache cache = this.cacheMap.get(field);
        Object value = Objects.requireNonNull(cache.getExpression(), "Never throw").getValue(context);

        // Returns the result of expression.
        if (!isNullOrEmptyString(value)) {
            // Forces the evaluated value to be a string
            // even if desired return type of expression is not String.
            return value.toString();
        }

        String defaultValue = cache.getAnalysis().getDefaultMeta().getValue();

        // Returns null if the converted value is null or empty string.
        if (isNullOrEmptyString(defaultValue)) {
            return null;
        }

        return defaultValue;
    }

    // -------------------------------------------------------------------------------------------------

    private Map<String, Object> getVariablesOf(Object model, Field field) {
        ExcelAnalysis analysis = this.cacheMap.get(field).analysis;

        if (analysis.hasFlag(ExcelWriteAnalyzer.FIELD_ACCESS)) {
            return FieldUtils.toMap(model, this.fields);

        } else if (analysis.hasFlag(ExcelWriteAnalyzer.GETTER)) {
            Map<String, Object> variables = new HashMap<>();
            for (Entry<Field, Method> entry : this.getterMap.entrySet()) {
                Method getter = entry.getValue();
                Object value = ReflectionUtils.invoke(getter, model);

                variables.put(entry.getKey().getName(), value);
            }

            return variables;

        } else {
            throw new AssertionError("Never throw; ExcelWriteAnalyzer adds the flags into each analysis");
        }
    }

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
