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

import com.github.javaxcel.core.annotation.ExcelColumn
import com.github.javaxcel.core.annotation.ExcelModel
import com.github.javaxcel.core.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.core.exception.NoTargetedFieldException
import com.github.javaxcel.styler.ExcelStyleConfig
import com.github.javaxcel.styler.NoStyleConfig
import com.github.javaxcel.styler.config.Configurer

@Subject(ModelDescriptorFactory)
class ModelDescriptorFactorySpec extends Specification {

    def "forRead creates descriptors using field names as headers"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()

        when:
        def descriptors = ModelDescriptorFactory.forRead(BasicModel, registry, [:])

        then:
        descriptors.collect { it.name() } == ["name", "age"]
        descriptors.collect { it.fieldKey() } == ["name", "age"]
    }

    def "forWrite uses @ExcelColumn.name when present"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()

        when:
        def descriptors = ModelDescriptorFactory.forWrite(NamedColumnModel, registry, [:])

        then:
        descriptors.collect { it.name() } == ["FullName", "age"]
    }

    def "forWrite shares @ExcelModel-derived header style across columns (single instance)"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()

        when:
        def descriptors = ModelDescriptorFactory.forWrite(ModelStyleOnly, registry, [:])

        then: "All descriptors carry the SAME ExcelStyleConfig instance — preserves legacy CellStyle dedup"
        def first = descriptors[0].headerStyle().get()
        descriptors.every { it.headerStyle().get().is(first) }
    }

    def "forWrite produces distinct ExcelStyleConfig instances for each @ExcelColumn override"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()

        when:
        def descriptors = ModelDescriptorFactory.forWrite(ColumnStyleOnly, registry, [:])

        then:
        def style0 = descriptors[0].headerStyle().get()
        def style1 = descriptors[1].headerStyle().get()
        !style0.is(style1)
    }

    def "forWrite throws NoTargetedFieldException when class has no targeted fields"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()

        when:
        ModelDescriptorFactory.forWrite(NoFieldsModel, registry, [:])

        then:
        thrown(NoTargetedFieldException)
    }

    def "forWrite skips fields annotated with @ExcelColumn(ignored = true)"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()

        when:
        def descriptors = ModelDescriptorFactory.forWrite(IgnoredFieldModel, registry, [:])

        then: "Only the non-ignored 'name' field is included"
        descriptors.size() == 1
        descriptors[0].name() == "name"
    }

    def "forRead skips fields annotated with @ExcelColumn(ignored = true)"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()

        when:
        def descriptors = ModelDescriptorFactory.forRead(IgnoredFieldModel, registry, [:])

        then:
        descriptors.size() == 1
        descriptors[0].name() == "name"
    }

    def "forWrite respects @ExcelModel(onlyExplicitlyAnnotated = true) and only includes annotated fields"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()

        when:
        def descriptors = ModelDescriptorFactory.forWrite(ExplicitOnlyModel, registry, [:])

        then: "Only the explicitly @ExcelColumn-annotated 'tag' field is included"
        descriptors.size() == 1
        descriptors[0].name() == "tag"
    }

    // -------------------------------------------------------------------------------------------------

    private static class BasicModel {
        private String name
        private int age
    }

    private static class NamedColumnModel {
        @ExcelColumn(name = "FullName")
        private String name
        private int age
    }

    @ExcelModel(headerStyle = TestHeaderStyle, bodyStyle = TestBodyStyle)
    private static class ModelStyleOnly {
        private String name
        private int age
        private long id
    }

    private static class ColumnStyleOnly {
        @ExcelColumn(headerStyle = TestHeaderStyle)
        private String name
        @ExcelColumn(headerStyle = TestHeaderStyle)
        private int age
    }

    private static class NoFieldsModel {
    }

    private static class IgnoredFieldModel {
        private String name
        @ExcelColumn(ignored = true)
        private String secret
    }

    @ExcelModel(onlyExplicitlyAnnotated = true)
    private static class ExplicitOnlyModel {
        private String name
        @ExcelColumn
        private String tag
    }

    static class TestHeaderStyle implements ExcelStyleConfig {
        @Override
        void configure(Configurer configurer) {}
    }

    static class TestBodyStyle implements ExcelStyleConfig {
        @Override
        void configure(Configurer configurer) {}
    }

}
