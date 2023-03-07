/*
 * Copyright 2023 Javaxcel
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

package com.github.javaxcel.core.analysis.out

import spock.lang.Specification
import spock.lang.Subject

import com.github.javaxcel.core.annotation.ExcelWriteExpression
import com.github.javaxcel.core.converter.handler.impl.lang.DoubleTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.IntegerTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.LongTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.StringTypeHandler
import com.github.javaxcel.core.converter.handler.impl.math.BigDecimalTypeHandler
import com.github.javaxcel.core.converter.handler.impl.math.BigIntegerTypeHandler
import com.github.javaxcel.core.converter.handler.impl.util.UUIDTypeHandler
import com.github.javaxcel.core.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.core.model.sample.ComplexSample
import com.github.javaxcel.core.model.sample.GenericSample
import com.github.javaxcel.core.model.sample.ModelSample
import com.github.javaxcel.core.model.sample.PlainSample
import com.github.javaxcel.core.out.strategy.impl.DefaultValue
import com.github.javaxcel.core.out.strategy.impl.UseGetters
import com.github.javaxcel.core.util.FieldUtils

import static com.github.javaxcel.core.analysis.ExcelAnalysis.DefaultMeta.Source.*
import static com.github.javaxcel.core.analysis.out.ExcelWriteAnalyzer.*

@Subject(ExcelWriteAnalyzer)
class ExcelWriteAnalyzerSpec extends Specification {

    def "Constraints for flag"() {
        given:
        def flags = [HANDLER, EXPRESSION, FIELD_ACCESS, GETTER].sort()

        expect: """
            1. All the flags are type of integer, not decimal.
            2. Each flag must be unique.
            3. The flags is a geometric sequence: a, ar, ar^2, ar^3, ... (a = 1, r = 2)
        """
        flags.grep(Integer).size() == flags.size()
        flags.unique() == flags
        for (def i = 0; i < flags.size() - 1; i++) {
            assert flags[i] * 2 == flags[i + 1]
        }
    }

    def "Checks default meta information of analyses"() {
        given:
        def fields = FieldUtils.getTargetedFields(type)

        when:
        def analyzer = new ExcelWriteAnalyzer(new DefaultExcelTypeHandlerRegistry())
        def analyses = analyzer.analyze(fields, arguments as Object[])

        then: """
            1. Result count of analyses is the same as count of the given fields.
            2. Each ExcelAnalysis.field is the same as the given fields.
            3. Each ExcelAnalysis.defaultMeta.value is equal to the expected.
            4. Each ExcelAnalysis.defaultMeta.source is equal to the expected.
        """
        analyses.size() == fields.size()
        analyses*.field == fields
        analyses*.defaultMeta*.value == values
        analyses*.defaultMeta*.source == sources

        where:
        type        | arguments               || values                               | sources
        PlainSample | [new UseGetters()]      || [null, "0.00", null]                 | [NONE, COLUMN, NONE]
        PlainSample | [new DefaultValue("-")] || ["-", "-", "-"]                      | [OPTION] * values.size()
        ModelSample | []                      || ["(empty)", "none", "(empty)", "[]"] | [MODEL, COLUMN, MODEL, COLUMN]
        ModelSample | [new DefaultValue("-")] || ["-", "-", "-", "-"]                 | [OPTION] * values.size()
    }

    def "Analyzes fields of non-randomized model"() {
        given:
        def fields = FieldUtils.getTargetedFields(type)

        when:
        def analyzer = new ExcelWriteAnalyzer(new DefaultExcelTypeHandlerRegistry())
        def analyses = analyzer.analyze(fields)

        then: "Field, value and handler of analysis is equal to the expected"
        analyses.size() == fields.size()
        analyses*.handler*.class == expected

        where:
        type          | expected
        PlainSample   | [LongTypeHandler, BigDecimalTypeHandler, StringTypeHandler]
        ModelSample   | [IntegerTypeHandler, StringTypeHandler, BigIntegerTypeHandler, StringTypeHandler]
        GenericSample | [LongTypeHandler, StringTypeHandler, BigDecimalTypeHandler, StringTypeHandler, StringTypeHandler, StringTypeHandler, LongTypeHandler]
        ComplexSample | [LongTypeHandler, null, null, null, null, null, null, UUIDTypeHandler, UUIDTypeHandler,
                         UUIDTypeHandler, DoubleTypeHandler, DoubleTypeHandler, DoubleTypeHandler, null, null,
                         LongTypeHandler, LongTypeHandler, null, null, LongTypeHandler, LongTypeHandler, null, null,
                         null, null, null, null, null, null, UUIDTypeHandler, UUIDTypeHandler, UUIDTypeHandler,
                         UUIDTypeHandler, UUIDTypeHandler, UUIDTypeHandler, DoubleTypeHandler, null]
    }

    // -------------------------------------------------------------------------------------------------

    def "Analyzes flags"() {
        given:
        def field = TestModel.getDeclaredField(fieldName)

        when:
        def analyzer = new ExcelWriteAnalyzer(new DefaultExcelTypeHandlerRegistry())
        def actual = analyzer.analyzeFlags(field, arguments as Object[])

        then:
        def flags = expected.inject(0) { acc, cur -> acc | cur }
        actual == flags

        where:
        fieldName | arguments          || expected
        "integer" | []                 || [HANDLER, FIELD_ACCESS]
        "integer" | [new UseGetters()] || [HANDLER, FIELD_ACCESS]
        "decimal" | []                 || [EXPRESSION, FIELD_ACCESS]
        "decimal" | [new UseGetters()] || [EXPRESSION, GETTER]
        "strings" | []                 || [HANDLER, FIELD_ACCESS]
        "strings" | [new UseGetters()] || [HANDLER, GETTER]
    }

    // -------------------------------------------------------------------------------------------------

    private static class TestModel {
        Integer integer
        @ExcelWriteExpression("#decimal")
        BigDecimal decimal
        List<String> strings

        String getInteger() {
            "$integer"
        }

        List<String> getStrings() {
            strings.collect { it.toUpperCase(Locale.US) }
        }
    }

}
