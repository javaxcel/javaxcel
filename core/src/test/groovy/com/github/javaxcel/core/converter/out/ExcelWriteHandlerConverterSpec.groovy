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

package com.github.javaxcel.core.converter.out

import com.github.javaxcel.core.analysis.ExcelAnalysis
import com.github.javaxcel.core.analysis.ExcelAnalysis.DefaultMeta.Source
import com.github.javaxcel.core.analysis.ExcelAnalysisImpl
import com.github.javaxcel.core.analysis.ExcelAnalysisImpl.DefaultMetaImpl
import com.github.javaxcel.core.analysis.out.ExcelWriteAnalyzer
import com.github.javaxcel.core.annotation.ExcelColumn
import com.github.javaxcel.core.annotation.ExcelModel
import com.github.javaxcel.core.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.test.converter.handler.impl.TimeUnitTypeHandler
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_Array1D
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_Array2D
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_Array3D
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_ColumnDefaultValue
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_Enum
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_GenericArray
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_Iterable
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_ModelDefaultValue
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_RawIterable
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_VariantIterable
import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

import java.lang.reflect.Field
import java.nio.file.AccessMode
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

class ExcelWriteHandlerConverterSpec extends Specification {

    def "Converts 1D array"() {
        given:
        def field = ExcelWriteHandlerConverter_TestModel_Array1D.getDeclaredField(fieldName)
        def model = new ExcelWriteHandlerConverter_TestModel_Array1D(array.asType(field.type))
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName  | array                                    || expected
        "booleans" | null                                     || null
        "booleans" | [false, true]                            || "[false, true]"
        "bytes"    | [-128, 0, 127]                           || "[-128, 0, 127]"
        "shorts"   | [-32768, 0, 32767]                       || "[-32768, 0, 32767]"
        "chars"    | ['a', 'B', '0', '/']                     || "[a, B, 0, /]"
        "ints"     | [74, 0, -12]                             || "[74, 0, -12]"
        "longs"    | [0, 9720, -8715]                         || "[0, 9720, -8715]"
        "floats"   | [0.0, 9.745, -1.14157]                   || "[0.0, 9.745, -1.14157]"
        "doubles"  | [3.141592, -0.0879, 0.0]                 || "[3.141592, -0.0879, 0.0]"
        "objects"  | []                                       || "[]"
        "objects"  | [null]                                   || "[]"
        "objects"  | [new Object() {
            String toString() { "java.lang.Object@x" }
        }]                                                    || "[java.lang.Object@x]"
        "strings"  | []                                       || "[]"
        "strings"  | [null]                                   || "[]"
        "strings"  | ["alpha", "beta"]                        || "[alpha, beta]"
        "strings"  | ["alpha", "", "gamma"]                   || "[alpha, , gamma]"
        "locales"  | []                                       || "[]"
        "locales"  | [null]                                   || "[]"
        "locales"  | [null, Locale.ROOT, null]                || "[, , ]"
        "locales"  | [Locale.US, Locale.KOREA, Locale.FRANCE] || "[en_US, ko_KR, fr_FR]"
    }

    def "Converts 2D array"() {
        given:
        def field = ExcelWriteHandlerConverter_TestModel_Array2D.getDeclaredField(fieldName)
        def model = new ExcelWriteHandlerConverter_TestModel_Array2D(array.asType(field.type))
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName  | array                                            || expected
        "booleans" | null                                             || null
        "booleans" | [[false], [true], [false, true]]                 || "[[false], [true], [false, true]]"
        "bytes"    | [[-128], null, [127]]                            || "[[-128], , [127]]"
        "shorts"   | [[-32768, 0, 32767]]                             || "[[-32768, 0, 32767]]"
        "chars"    | [['a', 'B'], [], ['0', '/']]                     || "[[a, B], [], [0, /]]"
        "ints"     | [null, [74, 0, -12]]                             || "[, [74, 0, -12]]"
        "longs"    | [[0], [], [9720, -8715]]                         || "[[0], [], [9720, -8715]]"
        "floats"   | [[0.0], [9.745, -1.14157]]                       || "[[0.0], [9.745, -1.14157]]"
        "doubles"  | [[3.141592, -0.0879, 0.0], null]                 || "[[3.141592, -0.0879, 0.0], ]"
        "objects"  | []                                               || "[]"
        "objects"  | [[null]]                                         || "[[]]"
        "objects"  | [[null, new Object() {
            String toString() { "java.lang.Object@x" }
        }]]                                                           || "[[, java.lang.Object@x]]"
        "locales"  | []                                               || "[]"
        "locales"  | [[]]                                             || "[[]]"
        "locales"  | [[], []]                                         || "[[], []]"
        "locales"  | [[], null, []]                                   || "[[], , []]"
        "locales"  | [null, [], [], null]                             || "[, [], [], ]"
        "locales"  | [null, [], null, [null, null]]                   || "[, [], , [, ]]"
        "locales"  | [null, [Locale.GERMANY, Locale.CHINA], [], null] || "[, [de_DE, zh_CN], [], ]"
        "locales"  | [[Locale.UK], [], [Locale.ITALY], []]            || "[[en_GB], [], [it_IT], []]"
    }

