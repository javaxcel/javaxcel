/*
 * Copyright 2026 Javaxcel
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

package com.github.javaxcel.core.internal.descriptor

import spock.lang.Specification
import spock.lang.Subject

import org.apache.poi.ss.usermodel.Workbook

import com.github.javaxcel.core.in.context.ExcelReadContext
import com.github.javaxcel.core.in.core.impl.DefaultExcelReader
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy
import com.github.javaxcel.core.in.strategy.impl.Limit
import com.github.javaxcel.core.in.strategy.impl.Parallel
import com.github.javaxcel.core.out.context.ExcelWriteContext
import com.github.javaxcel.core.out.core.impl.DefaultExcelWriter
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy
import com.github.javaxcel.core.out.strategy.impl.AutoResizedColumns
import com.github.javaxcel.core.out.strategy.impl.KeyNames
import com.github.javaxcel.core.out.strategy.impl.SheetName

@Subject(StrategyDedup)
class StrategyDedupSpec extends Specification {

    def "Empty array yields empty map for read strategies"() {
        given:
        def context = new ExcelReadContext<>(Mock(Workbook), String, DefaultExcelReader)

        when:
        def result = StrategyDedup.collect(new ExcelReadStrategy[0], context)

        then:
        result.isEmpty()
    }

    def "Read strategies are deduplicated by class name"() {
        given:
        def context = new ExcelReadContext<>(Mock(Workbook), String, DefaultExcelReader)
        def strategies = [new Limit(5), new Limit(10), new Parallel()] as ExcelReadStrategy[]

        when:
        def result = StrategyDedup.collect(strategies, context)

        then:
        result.size() == 2
        result.containsKey(Limit)
        result.containsKey(Parallel)
    }

    def "Unsupported read strategies are dropped"() {
        given:
        // Map mode (modelType = Map) — Parallel.isSupported returns false.
        def context = new ExcelReadContext<>(Mock(Workbook), Map, DefaultExcelReader)
        def strategies = [new Limit(3), new Parallel()] as ExcelReadStrategy[]

        when:
        def result = StrategyDedup.collect(strategies, context)

        then:
        result.size() == 1
        result.containsKey(Limit)
        !result.containsKey(Parallel)
    }

    def "Empty array yields empty map for write strategies"() {
        given:
        def context = new ExcelWriteContext<>(Mock(Workbook), String, DefaultExcelWriter)

        when:
        def result = StrategyDedup.collect(new ExcelWriteStrategy[0], context)

        then:
        result.isEmpty()
    }

    def "Write strategies are deduplicated by class name"() {
        given:
        def context = new ExcelWriteContext<>(Mock(Workbook), String, DefaultExcelWriter)
        def strategies = [new SheetName("first"), new SheetName("second"), new AutoResizedColumns(true)] as ExcelWriteStrategy[]

        when:
        def result = StrategyDedup.collect(strategies, context)

        then:
        result.size() == 2
        result.containsKey(SheetName)
        result.containsKey(AutoResizedColumns)
    }

    def "Null array throws IllegalArgumentException"() {
        given:
        def context = new ExcelReadContext<>(Mock(Workbook), String, DefaultExcelReader)

        when:
        StrategyDedup.collect((ExcelReadStrategy[]) null, context)

        then:
        thrown(IllegalArgumentException)
    }

    def "Array with null element throws IllegalArgumentException"() {
        given:
        def context = new ExcelReadContext<>(Mock(Workbook), String, DefaultExcelReader)
        def strategies = [new Limit(5), null] as ExcelReadStrategy[]

        when:
        StrategyDedup.collect(strategies, context)

        then:
        thrown(IllegalArgumentException)
    }

    def "Unsupported write strategies are dropped"() {
        given:
        // Model mode (modelType = String) — KeyNames write strategy supports Map only.
        def context = new ExcelWriteContext<>(Mock(Workbook), String, DefaultExcelWriter)
        def strategies = [new SheetName("first"),
                          new KeyNames(["a", "b"])] as ExcelWriteStrategy[]

        when:
        def result = StrategyDedup.collect(strategies, context)

        then:
        result.size() == 1
        result.containsKey(SheetName)
        !result.containsKey(KeyNames)
    }

}
