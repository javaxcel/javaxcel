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

package com.github.javaxcel.core.out.core

import spock.lang.Specification
import spock.lang.Subject

import org.apache.poi.ss.usermodel.Workbook

import com.github.javaxcel.core.out.context.ExcelWriteContext
import com.github.javaxcel.core.out.core.impl.ModelWriter
import com.github.javaxcel.core.out.strategy.impl.EnumDropdown
import com.github.javaxcel.core.out.strategy.impl.Filter
import com.github.javaxcel.core.out.strategy.impl.HiddenExtraColumns
import com.github.javaxcel.core.out.strategy.impl.HiddenExtraRows
import com.github.javaxcel.core.out.strategy.impl.UseGetters

@Subject(AbstractExcelWriter)
class AbstractExcelWriterSpec extends Specification {

    def "Sets no option"() {
        given:
        def context = new ExcelWriteContext<>(Mock(Workbook), Object, ModelWriter)
        def writer = Spy(AbstractExcelWriter, constructorArgs: [context]) as AbstractExcelWriter

        when:
        writer.options()

        then:
        context.strategyMap.isEmpty()
    }

    def "Sets options"() {
        given:
        def context = new ExcelWriteContext<>(Mock(Workbook), Object, ModelWriter)
        def writer = Spy(AbstractExcelWriter, constructorArgs: [context]) as AbstractExcelWriter

        when:
        writer.options(new UseGetters(), new HiddenExtraColumns(), new HiddenExtraRows())

        then:
        context.strategyMap.size() == 3
    }

    def "Sets one of duplicated options"() {
        given:
        def context = new ExcelWriteContext<>(Mock(Workbook), Object, ModelWriter)
        def writer = Spy(AbstractExcelWriter, constructorArgs: [context]) as AbstractExcelWriter

        when:
        writer.options(new EnumDropdown(), new Filter(false), new EnumDropdown(), new Filter(true))

        then:
        context.strategyMap.size() == 2
    }

    def "Discards previous options"() {
        given:
        def context = new ExcelWriteContext<>(Mock(Workbook), Object, ModelWriter)
        def writer = Spy(AbstractExcelWriter, constructorArgs: [context]) as AbstractExcelWriter

        when:
        writer.options(new EnumDropdown(), new Filter(false), new UseGetters(), new Filter(true))

        then:
        context.strategyMap.size() == 3

        when:
        writer.options(new HiddenExtraRows())

        then:
        context.strategyMap.size() == 1
    }

}
