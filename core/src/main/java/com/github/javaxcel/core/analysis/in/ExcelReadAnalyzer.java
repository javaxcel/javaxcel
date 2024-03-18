/*
 * Copyright 2022 Javaxcel
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

package com.github.javaxcel.core.analysis.in;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.javaxcel.core.analysis.AbstractExcelAnalyzer;
import com.github.javaxcel.core.analysis.ExcelAnalysis.DefaultMeta;
import com.github.javaxcel.core.analysis.ExcelAnalysis.DefaultMeta.Source;
import com.github.javaxcel.core.analysis.ExcelAnalysisImpl.DefaultMetaImpl;
import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelReadExpression;
import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.converter.in.ExcelReadExpressionConverter;
import com.github.javaxcel.core.converter.in.ExcelReadHandlerConverter;
import com.github.javaxcel.core.exception.ExcelColumnValidationException;
import com.github.javaxcel.core.in.strategy.impl.UseSetters;
import com.github.javaxcel.core.util.FieldUtils;
import com.github.javaxcel.core.util.ObjectUtils;
import com.github.javaxcel.core.validator.ExcelColumnValidator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import io.github.imsejin.common.util.ArrayUtils;
import io.github.imsejin.common.util.ReflectionUtils;
import io.github.imsejin.common.util.StringUtils;

import static java.util.stream.Collectors.*;

/**
 * Analyzer for reading Excel
 *
 * @since 0.9.0
 */
public class ExcelReadAnalyzer extends AbstractExcelAnalyzer {

    /**
     * Flag which indicates that the field should be handled by {@link ExcelReadHandlerConverter}.
     */
    public static final int HANDLER = 0x01;

    /**
     * Flag which indicates that the field should be handled by {@link ExcelReadExpressionConverter}.
     */
    public static final int EXPRESSION = 0x02;

    /**
     * Flag which indicates that value of the field should be set through access to field.
     */
    public static final int FIELD_ACCESS = 0x04;

    /**
     * Flag which indicates that value of the field should be set through setter for the field.
     */
    public static final int SETTER = 0x08;

    /**
     * Instantiates a new analyzer for reading Excel.
     *
     * @param registry registry of handlers
     */
    public ExcelReadAnalyzer(ExcelTypeHandlerRegistry registry) {
        super(registry);
    }

    @Override
    protected DefaultMeta analyzeDefaultMeta(Field field, Object[] arguments) {
        // ExcelReader supports only @ExcelColumn.defaultValue.
        ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
        if (columnAnnotation != null && !columnAnnotation.defaultValue().isEmpty()) {
            String value = columnAnnotation.defaultValue();
            return new DefaultMetaImpl(value, Source.COLUMN);
        }

        return DefaultMetaImpl.EMPTY;
    }

    @Override
    protected int analyzeFlags(Field field, Object[] arguments) {
        UseSetters us = ObjectUtils.resolveFirst(UseSetters.class, arguments);

        int flags = 0x00;
        flags |= field.isAnnotationPresent(ExcelReadExpression.class) ? EXPRESSION : HANDLER;
        flags |= us == null ? FIELD_ACCESS : SETTER;

        // Checks if getter of the field exists.
        if ((flags & SETTER) == SETTER) {
            try {
                FieldUtils.resolveSetter(field);
            } catch (RuntimeException ignored) {
                // When it doesn't exist, removes flag of SETTER from the flags.
                flags = flags ^ SETTER;
                flags |= FIELD_ACCESS;
            }
        }

        return flags;
    }

    @Override
    protected List<ExcelColumnValidator> analyzeValidators(Field field, Object[] arguments) {
        ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
        if (columnAnnotation != null && !ArrayUtils.isNullOrEmpty(columnAnnotation.validations())) {
            return Stream.of(columnAnnotation.validations())
                    .flatMap(validation -> {
                        Stream<ExcelColumnValidator> s = Stream.of(validation.validators())
                                .map(ReflectionUtils::instantiate);

                        String regexp = validation.regexp();
                        if (StringUtils.isNullOrEmpty(regexp)) {
                            return s;
                        }

                        Pattern pattern = Pattern.compile(regexp, validation.flags());
                        ExcelColumnRegExpValidator validator = new ExcelColumnRegExpValidator(pattern);

                        // Puts an ExcelColumnRegExpValidator first,
                        // because it is intended to provide a simple validator.
                        return Stream.concat(Stream.of(validator), s);
                    })
                    .collect(toList());
        }

        return Collections.emptyList();
    }

    // -------------------------------------------------------------------------------------------------

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ExcelColumnRegExpValidator implements ExcelColumnValidator {
        @NotNull
        private final Pattern pattern;

        @Override
        public void validate(@Nullable String cellValue) {
            if (cellValue == null || !this.pattern.matcher(cellValue).matches()) {
                throw new ExcelColumnValidationException(
                        "Invalid cell value for regular expression[%s]: %s", this.pattern.pattern(), cellValue);
            }
        }
    }

}
