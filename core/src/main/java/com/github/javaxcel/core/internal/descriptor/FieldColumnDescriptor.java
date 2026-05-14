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

package com.github.javaxcel.core.internal.descriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;
import lombok.Getter;

import com.github.javaxcel.core.internal.analysis.ExcelAnalysis;
import com.github.javaxcel.core.internal.converter.in.ExcelReadConverter;
import com.github.javaxcel.core.internal.converter.out.ExcelWriteConverter;
import com.github.javaxcel.core.validator.ExcelColumnValidator;
import com.github.javaxcel.styler.ExcelStyleConfig;

/**
 * {@link ColumnDescriptor} backed by a {@link Field} on a model class.
 *
 * <p>Either of the converters may be {@code null} when the descriptor is built
 * for a single direction (read-only or write-only). Calling the unsupported
 * direction throws {@link UnsupportedOperationException}.
 *
 * @param <T> type of the owning model
 * @since 0.x
 */
public final class FieldColumnDescriptor<T> implements ColumnDescriptor<T> {

    @Getter
    private final Field field;

    private final String name;

    private final @Nullable ExcelReadConverter readConverter;

    private final @Nullable ExcelWriteConverter writeConverter;

    private final @Nullable ExcelStyleConfig headerStyle;

    private final @Nullable ExcelStyleConfig bodyStyle;

    private final List<ExcelColumnValidator> validators;

    private final String @Nullable [] dropdownItems;

    private final boolean isFinal;

    /**
     * The {@link ExcelAnalysis} this descriptor was built from, or {@code null}
     * when not retained. Used by {@code ModelAssembler} to feed the underlying
     * {@code ExcelModelCreationProcessor}.
     */
    @Getter
    private final @Nullable ExcelAnalysis analysis;

    public FieldColumnDescriptor(
            Field field,
            String name,
            @Nullable ExcelReadConverter readConverter,
            @Nullable ExcelWriteConverter writeConverter,
            @Nullable ExcelStyleConfig headerStyle,
            @Nullable ExcelStyleConfig bodyStyle,
            @Nullable List<ExcelColumnValidator> validators,
            String @Nullable [] dropdownItems,
            @Nullable ExcelAnalysis analysis) {
        Asserts.that(field)
                .describedAs("FieldColumnDescriptor.field is not allowed to be null")
                .isNotNull();
        Asserts.that(name)
                .describedAs("FieldColumnDescriptor.name is not allowed to be null or empty")
                .isNotNull()
                .isNotEmpty();

        this.field = field;
        this.name = name;
        this.readConverter = readConverter;
        this.writeConverter = writeConverter;
        this.headerStyle = headerStyle;
        this.bodyStyle = bodyStyle;
        this.validators = validators == null ? Collections.emptyList() : List.copyOf(validators);
        this.dropdownItems = dropdownItems;
        this.isFinal = Modifier.isFinal(field.getModifiers());
        this.analysis = analysis;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String fieldKey() {
        return this.field.getName();
    }

    @Override
    public boolean isFinalField() {
        return this.isFinal;
    }

    @Nullable
    @Override
    public Object readValue(Map<String, String> row) {
        if (this.readConverter == null) {
            throw new UnsupportedOperationException(
                    "FieldColumnDescriptor[" + this.field.getName() + "] is not configured for reading");
        }
        return this.readConverter.convert(row, this.field);
    }

    @Nullable
    @Override
    public String writeValue(T model) {
        if (this.writeConverter == null) {
            throw new UnsupportedOperationException(
                    "FieldColumnDescriptor[" + this.field.getName() + "] is not configured for writing");
        }
        return this.writeConverter.convert(model, this.field);
    }

    @Override
    public Optional<ExcelStyleConfig> headerStyle() {
        return Optional.ofNullable(this.headerStyle);
    }

    @Override
    public Optional<ExcelStyleConfig> bodyStyle() {
        return Optional.ofNullable(this.bodyStyle);
    }

    @Override
    public List<ExcelColumnValidator> validators() {
        return this.validators;
    }

    @Override
    public Optional<String[]> dropdownItems() {
        return Optional.ofNullable(this.dropdownItems);
    }

}
