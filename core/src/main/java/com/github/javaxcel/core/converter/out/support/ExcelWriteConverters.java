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

package com.github.javaxcel.core.converter.out.support;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

import com.github.javaxcel.core.analysis.ExcelAnalysis;
import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.converter.out.ExcelWriteConverter;
import com.github.javaxcel.core.converter.out.ExcelWriteExpressionConverter;
import com.github.javaxcel.core.converter.out.ExcelWriteHandlerConverter;

/**
 * Container of converters for writing Excel as candidates
 *
 * <p> This container has converters as candidates. When it takes the parameters to convert,
 * it chooses a candidate that can handle the parameters.
 *
 * @since 0.7.0
 */
public final class ExcelWriteConverters implements ExcelWriteConverter {

    private final List<ExcelWriteConverter> candidates;

    public ExcelWriteConverters(Iterable<ExcelAnalysis> analyses, ExcelTypeHandlerRegistry registry) {
        List<ExcelWriteConverter> converters = new ArrayList<>();

        converters.add(new ExcelWriteHandlerConverter(analyses, registry));
        converters.add(new ExcelWriteExpressionConverter(analyses));

        this.candidates = Collections.unmodifiableList(converters);
    }

    @Override
    @ExcludeFromGeneratedJacocoReport
    public boolean supports(Field field) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String convert(Object model, Field field) {
        for (ExcelWriteConverter converter : this.candidates) {
            if (converter.supports(field)) {
                return converter.convert(model, field);
            }
        }

        throw new AssertionError("Never throw");
    }

}
