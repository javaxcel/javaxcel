/*
 * Copyright 2025 Javaxcel
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

package com.github.javaxcel.core.internal.converter.in.support

import spock.lang.Specification
import spock.lang.Subject

@Subject(StringArraySplitter)
class StringArraySplitterSpec extends Specification {

    def "Splits a string as array shallowly"() {
        given:
        StringArraySplitter splitter = new StringArraySplitter(", ")

        when:
        String[] actual = splitter.shallowSplit(string)

        then:
        actual == expected as String[]

        where:
        string                                                            | expected
        "[]"                                                              | []
        "[10]"                                                            | ["10"]
        "[, ]"                                                            | ["", ""]
        "[[], , ]"                                                        | ["[]", "", ""]
        "[, , []]"                                                        | ["", "", "[]"]
        "[1, 2, 3]"                                                       | ["1", "2", "3"]
        "[[], [], ]"                                                      | ["[]", "[]", ""]
        "[, [], []]"                                                      | ["", "[]", "[]"]
        "[[], , []]"                                                      | ["[]", "", "[]"]
        "[, , , , ]"                                                      | ["", "", "", "", ""]
        "[, , [2], []]"                                                   | ["", "", "[2]", "[]"]
        "[[], , , , []]"                                                  | ["[]", "", "", "", "[]"]
        "[, [1, ], [2], ]"                                                | ["", "[1, ]", "[2]", ""]
        "[, , [2], [], ]"                                                 | ["", "", "[2]", "[]", ""]
        "[, , , [], , []]"                                                | ["", "", "", "[]", "", "[]"]
        "[, , [], , [2], ]"                                               | ["", "", "[]", "", "[2]", ""]
        "[, , , [], , [2], ]"                                             | ["", "", "", "[]", "", "[2]", ""]
        "[[], [1, 2, 4, 5], [0, [0]], [], 2]"                             | ["[]", "[1, 2, 4, 5]", "[0, [0]]", "[]", "2"]
        "[, [[[2, 5]]], [], [, [, [1]]], , [[[2], [4, 5], [6]], [], ], ]" | ["", "[[[2, 5]]]", "[]", "[, [, [1]]]", "", "[[[2], [4, 5], [6]], [], ]", ""]
    }

    def "Gets length of array shallowly"() {
        given:
        StringArraySplitter splitter = new StringArraySplitter(", ")

        when:
        int actual = splitter.getShallowLength(string)

        then:
        actual == expected

        where:
        string                                                            | expected
        "[]"                                                              | 0
        "[10]"                                                            | 1
        "[, ]"                                                            | 2
        "[1, 2, 3]"                                                       | 3
        "[, [1, ], [2], ]"                                                | 4
        "[, , [2], []]"                                                   | 4
        "[, , , [], , [2], ]"                                             | 7
        "[[], [1, 2, 4, 5], [0, [0]], [], 2]"                             | 5
        "[, [[[2, 5]]], [], [, [, [1]]], , [[[2], [4, 5], [6]], [], ], ]" | 7
    }

}