    def "Converts 3D array"() {
        given:
        def field = ExcelWriteHandlerConverter_TestModel_Array3D.getDeclaredField(fieldName)
        def model = new ExcelWriteHandlerConverter_TestModel_Array3D(array.asType(field.type))
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName  | array                                                            || expected
        "booleans" | null                                                             || null
        "booleans" | [[], [[false], [true]], null]                                    || "[[], [[false], [true]], ]"
        "bytes"    | [null, [[-128]], [[127]]]                                        || "[, [[-128]], [[127]]]"
        "shorts"   | [[[-32768, 0, 32767]]]                                           || "[[[-32768, 0, 32767]]]"
        "chars"    | [[['a'], [], ['B']], [], [['0', '/']]]                           || "[[[a], [], [B]], [], [[0, /]]]"
        "ints"     | [null, [null, [74], [0, -12]]]                                   || "[, [, [74], [0, -12]]]"
        "longs"    | [[[0], null], [], [[9720, -8715], null]]                         || "[[[0], ], [], [[9720, -8715], ]]"
        "floats"   | [[[], [0.0]], [[9.745], [-1.14157]]]                             || "[[[], [0.0]], [[9.745], [-1.14157]]]"
        "doubles"  | [[[3.141592, -0.0879], [0.0]], null]                             || "[[[3.141592, -0.0879], [0.0]], ]"
        "objects"  | []                                                               || "[]"
        "objects"  | [null, [[]], []]                                                 || "[, [[]], []]"
        "objects"  | [[], null, [[null, new Object() {
            String toString() { "java.lang.Object@x" }
        }, null]]]                                                                    || "[[], , [[, java.lang.Object@x, ]]]"
        "locales"  | []                                                               || "[]"
        "locales"  | [[]]                                                             || "[[]]"
        "locales"  | [[], []]                                                         || "[[], []]"
        "locales"  | [[], null, []]                                                   || "[[], , []]"
        "locales"  | [null, null, null, null]                                         || "[, , , ]"
        "locales"  | [null, [[], []], [], null]                                       || "[, [[], []], [], ]"
        "locales"  | [[[Locale.US, Locale.ENGLISH], [Locale.KOREA, Locale.KOREAN]]]   || "[[[en_US, en], [ko_KR, ko]]]"
        "locales"  | [[null, [null, Locale.ROOT], [Locale.JAPAN, Locale.TAIWAN]], []] || "[[, [, ], [ja_JP, zh_TW]], []]"
    }

    def "Converts generic array"() {
        given:
        def model = new ExcelWriteHandlerConverter_TestModel_GenericArray()
        model[fieldName] = value

        and:
        def field = model.class.getDeclaredField(fieldName)
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName       | value                                    || expected
        "objects"       | null                                     || null
        "objects"       | [null, null]                             || "[, ]"
        "strings_1"     | ["alpha"]                                || "[alpha]"
        "strings_2"     | ["alpha", null, "beta"]                  || "[alpha, , beta]"
        "strings_array" | [[], null, ["alpha", "beta"], ["gamma"]] || "[[], , [alpha, beta], [gamma]]"
    }

