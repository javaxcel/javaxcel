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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.github.javaxcel.core.validator.ExcelColumnValidator;
import com.github.javaxcel.styler.ExcelStyleConfig;

/**
 * First-class abstraction for a single Excel column.
 *
 * <p>Encapsulates everything an Excel reader/writer needs to know about one column:
 * how to read a value from a row map, how to write a value from a model, optional
 * styles, validators, and dropdown items. The {@code DefaultExcelReader} and
 * {@code DefaultExcelWriter} engines drive the sheet by iterating over a list of
 * descriptors, eliminating the Model-vs-Map polymorphism axis from the engines.
 *
 * <p>This is an internal abstraction. The public extension point may be promoted
 * in a future major release.
 *
 * @param <T> type of model this column belongs to
 * @since 0.x
 */
public interface ColumnDescriptor<T> {

    /**
     * Header name shown on the first row of the sheet.
     */
    String name();

    /**
     * Reads one column value from the given row.
     *
     * @param row map keyed by header name to raw cell value
     * @return converted value for this column
     */
    @Nullable
    Object readValue(Map<String, String> row);

    /**
     * Produces the cell value for this column from the given model.
     *
     * @param model source model
     * @return stringified cell value, or {@code null} when the cell should be empty
     */
    @Nullable
    String writeValue(T model);

    /**
     * Style for the header cell of this column. Empty when no per-column override.
     */
    default Optional<ExcelStyleConfig> headerStyle() {
        return Optional.empty();
    }

    /**
     * Style for the body cells of this column. Empty when no per-column override.
     */
    default Optional<ExcelStyleConfig> bodyStyle() {
        return Optional.empty();
    }

    /**
     * Validators run against the raw cell value during reads.
     */
    default List<ExcelColumnValidator> validators() {
        return Collections.emptyList();
    }

    /**
     * Dropdown items to constrain the column. Empty when not applicable.
     */
    default Optional<String[]> dropdownItems() {
        return Optional.empty();
    }

    /**
     * Storage key used by the model assembler to look up the converted value
     * inside its intermediate mock map. For {@code FieldColumnDescriptor} this is
     * the underlying field name; for {@code MapKeyColumnDescriptor} this is the
     * map key. Defaults to {@link #name()}.
     */
    default String fieldKey() {
        return name();
    }

    /**
     * Whether the underlying field is {@code final} and must not be reassigned
     * after construction. Used by the model assembler to skip reflection-based
     * field assignment for final fields.
     */
    default boolean isFinalField() {
        return false;
    }

}
