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

package com.github.javaxcel.test.converter.in

import com.github.javaxcel.core.converter.in.ExcelReadHandlerConverter
import spock.lang.Subject

import java.util.concurrent.BlockingDeque
import java.util.concurrent.BlockingQueue

@Subject(ExcelReadHandlerConverter)
@SuppressWarnings("unused")
class ExcelReadHandlerConverter_TestModel_RawIterable {

    Iterable iterable
    Collection collection
    List list
    Set set
    SortedSet sortedSet
    NavigableSet navigableSet
    Queue queue
    Deque deque
    BlockingQueue blockingQueue
    BlockingDeque blockingDeque

}
