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

package com.github.javaxcel.constant;

import com.github.javaxcel.annotation.ExcelReaderExpression;
import com.github.javaxcel.annotation.ExcelWriterExpression;
import com.github.javaxcel.converter.in.DefaultInputConverter;
import com.github.javaxcel.converter.in.ExpressionInputConverter;
import com.github.javaxcel.converter.out.DefaultOutputConverter;
import com.github.javaxcel.converter.out.ExpressionOutputConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Type of conversion
 */
public enum ConversionType {

    /**
     * @see DefaultOutputConverter
     * @see DefaultInputConverter
     */
    DEFAULT,

    /**
     * @see ExpressionOutputConverter
     * @see ExpressionInputConverter
     */
    EXPRESSION;

    public static ConversionType of(Field field, ConverterType converterType) {
        Annotation annotation;
        switch (converterType) {
            case IN:
                annotation = field.getAnnotation(ExcelReaderExpression.class);
                break;
            case OUT:
                annotation = field.getAnnotation(ExcelWriterExpression.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown converter type: " + converterType);
        }

        return annotation == null ? DEFAULT : EXPRESSION;
    }

}
