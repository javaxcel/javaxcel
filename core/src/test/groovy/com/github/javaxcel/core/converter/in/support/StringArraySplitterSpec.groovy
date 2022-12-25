package com.github.javaxcel.core.converter.in.support

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
