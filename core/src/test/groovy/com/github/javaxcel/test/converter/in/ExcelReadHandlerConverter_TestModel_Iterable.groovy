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

@SuppressWarnings("unused")
class ExcelReadHandlerConverter_TestModel_Iterable<
        A extends String,
        B extends Iterable<BigDecimal>> {

    Iterable<Integer> iterable_string
    Collection<Long> collection_long
    Collection<List<Long>> collection_list_long
    List<A> list_string
    B iterable_bigDecimal
    List<Iterable<A>> list_iterable_string
    Iterable<B> iterable_iterable_bigDecimal

}
