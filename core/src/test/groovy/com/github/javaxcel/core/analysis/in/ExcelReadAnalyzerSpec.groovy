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

package com.github.javaxcel.core.analysis.in

import spock.lang.Specification
import spock.lang.Subject

import com.github.javaxcel.core.annotation.ExcelReadExpression
import com.github.javaxcel.core.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.core.in.strategy.impl.UseSetters

import static com.github.javaxcel.core.analysis.in.ExcelReadAnalyzer.*

@Subject(ExcelReadAnalyzer)
class ExcelReadAnalyzerSpec extends Specification {

    def "Constraints for flag"() {
        given:
        def flags = [HANDLER, EXPRESSION, FIELD_ACCESS, SETTER].sort()

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

    // -------------------------------------------------------------------------------------------------

    def "Analyzes flags"() {
        given:
        def field = TestModel.getDeclaredField(fieldName)

        when:
        def analyzer = new ExcelReadAnalyzer(new DefaultExcelTypeHandlerRegistry())
        def actual = analyzer.analyzeFlags(field, arguments as Object[])

        then:
        def flags = expected.inject(0) { acc, cur -> acc | cur }
        actual == flags

        where:
        fieldName | arguments          || expected
        "integer" | []                 || [HANDLER, FIELD_ACCESS]
        "integer" | [new UseSetters()] || [HANDLER, FIELD_ACCESS]
        "decimal" | []                 || [EXPRESSION, FIELD_ACCESS]
        "decimal" | [new UseSetters()] || [EXPRESSION, SETTER]
        "strings" | []                 || [HANDLER, FIELD_ACCESS]
        "strings" | [new UseSetters()] || [HANDLER, SETTER]
    }

    // -------------------------------------------------------------------------------------------------

    private static class TestModel {
        Integer integer
        @ExcelReadExpression("new java.math.BigDecimal(#decimal)")
        BigDecimal decimal
        List<String> strings

        void setInteger(String integer) {
            this.integer = Integer.parseInt(integer.replace('$', ''))
        }

        void setStrings(List<String> strings) {
            this.strings = strings.collect { it.toLowerCase(Locale.US) }
        }
    }

}
