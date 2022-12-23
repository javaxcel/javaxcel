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

package com.github.javaxcel.core.converter.in.support;

import io.github.imsejin.common.assertion.Asserts;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class CollectionSupplier {

    private static final Map<Class<? extends Iterable>, Function<Integer, Collection<?>>> SUPPLIER_MAP;

    static {
        Map<Class<? extends Iterable>, Function<Integer, Collection<?>>> supplierMap = new HashMap<>();

        supplierMap.put(BlockingDeque.class, LinkedBlockingDeque::new);
        supplierMap.put(BlockingQueue.class, ArrayBlockingQueue::new);
        supplierMap.put(Deque.class, ArrayDeque::new);
        supplierMap.put(Queue.class, n -> new LinkedList<>());
        supplierMap.put(NavigableSet.class, n -> new TreeSet<>());
        supplierMap.put(SortedSet.class, n -> new TreeSet<>());
        supplierMap.put(Set.class, HashSet::new);
        supplierMap.put(List.class, ArrayList::new);
        supplierMap.put(Collection.class, ArrayList::new);
        supplierMap.put(Iterable.class, ArrayList::new);

        Asserts.that(supplierMap)
                .isNotNull()
                .isNotEmpty()
                .asKeySet()
                .allMatch(Class::isInterface);

        SUPPLIER_MAP = Collections.unmodifiableMap(supplierMap);
    }

    public static Collection<?> supply(Class<?> iterableType, int capacity) {
        Asserts.that(iterableType)
                .isNotNull()
                .is(Iterable.class::isAssignableFrom)
                .isInterface();

        // BlockingQueue, BlockingDeque doesn't allow zero as its capacity.
        if (capacity == 0) {
            capacity++;
        }

        Asserts.that(capacity)
                .isNotNull()
                .isPositive();

        Function<Integer, Collection<?>> function = SUPPLIER_MAP.get(iterableType);
        if (function == null) {
            throw new UnsupportedOperationException("CollectionSupplier cannot support a type: " + iterableType);
        }

        return function.apply(capacity);
    }

}
