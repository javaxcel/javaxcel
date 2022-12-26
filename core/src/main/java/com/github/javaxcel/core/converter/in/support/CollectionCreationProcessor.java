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

/**
 * Processor for creation of collection
 *
 * @since 0.9.0
 */
public class CollectionCreationProcessor {

    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends Iterable>, Function<Integer, Collection<?>>> CREATION_MAP;

    static {
        @SuppressWarnings("rawtypes")
        Map<Class<? extends Iterable>, Function<Integer, Collection<?>>> creationMap = new HashMap<>();

        creationMap.put(Iterable.class, ArrayList::new);
        creationMap.put(Collection.class, ArrayList::new);
        creationMap.put(List.class, ArrayList::new);
        creationMap.put(Set.class, HashSet::new);
        creationMap.put(SortedSet.class, n -> new TreeSet<>());
        creationMap.put(NavigableSet.class, n -> new TreeSet<>());
        creationMap.put(Queue.class, n -> new LinkedList<>());
        creationMap.put(Deque.class, ArrayDeque::new);
        creationMap.put(BlockingQueue.class, ArrayBlockingQueue::new);
        creationMap.put(BlockingDeque.class, LinkedBlockingDeque::new);

        Asserts.that(creationMap)
                .isNotNull()
                .isNotEmpty()
                .asKeySet()
                .allMatch(Class::isInterface);

        CREATION_MAP = Collections.unmodifiableMap(creationMap);
    }

    /**
     * Creates a new collection.
     *
     * @param iterableType type of Iterable
     * @param capacity     initial capacity of the collection
     * @return collection
     */
    public static Collection<?> create(Class<?> iterableType, int capacity) {
        Asserts.that(iterableType)
                .isNotNull()
                .is(Iterable.class::isAssignableFrom)
                .isInterface();

        // BlockingQueue, BlockingDeque doesn't allow zero as its capacity.
        if (capacity == 0) {
            capacity = 1;
        }

        Asserts.that(capacity)
                .isNotNull()
                .isPositive();

        Function<Integer, Collection<?>> creator = CREATION_MAP.get(iterableType);
        if (creator == null) {
            throw new UnsupportedOperationException("CollectionCreationProcessor cannot support a type: " + iterableType);
        }

        return creator.apply(capacity);
    }

}
