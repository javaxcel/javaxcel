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

package com.github.javaxcel.test.converter.out

import spock.lang.Subject

import java.util.concurrent.BlockingDeque
import java.util.concurrent.BlockingQueue

import com.github.javaxcel.core.converter.out.ExcelWriteHandlerConverter

@Subject(ExcelWriteHandlerConverter)
@SuppressWarnings("unused")
class ExcelWriteHandlerConverter_TestModel_VariantIterable {

    Iterable<Boolean> iterable_boolean
    Collection<Byte> collection_byte
    List<Short> list_short
    Set<Character> set_character
    SortedSet<Integer> sortedSet_integer
    NavigableSet<Long> navigableSet_long
    Queue<Float> queue_float
    Deque<Double> deque_double
    BlockingQueue<String> blockingQueue_string
    BlockingDeque<Locale> blockingDeque_locale

}
