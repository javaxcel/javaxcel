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

package com.github.javaxcel.core.in.core

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

import org.apache.poi.xssf.streaming.SXSSFWorkbook

import com.github.pjfanning.xlsx.StreamingReader

import com.github.javaxcel.core.Javaxcel
import com.github.javaxcel.core.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.core.in.strategy.impl.KeyNames
import com.github.javaxcel.core.in.strategy.impl.Limit
import com.github.javaxcel.core.in.strategy.impl.Parallel

@Subject(DefaultExcelReader)
class DefaultExcelReaderSpec extends Specification {

    @TempDir
    private Path path

    def "forModel creates reader bound to a model class"() {
        given:
        def workbook = new SXSSFWorkbook()
        def registry = new DefaultExcelTypeHandlerRegistry()

        when:
        def reader = DefaultExcelReader.forModel(workbook, Person, registry)

        then:
        reader != null
        reader.getContext().getModelType() == Person
    }

    def "forMap creates reader bound to Map type"() {
        given:
        def workbook = new SXSSFWorkbook()

        when:
        def reader = DefaultExcelReader.forMap(workbook)

        then:
        reader != null
        reader.getContext().getModelType() == Map
    }

    def "options() deduplicates strategies by class"() {
        given:
        def workbook = new SXSSFWorkbook()
        def reader = DefaultExcelReader.forModel(workbook, Person, new DefaultExcelTypeHandlerRegistry())

        when:
        reader.options(new Limit(5), new Limit(10), new Parallel())

        then:
        def map = reader.getContext().getStrategyMap()
        map.size() == 2
        map.containsKey(Limit)
        map.containsKey(Parallel)
    }

    def "Map mode strategies for Model are filtered out"() {
        given:
        def workbook = new SXSSFWorkbook()
        def reader = DefaultExcelReader.forModel(workbook, Person, new DefaultExcelTypeHandlerRegistry())

        when:
        reader.options(new KeyNames(["a", "b"]))  // KeyNames is for Map mode only

        then:
        reader.getContext().getStrategyMap().isEmpty()
    }

    def "round-trip — write Person list and read back"() {
        given:
        def filePath = path.resolve("default-reader.xlsx")
        def people = [
                new Person(name: "Alice", age: 30),
                new Person(name: "Bob", age: 40),
        ]

        and:
        Javaxcel.newInstance()
                .writer(new SXSSFWorkbook(), Person)
                .write(Files.newOutputStream(filePath), people)

        when:
        def actual = Javaxcel.newInstance()
                .reader(StreamingReader.builder().open(Files.newInputStream(filePath)), Person)
                .read()

        then:
        actual.size() == 2
        actual[0].name == "Alice"
        actual[0].age == 30
        actual[1].name == "Bob"
        actual[1].age == 40
    }

    // -------------------------------------------------------------------------------------------------

    private static class Person {
        private String name
        private int age
    }

}
