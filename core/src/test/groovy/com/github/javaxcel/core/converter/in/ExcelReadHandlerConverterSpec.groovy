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

import com.github.javaxcel.core.analysis.ExcelAnalysis
import com.github.javaxcel.core.analysis.ExcelAnalysis.DefaultMeta.Source
import com.github.javaxcel.core.analysis.ExcelAnalysisImpl
import com.github.javaxcel.core.analysis.ExcelAnalysisImpl.DefaultMetaImpl
import com.github.javaxcel.core.analysis.in.ExcelReadAnalyzer
import com.github.javaxcel.core.annotation.ExcelColumn
import com.github.javaxcel.core.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.test.converter.handler.impl.TimeUnitTypeHandler
import com.github.javaxcel.test.model.Array1D
import com.github.javaxcel.test.model.Array2D
import com.github.javaxcel.test.model.Array3D
import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

import java.lang.reflect.Field
import java.nio.file.AccessMode
import java.util.concurrent.TimeUnit

class ExcelReadHandlerConverterSpec extends Specification {

    def "Converts into 1D Array"() {
        given:
        def variables = [(fieldName): value]
        def field = Array1D.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected

        where:
        fieldName  | value                           || expected
        "booleans" | null                            || null
        "objects"  | null                            || [] as Object[] // @ExcelColumn.defaultValue = "[]"
        "booleans" | "[false, true]"                 || [false, true] as boolean[]
        "bytes"    | "[-128, 0, 127]"                || [-128, 0, 127] as byte[]
        "shorts"   | "[-32768, 0, 32767]"            || [-32768, 0, 32767] as short[]
        "chars"    | "[a, B, 0, /]"                  || ['a', 'B', '0', '/'] as char[]
        "ints"     | "[74, 0, -12]"                  || [74, 0, -12] as int[]
        "longs"    | "[0, 9720, -8715]"              || [0, 9720, -8715] as long[]
        "floats"   | "[0.0, 9.745, -1.14157]"        || [0.0, 9.745, -1.14157] as float[]
        "doubles"  | "[3.141592, -0.0879, 0.0]"      || [3.141592, -0.0879, 0.0] as double[]
        "objects"  | "[]"                            || [] as Object[]
        "objects"  | "[, java.lang.Object@736e9adb]" || [null, null] as Object[]
        "strings"  | "[]"                            || [] as String[]
        "strings"  | "[, ]"                          || [null, null] as String[]
        "strings"  | "[alpha, beta]"                 || ["alpha", "beta"] as String[]
        "strings"  | "[alpha, , gamma]"              || ["alpha", null, "gamma"] as String[]
        "locales"  | "[]"                            || [] as Locale[]
        "locales"  | "[, ]"                          || [Locale.ROOT, Locale.ROOT] as Locale[]
        "locales"  | "[en_US, ko_KR, fr_FR]"         || [Locale.US, Locale.KOREA, Locale.FRANCE] as Locale[]
        "locales"  | "[en, ja, , ]"                  || [Locale.ENGLISH, Locale.JAPANESE, Locale.ROOT, Locale.ROOT] as Locale[]
    }

    def "Converts into 2D Array"() {
        given:
        def variables = [(fieldName): value]
        def field = Array2D.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.SETTER)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected

