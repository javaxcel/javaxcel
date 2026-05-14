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

import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;

import com.github.javaxcel.core.internal.util.ObjectUtils;

/**
 * {@link ColumnDescriptor} backed by a single key in a {@code Map}-based payload.
 *
 * <p>Reads return the raw cell value as {@code String}; writes look up the value
 * for the column's key in the model {@code Map} and stringify it (with optional
 * default fallback for null/empty values).
 *
 * <p>The {@link #name()} may differ from the underlying {@link #fieldKey()} when
 * a {@code KeyNames} strategy renames the column for display purposes.
 *
 * @since 0.x
 */
public final class MapKeyColumnDescriptor implements ColumnDescriptor<Map<String, Object>> {

    private final String key;

    private final String displayName;

    private final @Nullable String defaultValue;

    public MapKeyColumnDescriptor(String key, String displayName, @Nullable String defaultValue) {
        Asserts.that(key)
                .describedAs("MapKeyColumnDescriptor.key is not allowed to be null or empty")
                .isNotNull()
                .isNotEmpty();
        Asserts.that(displayName)
                .describedAs("MapKeyColumnDescriptor.displayName is not allowed to be null or empty")
                .isNotNull()
                .isNotEmpty();

        this.key = key;
        this.displayName = displayName;
        this.defaultValue = defaultValue;
    }

    @Override
    public String name() {
        return this.displayName;
    }

    @Override
    public String fieldKey() {
        return this.key;
    }

    @Nullable
    @Override
    public Object readValue(Map<String, String> row) {
        return row.get(this.key);
    }

    @Nullable
    @Override
    public String writeValue(Map<String, Object> model) {
        Object value = model.get(this.key);
        if (!ObjectUtils.isNullOrEmptyCharSequence(value)) {
            return value.toString();
        }
        if (!StringUtils.isNullOrEmpty(this.defaultValue)) {
            return this.defaultValue;
        }
        return null;
    }

}