    def "Converts Iterable"() {
        given:
        def model = new ExcelWriteHandlerConverter_TestModel_Iterable()
        model[fieldName] = value

        and:
        def field = model.class.getDeclaredField(fieldName)
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName                      | value                                   || expected
        "iterable_integer"             | []                                      || "[]"
        "iterable_integer"             | [74, 0, -12]                            || "[74, 0, -12]"
        "collection_long"              | [0, 9720, -8715]                        || "[0, 9720, -8715]"
        "set_locale"                   | [Locale.US, Locale.KOREA, Locale.JAPAN] || "[en_US, ko_KR, ja_JP]"
        "collection_list_long"         | [[]]                                    || "[[]]"
        "collection_list_long"         | [[241832184], null, [748015106], []]    || "[[241832184], , [748015106], []]"
        "list_string"                  | ["alpha", "beta"]                       || "[alpha, beta]"
        "iterable_bigDecimal"          | [-0.7215, 3.141592, null]               || "[-0.7215, 3.141592, ]"
        "list_iterable_string"         | [["-9", "-3"], ["-1", "0"], ["3", "9"]] || "[[-9, -3], [-1, 0], [3, 9]]"
        "iterable_iterable_bigDecimal" | [[9.155, 12.784, -0.019], [], [6.218]]  || "[[9.155, 12.784, -0.019], [], [6.218]]"
    }

    def "Converts variant sub-interfaces of Iterable"() {
        given:
        def model = new ExcelWriteHandlerConverter_TestModel_VariantIterable()
        model[fieldName] = value

        and:
        def field = model.class.getDeclaredField(fieldName)
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName              | value                                                                   || expected
        "iterable_boolean"     | [true, false]                                                           || "[true, false]"
        "collection_byte"      | [-128, 0, 127]                                                          || "[-128, 0, 127]"
        "list_short"           | [-32768, 0, 32767]                                                      || "[-32768, 0, 32767]"
        "set_character"        | ['A', ' ', 'B', 'C']                                                    || "[A,  , B, C]"
        "sortedSet_integer"    | new TreeSet<>([-1048576, 0, 1048576])                                   || "[-1048576, 0, 1048576]"
        "navigableSet_long"    | new TreeSet<>([-4294967296L, 0L, 4294967296L])                          || "[-4294967296, 0, 4294967296]"
        "queue_float"          | new LinkedList<>([-1.414213F, 3.141592F])                               || "[-1.414213, 3.141592]"
        "deque_double"         | new ArrayDeque<>([-1.618033988749894D, 2.718281828459045D])             || "[-1.618033988749894, 2.718281828459045]"
        "blockingQueue_string" | new ArrayBlockingQueue<>(4, false, ["alpha", "beta", "gamma", "delta"]) || "[alpha, beta, gamma, delta]"
        "blockingDeque_locale" | new LinkedBlockingDeque<>([Locale.US, Locale.KOREA, Locale.JAPAN])      || "[en_US, ko_KR, ja_JP]"
    }

    def "Converts raw Iterable"() {
        given:
        def model = new ExcelWriteHandlerConverter_TestModel_RawIterable()
        model[fieldName] = value

        and:
        def field = model.class.getDeclaredField(fieldName)
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName       | value                                                     || expected
        "iterable"      | [true, false]                                             || "[true, false]"
        "collection"    | [-128, 0, 127]                                            || "[-128, 0, 127]"
        "list"          | ["-32,768", 0, 32767]                                     || "[-32,768, 0, 32767]"
        "set"           | ['A', 0, 'B', 24]                                         || "[A, 0, B, 24]"
        "sortedSet"     | new TreeSet([3.14D, -1048576D])                           || "[-1048576.0, 3.14]"
        "navigableSet"  | new TreeSet(["alpha", 'D', "beta"])                       || "[D, alpha, beta]"
        "queue"         | new LinkedList([-1.414213F, "gamma"])                     || "[-1.414213, gamma]"
        "deque"         | new ArrayDeque([Locale.US, 2.718281828459045D])           || "[en_US, 2.718281828459045]"
        "blockingQueue" | new ArrayBlockingQueue(2, false, ["delta", Locale.KOREA]) || "[delta, ko_KR]"
        "blockingDeque" | new LinkedBlockingDeque([3.141592F, Locale.JAPAN])        || "[3.141592, ja_JP]"
    }

