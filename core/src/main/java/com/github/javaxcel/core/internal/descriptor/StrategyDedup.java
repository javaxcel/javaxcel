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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ArrayUtils;

import com.github.javaxcel.core.in.context.ExcelReadContext;
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy;
import com.github.javaxcel.core.out.context.ExcelWriteContext;
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

/**
 * Deduplicates strategies by class name, dropping any whose
 * {@code isSupported(context)} returns {@code false}. Called by
 * {@code DefaultExcelReader.options} and {@code DefaultExcelWriter.options}.
 *
 * <p>Equality between two {@link ExcelReadStrategy} instances of the same class
 * is determined by their class name — not by {@code equals}/{@code hashCode}.
 * Passing two instances of the same class keeps only one.
 */
public final class StrategyDedup {

    private StrategyDedup() {
    }

    public static Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> collect(
            ExcelReadStrategy[] strategies, ExcelReadContext<?> context) {
        Asserts.that(strategies)
                .describedAs("strategies is not allowed to be null")
                .isNotNull()
                .describedAs("strategies cannot have null element: {0}", ArrayUtils.toString(strategies))
                .doesNotContainNull();

        if (strategies.length == 0) {
            return Collections.emptyMap();
        }

        Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> map = Arrays.stream(strategies)
                .filter(it -> it.isSupported(context))
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(it -> it.getClass().getName()))),
                        set -> set.stream().collect(toMap(ExcelReadStrategy::getClass, Function.identity()))));

        return Collections.unmodifiableMap(map);
    }

    public static Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> collect(
            ExcelWriteStrategy[] strategies, ExcelWriteContext<?> context) {
        Asserts.that(strategies)
                .describedAs("strategies is not allowed to be null")
                .isNotNull()
                .describedAs("strategies cannot have null element: {0}", ArrayUtils.toString(strategies))
                .doesNotContainNull();

        if (strategies.length == 0) {
            return Collections.emptyMap();
        }

        Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> map = Arrays.stream(strategies)
                .filter(it -> it.isSupported(context))
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(it -> it.getClass().getName()))),
                        set -> set.stream().collect(toMap(ExcelWriteStrategy::getClass, Function.identity()))));

        return Collections.unmodifiableMap(map);
    }

}
