/*
 * Copyright 2026 Javaxcel
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

package com.github.javaxcel.core.internal.assembler;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.imsejin.common.assertion.Asserts;

import com.github.javaxcel.core.in.processor.ExcelModelCreationProcessor;
import com.github.javaxcel.core.in.resolver.AbstractExcelModelExecutableResolver;
import com.github.javaxcel.core.internal.analysis.ExcelAnalysis;
import com.github.javaxcel.core.internal.descriptor.ColumnDescriptor;
import com.github.javaxcel.core.internal.descriptor.FieldColumnDescriptor;
import com.github.javaxcel.core.validator.ExcelColumnValidator;

/**
 * Assembles a model {@code T} from a row map by invoking each column descriptor's
 * read converter and feeding the resulting "mock" map to
 * {@link ExcelModelCreationProcessor}.
 *
 * <p>Replaces {@code ModelReader.toActualModel(Map)} from the previous architecture.
 * The model creation processor (constructor + setter resolution) is reused as-is.
 *
 * @param <T> type of model
 * @since 0.x
 */
public final class ModelAssembler<T> {

    private final ExcelModelCreationProcessor<T> processor;

    public ModelAssembler(Class<T> modelType, List<? extends ColumnDescriptor<T>> columns) {
        Asserts.that(modelType)
                .describedAs("ModelAssembler.modelType is not allowed to be null")
                .isNotNull();
        Asserts.that(columns)
                .describedAs("ModelAssembler.columns is not allowed to be null or empty")
                .isNotNull()
                .isNotEmpty();

        List<Field> fields = new ArrayList<>(columns.size());
        List<ExcelAnalysis> analyses = new ArrayList<>(columns.size());
        for (ColumnDescriptor<T> column : columns) {
            if (!(column instanceof FieldColumnDescriptor<T> fieldColumn)) {
                throw new IllegalArgumentException(
                        "ModelAssembler requires FieldColumnDescriptor, got: " + column.getClass().getName());
            }
            ExcelAnalysis analysis = fieldColumn.getAnalysis();
            Asserts.that(analysis)
                    .describedAs("FieldColumnDescriptor[{0}] must carry analysis for ModelAssembler",
                            fieldColumn.getField().getName())
                    .isNotNull();
            fields.add(fieldColumn.getField());
            analyses.add(analysis);
        }

        Executable executable = AbstractExcelModelExecutableResolver.resolve(modelType);
        ExcelModelCreationProcessor<T> processor = new ExcelModelCreationProcessor<>(modelType, fields, executable);
        processor.setAnalyses(analyses);
        this.processor = processor;
    }

    /**
     * Reads each column from the row, runs validators against the raw cell value,
     * and creates the model via the underlying processor.
     */
    public T assemble(Map<String, String> row, List<? extends ColumnDescriptor<T>> columns) {
        Map<String, Object> mock = new HashMap<>();
        for (ColumnDescriptor<T> column : columns) {
            String key = column.fieldKey();
            String rawValue = row.get(key);

            for (ExcelColumnValidator validator : column.validators()) {
                validator.validate(rawValue);
            }

            mock.put(key, column.readValue(row));
        }

        return this.processor.createModel(mock);
    }

}
