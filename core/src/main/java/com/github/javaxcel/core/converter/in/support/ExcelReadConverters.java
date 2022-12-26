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

package com.github.javaxcel.core.converter.in.support;

import com.github.javaxcel.core.analysis.ExcelAnalysis;
import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.converter.in.ExcelReadConverter;
import com.github.javaxcel.core.converter.in.ExcelReadExpressionConverter;
import com.github.javaxcel.core.converter.in.ExcelReadHandlerConverter;
import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Container of converters for reading Excel as candidates
 *
 * <p> This container has converters as candidates. When it takes the parameters to convert,
 * it chooses a candidate that can handle the parameters.
 *
 * @since 0.7.1
 */
public final class ExcelReadConverters implements ExcelReadConverter {

    private final List<ExcelReadConverter> candidates;

    public ExcelReadConverters(Iterable<ExcelAnalysis> analyses, ExcelTypeHandlerRegistry registry) {
        List<ExcelReadConverter> converters = new ArrayList<>();

        converters.add(new ExcelReadHandlerConverter(analyses, registry));
        converters.add(new ExcelReadExpressionConverter(analyses));

        this.candidates = Collections.unmodifiableList(converters);
    }

    @Override
    @ExcludeFromGeneratedJacocoReport
    public boolean supports(Field field) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object convert(Map<String, String> variables, Field field) {
        for (ExcelReadConverter converter : this.candidates) {
            if (converter.supports(field)) {
                return converter.convert(variables, field);
            }
        }

        throw new AssertionError("Never throw");
    }

}
