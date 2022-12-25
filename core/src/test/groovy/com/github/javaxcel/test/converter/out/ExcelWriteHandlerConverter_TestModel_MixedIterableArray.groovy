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

@SuppressWarnings("unused")
class ExcelWriteHandlerConverter_TestModel_MixedIterableArray {

    Iterable<Integer[]> iterable_array_integer
    List<List<String[][]>> list_list_array_array_string
    Collection<BigDecimal>[] array_collection_bigDecimal
    Set<Double>[][] array_array_set_double
    Queue[] array_queue_raw

}