        where:
        fieldName  | value                               || expected
        "objects"  | null                                || null
        "booleans" | "[[false], [true], [false, true]]"  || [[false], [true], [false, true]] as boolean[][]
        "bytes"    | "[[-128], , [127]]"                 || [[-128], null, [127]] as byte[][]
        "shorts"   | "[[-32768, 0, 32767]]"              || [[-32768, 0, 32767]] as short[][]
        "chars"    | "[[a, B], [], [0, /]]"              || [['a', 'B'], [], ['0', '/']] as char[][]
        "ints"     | "[, [74, 0, -12]]"                  || [null, [74, 0, -12]] as int[][]
        "longs"    | "[[0], [], [9720, -8715]]"          || [[0], [], [9720, -8715]] as long[][]
        "floats"   | "[[0.0], [9.745, -1.14157]]"        || [[0.0], [9.745, -1.14157]] as float[][]
        "doubles"  | "[[3.141592, -0.0879, 0.0], ]"      || [[3.141592, -0.0879, 0.0], null] as double[][]
        "objects"  | "[]"                                || [] as Object[][]
        "objects"  | "[, [], ]"                          || [null, [], null] as Object[][]
        "objects"  | "[[java.lang.Object@736e9adb], []]" || [[null], []] as Object[][]
        "locales"  | "[]"                                || [] as Locale[][]
        "locales"  | "[[]]"                              || [[]] as Locale[][]
        "locales"  | "[[], []]"                          || [[], []] as Locale[][]
        "locales"  | "[[], , []]"                        || [[], null, []] as Locale[][]
        "locales"  | "[, [], [], ]"                      || [null, [], [], null] as Locale[][]
        "locales"  | "[, [], , [, ]]"                    || [null, [], null, [Locale.ROOT, Locale.ROOT]] as Locale[][]
        "locales"  | "[, [de_DE, zh_CN], [], ]"          || [null, [Locale.GERMANY, Locale.CHINA], [], null] as Locale[][]
        "locales"  | "[[en_GB], [], [it_IT], []]"        || [[Locale.UK], [], [Locale.ITALY], []] as Locale[][]
    }

    def "Converts into 3D Array"() {
        given:
        def variables = [(fieldName): value]
        def field = Array3D.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected

        where:
        fieldName  | value                                     || expected
        "objects"  | null                                      || null
        "booleans" | "[[], [[false], [true]], ]"               || [[], [[false], [true]], null] as boolean[][][]
        "bytes"    | "[, [[-128]], [[127]]]"                   || [null, [[-128]], [[127]]] as byte[][][]
        "shorts"   | "[[[-32768, 0, 32767]]]"                  || [[[-32768, 0, 32767]]] as short[][][]
        "chars"    | "[[[a], [], [B]], [], [[0, /]]]"          || [[['a'], [], ['B']], [], [['0', '/']]] as char[][][]
        "ints"     | "[, [, [74], [0, -12]]]"                  || [null, [null, [74], [0, -12]]] as int[][][]
        "longs"    | "[[[0], ], [], [[9720, -8715], ]]"        || [[[0], null], [], [[9720, -8715], null]] as long[][][]
        "floats"   | "[[[], [0.0]], [[9.745], [-1.14157]]]"    || [[[], [0.0]], [[9.745], [-1.14157]]] as float[][][]
        "doubles"  | "[[[3.141592, -0.0879], [0.0]], ]"        || [[[3.141592, -0.0879], [0.0]], null] as double[][][]
        "objects"  | "[]"                                      || [] as Object[][][]
        "objects"  | "[, [], [[]]]"                            || [null, [], [[]]] as Object[][][]
        "objects"  | "[, [[java.lang.Object@736e9adb, ]], []]" || [null, [[null, null]], []] as Object[][][]
        "locales"  | "[]"                                      || [] as Locale[][][]
        "locales"  | "[[]]"                                    || [[]] as Locale[][][]
        "locales"  | "[[], []]"                                || [[], []] as Locale[][][]
        "locales"  | "[[], , []]"                              || [[], null, []] as Locale[][][]
        "locales"  | "[, , , ]"                                || [null, null, null, null] as Locale[][][]
        "locales"  | "[, [[], []], [], ]"                      || [null, [[], []], [], null] as Locale[][][]
        "locales"  | "[[[en_US, en], [ko_KR, ko]]]"            || [[[Locale.US, Locale.ENGLISH], [Locale.KOREA, Locale.KOREAN]]] as Locale[][][]
        "locales"  | "[[, [, ], [ja_JP, zh_TW]], []]"          || [[null, [Locale.ROOT, Locale.ROOT], [Locale.JAPAN, Locale.TAIWAN]], []] as Locale[][][]
    }

    def "Converts into iterable"() {
        given:
        def variables = [(fieldName): value]
        def field = IterableModel.getDeclaredField(fieldName)
        def analyses = analyze(field.declaringClass.declaredFields, ExcelReadAnalyzer.FIELD_ACCESS)

        when:
        def converter = new ExcelReadHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(variables, field)

        then:
        actual == expected

        where:
        fieldName                      | value                                    || expected
        "iterable_string"              | null                                     || null
        "iterable_string"              | "[]"                                     || []
        "iterable_string"              | "[74, 0, -12]"                           || [74, 0, -12]
        "collection_long"              | "[0, 9720, -8715]"                       || [0, 9720, -8715]
        "collection_list_long"         | "[[]]"                                   || [[]]
        "collection_list_long"         | "[[241832184], , [748015106], []]"       || [[241832184], null, [748015106], []]
        "list_string"                  | "[alpha, beta]"                          || ["alpha", "beta"]
        "iterable_bigDecimal"          | "[-0.7215, 3.141592, ]"                  || [-0.7215, 3.141592, null]
        "list_iterable_string"         | "[[-9, -3], [-1, 0], [3, 9]]"            || [["-9", "-3"], ["-1", "0"], ["3", "9"]]
        "iterable_iterable_bigDecimal" | "[[9.155, 12.784, -0.019], [], [6.218]]" || [[9.155, 12.784, -0.019], [], [6.218]]
    }

    def "Converts into enum by custom handler"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()
        registry.add(new TimeUnitTypeHandler())
        def variables = [(fieldName): value]
        def field = EnumModel.getDeclaredField(fieldName)
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

    @EqualsAndHashCode
    private static class EnumModel {
        AccessMode accessMode
        TimeUnit timeUnit
    }

    private static class IterableModel<
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

}