    def "Converts mixed Iterable and array"() {
        given:
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.FIELD_ACCESS)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName           | model                                                                       || expected
        "collection_array"  | new IterableArray(collection_array: [])                                     || "[]"
        "collection_array"  | new IterableArray(collection_array: [[], [1, 2, 3], [4], null, [5, 6]])     || "[[], [1, 2, 3], [4], , [5, 6]]"
        "list_2d_array"     | new IterableArray(list_2d_array: [[["a"], ["b"]], [["c", "d"]], [["e"]]])   || "[[[a], [b]], [[c, d]], [[e]]]"
        "iterable_iterable" | new IterableArray(iterable_iterable: [[2.5, 3.2], null, [-0.14, null], []]) || "[[2.5, 3.2], , [-0.14, ], []]"
    }

    def "Converts enum by custom handler"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()
        registry.add(new TimeUnitTypeHandler())

        and:
        def model = new ExcelWriteHandlerConverter_TestModel_Enum()
        model[fieldName] = value

        and:
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, registry)
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName    | value                 || expected
        "accessMode" | null                  || null
        "accessMode" | AccessMode.READ       || "READ"
        "accessMode" | AccessMode.WRITE      || "WRITE"
        "accessMode" | AccessMode.EXECUTE    || "EXECUTE"
        "timeUnit"   | null                  || null
        "timeUnit"   | TimeUnit.DAYS         || "days"
        "timeUnit"   | TimeUnit.HOURS        || "hrs"
        "timeUnit"   | TimeUnit.MINUTES      || "min"
        "timeUnit"   | TimeUnit.SECONDS      || "sec"
        "timeUnit"   | TimeUnit.MILLISECONDS || "ms"
        "timeUnit"   | TimeUnit.MICROSECONDS || "μs"
        "timeUnit"   | TimeUnit.NANOSECONDS  || "ns"
    }

    def "Converts through default value on model"() {
        given:
        def model = new ExcelWriteHandlerConverter_TestModel_ModelDefaultValue()

        and:
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName  | expected
        "_boolean" | "false"
        "_byte"    | "0"
        "_short"   | "0"
        "_char"    | "\u0000"
        "_int"     | "0"
        "_long"    | "0"
        "_float"   | "0.0"
        "_double"  | "0.0"
        "string"   | "<null>"
        "locale"   | "<null>"
        "objects"  | "<null>"
        "chars"    | "<null>"
        "strings"  | "<null>"
        "locales"  | "<null>"
    }

    def "Converts through default value on column"() {
        given:
        def model = new ExcelWriteHandlerConverter_TestModel_ColumnDefaultValue()

        and:
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName  | expected
        "_boolean" | "false"
        "_byte"    | "0"
        "_short"   | "0"
        "_char"    | "\u0000"
        "_int"     | "0"
        "_long"    | "0"
        "_float"   | "0.0"
        "_double"  | "0.0"
        "string"   | "[1]"
        "locale"   | "en_US"
        "objects"  | "[]"
        "chars"    | "[A, B, C]"
        "strings"  | "[alpha, beta]"
        "locales"  | "[[en_US], [ko_KR]]"
    }

    // -------------------------------------------------------------------------------------------------

    private static Iterable<ExcelAnalysis> analyze(Field[] fields, int flags) {
        fields.findAll { !it.isSynthetic() }.collect {
            def analysis = new ExcelAnalysisImpl(it)

            def defaultMeta = DefaultMetaImpl.EMPTY
            if (it.isAnnotationPresent(ExcelColumn)) {
                defaultMeta = new DefaultMetaImpl(it.getAnnotation(ExcelColumn).defaultValue(), Source.COLUMN)
            } else if (it.declaringClass.isAnnotationPresent(ExcelModel)) {
                defaultMeta = new DefaultMetaImpl(it.declaringClass.getAnnotation(ExcelModel).defaultValue(), Source.MODEL)
            }
            analysis.defaultMeta = defaultMeta
            analysis.addFlags(ExcelWriteAnalyzer.HANDLER | flags)

            analysis
        }
    }

    // -------------------------------------------------------------------------------------------------

    @EqualsAndHashCode
    private static class IterableArray {
        Collection<int[]> collection_array
        List<String>[][] list_2d_array
        Iterable<Iterable<BigDecimal>> iterable_iterable
    }

}
