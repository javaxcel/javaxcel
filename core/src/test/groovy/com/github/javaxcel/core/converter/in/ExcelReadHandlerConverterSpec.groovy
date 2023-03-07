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

package com.github.javaxcel.core.converter.in

import spock.lang.Specification
import spock.lang.Subject

import java.lang.reflect.Field
import java.nio.file.AccessMode
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

import com.github.javaxcel.core.analysis.ExcelAnalysis
import com.github.javaxcel.core.analysis.ExcelAnalysis.DefaultMeta.Source
import com.github.javaxcel.core.analysis.ExcelAnalysisImpl
import com.github.javaxcel.core.analysis.ExcelAnalysisImpl.DefaultMetaImpl
import com.github.javaxcel.core.analysis.in.ExcelReadAnalyzer
import com.github.javaxcel.core.annotation.ExcelColumn
import com.github.javaxcel.core.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.test.converter.handler.impl.TimeUnitTypeHandler
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_Array1D
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_Array2D
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_Array3D
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_DefaultValue
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_Enum
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_GenericArray
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_Iterable
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_MixedIterableArray
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_RawIterable
import com.github.javaxcel.test.converter.in.ExcelReadHandlerConverter_TestModel_VariantIterable

@Subject(ExcelReadHandlerConverter)
class ExcelReadHandlerConverterSpec extends Specification {

    def "Converts into 1D array"() {
        given:
        def variables = [(fieldName): value]
        def field = ExcelReadHandlerConverter_TestModel_Array1D.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected.asType(field.type)

        where:
        fieldName  | value                           || expected
        "booleans" | null                            || null
        "booleans" | "[false, true]"                 || [false, true]
        "bytes"    | "[-128, 0, 127]"                || [-128, 0, 127]
        "shorts"   | "[-32768, 0, 32767]"            || [-32768, 0, 32767]
        "chars"    | "[a, B, 0, /]"                  || ['a', 'B', '0', '/']
        "ints"     | "[74, 0, -12]"                  || [74, 0, -12]
        "longs"    | "[0, 9720, -8715]"              || [0, 9720, -8715]
        "floats"   | "[0.0, 9.745, -1.14157]"        || [0.0, 9.745, -1.14157]
        "doubles"  | "[3.141592, -0.0879, 0.0]"      || [3.141592, -0.0879, 0.0]
        "objects"  | "[]"                            || []
        "objects"  | "[, java.lang.Object@736e9adb]" || [null, null]
        "strings"  | "[]"                            || []
        "strings"  | "[, ]"                          || [null, null]
        "strings"  | "[alpha, beta]"                 || ["alpha", "beta"]
        "strings"  | "[alpha, , gamma]"              || ["alpha", null, "gamma"]
        "locales"  | "[]"                            || []
        "locales"  | "[, ]"                          || [null, null]
        "locales"  | "[en_US, ko_KR, fr_FR]"         || [Locale.US, Locale.KOREA, Locale.FRANCE]
        "locales"  | "[en, ja, , ]"                  || [Locale.ENGLISH, Locale.JAPANESE, null, null]
    }

    def "Converts into 2D array"() {
        given:
        def variables = [(fieldName): value]
        def field = ExcelReadHandlerConverter_TestModel_Array2D.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.SETTER)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected.asType(field.type)

