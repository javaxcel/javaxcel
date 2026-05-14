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

@Subject(ColumnDescriptor)
class ColumnDescriptorSpec extends Specification {

    def "default methods return empty values"() {
        given:
        def descriptor = new ColumnDescriptor<String>() {
            @Override
            String name() { "header" }

            @Override
            Object readValue(Map<String, String> row) { row.get("header") }

            @Override
            String writeValue(String model) { model }
        }

        expect:
        descriptor.name() == "header"
        descriptor.fieldKey() == "header"
        !descriptor.isFinalField()
        !descriptor.headerStyle().isPresent()
        !descriptor.bodyStyle().isPresent()
        descriptor.validators().isEmpty()
        !descriptor.dropdownItems().isPresent()
    }

    def "readValue and writeValue delegate to overridden methods"() {
        given:
        def descriptor = new ColumnDescriptor<Integer>() {
            @Override
            String name() { "n" }

            @Override
            Object readValue(Map<String, String> row) {
                Integer.parseInt(row.get("n"))
            }

            @Override
            String writeValue(Integer model) { model.toString() }
        }

        expect:
        descriptor.readValue([n: "42"]) == 42
        descriptor.writeValue(7) == "7"
    }

}
