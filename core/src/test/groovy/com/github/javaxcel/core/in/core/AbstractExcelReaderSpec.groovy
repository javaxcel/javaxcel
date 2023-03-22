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

package com.github.javaxcel.core.in.core

import spock.lang.Specification
import spock.lang.Subject

import org.apache.poi.ss.usermodel.Workbook

import com.github.javaxcel.core.in.context.ExcelReadContext
import com.github.javaxcel.core.in.core.impl.ModelReader
import com.github.javaxcel.core.in.strategy.impl.Limit
import com.github.javaxcel.core.in.strategy.impl.Parallel
import com.github.javaxcel.core.in.strategy.impl.UseSetters

@Subject(AbstractExcelReader)
class AbstractExcelReaderSpec extends Specification {

    def "Sets no option"() {
        given:
        def context = createContext()
        def reader = createReader(context)

        when:
        reader.options()

        then:
        context.strategyMap.isEmpty()
    }

    def "Sets options"() {
        given:
        def context = createContext()
        def reader = createReader(context)

        when:
        reader.options(new Limit(10), new Parallel(), new UseSetters())

        then:
        context.strategyMap.size() == 3
    }

    def "Sets one of duplicated options"() {
        given:
        def context = createContext()
        def reader = createReader(context)

        when:
        reader.options(new Parallel(), new Limit(5), new Parallel(), new Limit(10))

        then:
        context.strategyMap.size() == 2
    }

    def "Discards previous options"() {
        given:
        def context = createContext()
        def reader = createReader(context)

        when:
        reader.options(new Parallel(), new Limit(5), new UseSetters(), new Limit(10))

        then:
        context.strategyMap.size() == 3

        when:
        reader.options(new UseSetters())

        then:
        context.strategyMap.size() == 1
    }

    // -------------------------------------------------------------------------------------------------

    private ExcelReadContext createContext(Class modelType = Object, Class<? extends ExcelReader> readerType = ModelReader) {
        new ExcelReadContext<>(Mock(Workbook), modelType, readerType)
    }

    private AbstractExcelReader createReader(ExcelReadContext context) {
        Spy(AbstractExcelReader, constructorArgs: [context]) as AbstractExcelReader
    }

}
