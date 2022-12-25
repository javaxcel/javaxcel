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
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_Enum
import com.github.javaxcel.test.converter.out.ExcelWriteHandlerConverter_TestModel_GenericArray
import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

import java.lang.reflect.Field
import java.nio.file.AccessMode
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


    def "Converts mixed iterable and array"() {
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

    def "Returns default value"() {
        given:
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName | model                                || expected
        "id"      | new DefaultValueModel(id: 12)        || "12"
        "id"      | new DefaultValueModel(id: null)      || "-1"
        "name"    | new DefaultValueModel(name: "alpha") || "alpha"
        "name"    | new DefaultValueModel(name: null)    || "<null>"
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

    @ExcelModel(defaultValue = "<null>")
    @EqualsAndHashCode
    private static class DefaultValueModel {
        @ExcelColumn(defaultValue = "-1")
        Long id
        String name
    }

    @EqualsAndHashCode
    private static class IterableArray {
        Collection<int[]> collection_array
        List<String>[][] list_2d_array
        Iterable<Iterable<BigDecimal>> iterable_iterable
    }

}
