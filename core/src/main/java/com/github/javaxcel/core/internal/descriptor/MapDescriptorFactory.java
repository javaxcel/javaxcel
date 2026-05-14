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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;

import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;
import com.github.javaxcel.core.out.strategy.impl.DefaultValue;
import com.github.javaxcel.core.out.strategy.impl.KeyNames;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

/**
 * Builds {@link ColumnDescriptor} lists for Map-backed payloads.
 *
 * <p>Replaces the inline logic from {@code MapWriter.setKeys} +
 * {@code MapWriter.changeKeys} + {@code MapWriter.setDefaultValue}.
 *
 * @since 0.x
 */
public final class MapDescriptorFactory {

    private MapDescriptorFactory() {
    }

    /**
     * Builds descriptors for writing a list of maps. Keys are collected from the
     * union of all maps' key sets in encounter order. The {@link KeyNames}
     * strategy reorders the columns and optionally renames them; the
     * {@link DefaultValue} strategy supplies a fallback for null/empty cells.
     */
    @SuppressWarnings("unchecked")
    public static List<ColumnDescriptor<Map<String, Object>>> forWrite(
            List<? extends Map<String, ?>> list,
            Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategies) {
        Asserts.that(list)
                .describedAs("MapDescriptorFactory.list is not allowed to be null")
                .isNotNull();
        Asserts.that(strategies)
                .describedAs("MapDescriptorFactory.strategies is not allowed to be null")
                .isNotNull();

        List<String> keys = list.stream()
                .flatMap(it -> it.keySet().stream())
                .distinct()
                .collect(toList());

        Asserts.that(keys)
                .describedAs("MapDescriptorFactory.keys is not allowed to be empty")
                .isNotEmpty()
                .describedAs("MapDescriptorFactory.keys cannot have null or blank element: {0}", keys)
                .noneMatch(StringUtils::isNullOrBlank)
                .describedAs("MapDescriptorFactory.keys cannot have duplicated elements: {0}", keys)
                .doesNotHaveDuplicates();

        String defaultValue = resolveDefaultValue(strategies);
        Map<String, Integer> orders = null;
        List<String> displayNames = null;

        ExcelWriteStrategy keyNamesStrategy = strategies.get(KeyNames.class);
        if (keyNamesStrategy != null) {
            Map<String, Object> keyMap = (Map<String, Object>) keyNamesStrategy.execute(/* unused */ null);
            orders = (Map<String, Integer>) keyMap.get("orders");

            Asserts.that(keys)
                    .describedAs(
                            "KeyNames.orders.size must equal keys.size (keys: {0}, orders: {1})",
                            keys.size(), orders.size())
                    .hasSize(orders.size())
                    .describedAs(
                            "KeyNames.orders.keySet must contain only existing keys (keys: {0}, orderKeys: {1})",
                            keys, orders.keySet())
                    .containsOnly(orders.keySet().toArray(new String[0]));

            keys = new ArrayList<>(keys);
            keys.sort(comparing(orders::get));

            if (keyMap.containsKey("names")) {
                displayNames = (List<String>) keyMap.get("names");
            }
        }

        List<ColumnDescriptor<Map<String, Object>>> descriptors = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String displayName = displayNames != null ? displayNames.get(i) : key;
            descriptors.add(new MapKeyColumnDescriptor(key, displayName, defaultValue));
        }
        return Collections.unmodifiableList(descriptors);
    }

    /**
     * Builds descriptors for reading a Map sheet given a list of header names
     * (typically derived from the sheet's first row, or overridden by the
     * {@code in.strategy.impl.KeyNames} strategy).
     */
    public static List<ColumnDescriptor<Map<String, String>>> forRead(List<String> headerNames) {
        Asserts.that(headerNames)
                .describedAs("MapDescriptorFactory.headerNames is not allowed to be null or empty")
                .isNotNull()
                .isNotEmpty();
        // headerNames are the keys for both display and lookup in read mode.
        List<ColumnDescriptor<Map<String, String>>> descriptors = new ArrayList<>(headerNames.size());
        for (String headerName : headerNames) {
            descriptors.add(new ReadOnlyMapKeyColumnDescriptor(headerName));
        }
        return Collections.unmodifiableList(descriptors);
    }

    @Nullable
    private static String resolveDefaultValue(
            Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategies) {
        ExcelWriteStrategy strategy = strategies.get(DefaultValue.class);
        return strategy == null ? null : (String) strategy.execute(/* unused */ null);
    }

    /**
     * Read-only descriptor specialized for Map-mode reads where no value
     * conversion is needed beyond returning the raw row value as {@code String}.
     */
    private static final class ReadOnlyMapKeyColumnDescriptor implements ColumnDescriptor<Map<String, String>> {

        private final String key;

        ReadOnlyMapKeyColumnDescriptor(String key) {
            this.key = key;
        }

        @Override
        public String name() {
            return this.key;
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
        public String writeValue(Map<String, String> model) {
            throw new UnsupportedOperationException(
                    "ReadOnlyMapKeyColumnDescriptor[" + this.key + "] is read-only");
        }

    }

}