        where:
        fieldName  | value                                 || expected
        "booleans" | null                                  || null
        "booleans" | "[[false], [true], [false, true]]"    || [[false], [true], [false, true]]
        "bytes"    | "[[-128], , [127]]"                   || [[-128], null, [127]]
        "shorts"   | "[[-32768, 0, 32767]]"                || [[-32768, 0, 32767]]
        "chars"    | "[[a, B], [], [0, /]]"                || [['a', 'B'], [], ['0', '/']]
        "ints"     | "[, [74, 0, -12]]"                    || [null, [74, 0, -12]]
        "longs"    | "[[0], [], [9720, -8715]]"            || [[0], [], [9720, -8715]]
        "floats"   | "[[0.0], [9.745, -1.14157]]"          || [[0.0], [9.745, -1.14157]]
        "doubles"  | "[[3.141592, -0.0879, 0.0], ]"        || [[3.141592, -0.0879, 0.0], null]
        "objects"  | "[]"                                  || []
        "objects"  | "[, [], ]"                            || [null, [], null]
        "objects"  | "[[java.lang.Object@736e9adb], []]"   || [[null], []] // There is no handler for Object.
        "strings"  | "[]"                                  || []
        "strings"  | "[, ]"                                || [null, null]
        "strings"  | "[, [], [], ]"                        || [null, [], [], null]
        "strings"  | "[[alpha, beta], [gamma, ], ]"        || [["alpha", "beta"], ["gamma", null], null]
        "strings"  | "[[], [alpha, beta, gamma], [delta]]" || [[], ["alpha", "beta", "gamma"], ["delta"]]
        "locales"  | "[]"                                  || []
        "locales"  | "[[]]"                                || [[]]
        "locales"  | "[[], []]"                            || [[], []]
        "locales"  | "[[], , []]"                          || [[], null, []]
        "locales"  | "[, [], [], ]"                        || [null, [], [], null]
        "locales"  | "[, [], , [, ]]"                      || [null, [], null, [null, null]]
        "locales"  | "[, [de_DE, zh_CN], [], ]"            || [null, [Locale.GERMANY, Locale.CHINA], [], null]
        "locales"  | "[[en_GB], [], [it_IT], []]"          || [[Locale.UK], [], [Locale.ITALY], []]
    }

    def "Converts into 3D array"() {
        given:
        def variables = [(fieldName): value]
        def field = ExcelReadHandlerConverter_TestModel_Array3D.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected.asType(field.type)

        where:
        fieldName  | value                                          || expected
        "booleans" | null                                           || null
        "booleans" | "[[], [[false], [true]], ]"                    || [[], [[false], [true]], null]
        "bytes"    | "[, [[-128]], [[127]]]"                        || [null, [[-128]], [[127]]]
        "shorts"   | "[[[-32768, 0, 32767]]]"                       || [[[-32768, 0, 32767]]]
        "chars"    | "[[[a], [], [B]], [], [[0, /]]]"               || [[['a'], [], ['B']], [], [['0', '/']]]
        "ints"     | "[, [, [74], [0, -12]]]"                       || [null, [null, [74], [0, -12]]]
        "longs"    | "[[[0], ], [], [[9720, -8715], ]]"             || [[[0], null], [], [[9720, -8715], null]]
        "floats"   | "[[[], [0.0]], [[9.745], [-1.14157]]]"         || [[[], [0.0]], [[9.745], [-1.14157]]]
        "doubles"  | "[[[3.141592, -0.0879], [0.0]], ]"             || [[[3.141592, -0.0879], [0.0]], null]
        "objects"  | "[]"                                           || []
        "objects"  | "[, [], [[]]]"                                 || [null, [], [[]]]
        "objects"  | "[, [[java.lang.Object@736e9adb, ]], []]"      || [null, [[null, null]], []] // There is no handler for Object.
        "strings"  | "[]"                                           || []
        "strings"  | "[, ]"                                         || [null, null]
        "strings"  | "[, [], [], ]"                                 || [null, [], [], null]
        "strings"  | "[[[], []], [[, , ]], , []]"                   || [[[], []], [[null, null, null]], null, []]
        "strings"  | "[, [[alpha, ], [beta]], [[], []], [[gamma]]]" || [null, [["alpha", null], ["beta"]], [[], []], [["gamma"]]]
        "strings"  | "[[[alpha, beta, , gamma]], [[delta]], []]"    || [[["alpha", "beta", null, "gamma"]], [["delta"]], []]
        "locales"  | "[]"                                           || []
        "locales"  | "[[]]"                                         || [[]]
        "locales"  | "[[], []]"                                     || [[], []]
        "locales"  | "[[], , []]"                                   || [[], null, []]
        "locales"  | "[, , , ]"                                     || [null, null, null, null]
        "locales"  | "[, [[], []], [], ]"                           || [null, [[], []], [], null]
        "locales"  | "[[[en_US, en], [ko_KR, ko]]]"                 || [[[Locale.US, Locale.ENGLISH], [Locale.KOREA, Locale.KOREAN]]]
        "locales"  | "[[, [, ], [ja_JP, zh_TW]], []]"               || [[null, [null, null], [Locale.JAPAN, Locale.TAIWAN]], []]
    }

    def "Converts into generic array"() {
        given:
        def variables = [(fieldName): value]
        def field = ExcelReadHandlerConverter_TestModel_GenericArray.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.SETTER)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected.asType(field.type)

        where:
        fieldName       | value                            || expected
        "objects"       | null                             || null
        "objects"       | "[, ]"                           || [null, null]
        "strings_1"     | "[alpha]"                        || ["alpha"]
        "strings_2"     | "[alpha, , beta]"                || ["alpha", null, "beta"]
        "strings_array" | "[[], , [alpha, beta], [gamma]]" || [[], null, ["alpha", "beta"], ["gamma"]]
    }

    def "Converts into Iterable"() {
        given:
        def variables = [(fieldName): value]
        def field = ExcelReadHandlerConverter_TestModel_Iterable.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected.asType(field.type)

        where:
        fieldName                      | value                                    || expected
        "iterable_integer"             | null                                     || null
        "iterable_integer"             | "[74, 0, -12]"                           || [74, 0, -12]
        "collection_long"              | "[0, 9720, -8715]"                       || [0, 9720, -8715]
        "set_locale"                   | "[en_US, ko_KR, ja_JP]"                  || [Locale.US, Locale.KOREA, Locale.JAPAN]
        "collection_list_long"         | "[[]]"                                   || [[]]
        "collection_list_long"         | "[[241832184], , [748015106], []]"       || [[241832184], null, [748015106], []]
        "list_string"                  | "[alpha, beta]"                          || ["alpha", "beta"]
        "iterable_bigDecimal"          | "[-0.7215, 3.141592, ]"                  || [-0.7215, 3.141592, null]
        "list_iterable_string"         | "[[-9, -3], [-1, 0], [3, 9]]"            || [["-9", "-3"], ["-1", "0"], ["3", "9"]]
        "iterable_iterable_bigDecimal" | "[[9.155, 12.784, -0.019], [], [6.218]]" || [[9.155, 12.784, -0.019], [], [6.218]]
    }

    def "Converts into variant sub-interfaces of Iterable"() {
        given:
        def variables = [(fieldName): value]
        def field = ExcelReadHandlerConverter_TestModel_VariantIterable.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.SETTER)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field) as Iterable

        then:
        field.type.isInstance(actual)
        actual.class == expected.class

        and: "These are not supported on comparison: ArrayDeque, ArrayBlockingQueue, LinkedBlockingDeque"
        actual == expected || actual.toList() == expected.toList()

        where:
        fieldName              | value                                     || expected
        "iterable_boolean"     | "[true, false]"                           || [true, false]
        "collection_byte"      | "[-128, 0, 127]"                          || [-128, 0, 127]
        "list_short"           | "[-32768, 0, 32767]"                      || [-32768, 0, 32767]
        "set_character"        | "[A,  , B, C]"                            || new HashSet<>(['A', ' ', 'B', 'C'])
        "sortedSet_integer"    | "[-1048576, 0, 1048576]"                  || new TreeSet<>([-1048576, 0, 1048576])
        "navigableSet_long"    | "[-4294967296, 0, 4294967296]"            || new TreeSet<>([-4294967296L, 0L, 4294967296L])
        "queue_float"          | "[-1.414213, 3.141592]"                   || new LinkedList<>([-1.414213F, 3.141592F])
        "deque_double"         | "[-1.618033988749894, 2.718281828459045]" || new ArrayDeque<>([-1.618033988749894D, 2.718281828459045D])
        "blockingQueue_string" | "[alpha, beta, gamma, delta]"             || new ArrayBlockingQueue<>(4, false, ["alpha", "beta", "gamma", "delta"])
        "blockingDeque_locale" | "[en_US, ko_KR, ja_JP]"                   || new LinkedBlockingDeque<>([Locale.US, Locale.KOREA, Locale.JAPAN])
    }

    def "Converts into raw Iterable"() {
        given:
        def variables = [(fieldName): value]
        def field = ExcelReadHandlerConverter_TestModel_RawIterable.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.SETTER)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field) as Iterable

        then:
        field.type.isInstance(actual)

        and: "These are not supported on comparison: ArrayDeque, ArrayBlockingQueue, LinkedBlockingDeque"
        actual == expected || actual.toList() == expected.toList()

        where:
        fieldName       | value                || expected
        "iterable"      | "[true, false]"      || [null, null]
        "collection"    | "[-128, 0, 127]"     || [null, null, null]
        "list"          | "[-32768, 0, 32767]" || [null, null, null]
        "set"           | "[A, B, , C]"        || [null] // Set removes the duplicated element from itself.
        "sortedSet"     | "[]"                 || []
        "navigableSet"  | "[]"                 || []
        "queue"         | "[alpha]"            || [null]
        "deque"         | "[]"                 || []
        "blockingQueue" | "[]"                 || []
        "blockingDeque" | "[]"                 || []
    }

    def "Converts into enum by custom handler"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()
        registry.add(new TimeUnitTypeHandler())

        and:
        def variables = [(fieldName): value]
        def field = ExcelReadHandlerConverter_TestModel_Enum.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.SETTER)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, registry)
        def actual = converter.convert(variables, field)

        then:
        actual == expected

        where:
        fieldName    | value     || expected
        "accessMode" | null      || null
        "accessMode" | "READ"    || AccessMode.READ
        "accessMode" | "WRITE"   || AccessMode.WRITE
        "accessMode" | "EXECUTE" || AccessMode.EXECUTE
        "timeUnit"   | null      || null
        "timeUnit"   | "days"    || TimeUnit.DAYS
        "timeUnit"   | "hrs"     || TimeUnit.HOURS
        "timeUnit"   | "min"     || TimeUnit.MINUTES
        "timeUnit"   | "sec"     || TimeUnit.SECONDS
        "timeUnit"   | "ms"      || TimeUnit.MILLISECONDS
        "timeUnit"   | "μs"      || TimeUnit.MICROSECONDS
        "timeUnit"   | "ns"      || TimeUnit.NANOSECONDS
    }

    def "Converts through default value"() {
        given:
        def variables = [(fieldName): null]
        def field = ExcelReadHandlerConverter_TestModel_DefaultValue.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.SETTER)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected.asType(field.type)

        where:
        fieldName         | expected
        "_boolean"        | true
        "_byte"           | 127
        "_short"          | 32767
        "_char"           | 'A'
        "_int"            | 1048576
        "_long"           | -1073741824L
        "_float"          | 3.141592F
        "_double"         | -2.718281828459045D
        "boolean_wrapper" | true
        "byte_wrapper"    | -128
        "short_wrapper"   | -32768
        "char_wrapper"    | 'B'
        "int_wrapper"     | -1048576
        "long_wrapper"    | 1073741824L
        "float_wrapper"   | -3.141592F
        "double_wrapper"  | 2.718281828459045D
        "string"          | "[1]"
        "locale"          | Locale.US
        "objects"         | []
        "chars"           | ['A', 'B', 'C']
        "strings"         | ["alpha", "beta"]
        "locales"         | [[Locale.US], [Locale.KOREA]]
    }

    // -------------------------------------------------------------------------------------------------

    def "Fails to convert into mixed Iterable and array"() {
        given:
        def variables = [(fieldName): value]
        def field = ExcelReadHandlerConverter_TestModel_MixedIterableArray.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        converter.convert(variables, field)

        then:
        def e = thrown(UnsupportedOperationException)
        e.message.startsWith("Mixed array and iterable is not supported: $field")

        where:
        fieldName                      | value
        "iterable_array_integer"       | "[[1], [2], [3, 4]]"
        "list_list_array_array_string" | "[[[[A], [B]], [[C]]], [[[D]]]]"
        "array_collection_bigDecimal"  | "[, [1024], [64, -128]]"
        "array_array_set_double"       | "[[[3.14, 2.178]], [], [[-1.168]]]"
        "array_queue_raw"              | "[[1, A, 0.1, alpha]]"
    }

    // -------------------------------------------------------------------------------------------------

    private static Iterable<ExcelAnalysis> analyze(Field[] fields, int flags) {
        fields.findAll { !it.isSynthetic() }.collect {
            def analysis = new ExcelAnalysisImpl(it)

            def defaultMeta = DefaultMetaImpl.EMPTY
            if (it.isAnnotationPresent(ExcelColumn)) {
                defaultMeta = new DefaultMetaImpl(it.getAnnotation(ExcelColumn).defaultValue(), Source.COLUMN)
            }
            analysis.defaultMeta = defaultMeta
            analysis.addFlags(ExcelReadAnalyzer.HANDLER | flags)

            analysis
        }
    }

}
