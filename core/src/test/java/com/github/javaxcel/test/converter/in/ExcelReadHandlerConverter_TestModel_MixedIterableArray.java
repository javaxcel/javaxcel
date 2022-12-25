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

package com.github.javaxcel.test.converter.in;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@Getter
@Setter
@ToString
@SuppressWarnings("rawtypes")
public class ExcelReadHandlerConverter_TestModel_MixedIterableArray {

    private Iterable<Integer[]> iterable_array_integer;
    private List<List<String[][]>> list_list_array_array_string;
    private Collection<BigDecimal>[] array_collection_bigDecimal;
    private Set<Double>[][] array_array_set_double;
    private Queue[] array_queue_raw;

}
