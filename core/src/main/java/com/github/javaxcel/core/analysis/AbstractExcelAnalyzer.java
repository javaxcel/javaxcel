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

package com.github.javaxcel.core.analysis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.imsejin.common.assertion.Asserts;

import com.github.javaxcel.core.analysis.ExcelAnalysis.DefaultMeta;
import com.github.javaxcel.core.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.converter.in.support.FieldTypeResolver;

/**
 * Abstract analyzer for preparing the fields to handle for Excel
 *
 * @since 0.9.0
 */
public abstract class AbstractExcelAnalyzer implements ExcelAnalyzer {

    private final ExcelTypeHandlerRegistry registry;

    /**
     * Instantiates a new analyzer for Excel.
     *
     * @param registry registry of handlers
     */
    protected AbstractExcelAnalyzer(ExcelTypeHandlerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public final List<ExcelAnalysis> analyze(List<Field> fields, Object... arguments) {
        Asserts.that(fields)
                .describedAs("ExcelAnalyzer cannot analyze null as fields")
                .isNotNull()
                .describedAs("ExcelAnalyzer cannot analyze empty fields")
                .isNotEmpty();

        List<ExcelAnalysis> analyses = new ArrayList<>();
        for (Field field : fields) {
            ExcelAnalysisImpl analysis = new ExcelAnalysisImpl(field);

            // Analyzes default meta information for the field.
            DefaultMeta defaultMeta = analyzeDefaultMeta(field, arguments);
            analysis.setDefaultMeta(defaultMeta);

            // Analyzes handler for the field.
            Class<?> concreteType = FieldTypeResolver.resolveConcreteType(field);
            ExcelTypeHandler<?> handler = this.registry.getHandler(concreteType);
            if (handler != null) {
                analysis.setHandler(handler);
            }

            // Analyzes flags for the field.
            int flags = analyzeFlags(field, arguments);
            analysis.addFlags(flags);

            analyses.add(analysis);
        }

        return Collections.unmodifiableList(analyses);
    }

    // -------------------------------------------------------------------------------------------------

    /**
     * Analyzes the field and returns default meta information of it.
     *
     * @param field     targeted field
     * @param arguments optional arguments
     * @return default meta information
     */
    protected abstract DefaultMeta analyzeDefaultMeta(Field field, Object[] arguments);

    /**
     * Analyzes the fields and returns flags for it.
     *
     * @param field     targeted field
     * @param arguments optional arguments
     * @return flags
     */
    protected abstract int analyzeFlags(Field field, Object[] arguments);

}
