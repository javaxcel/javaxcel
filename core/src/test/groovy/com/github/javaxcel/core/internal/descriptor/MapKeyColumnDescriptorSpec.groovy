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

@Subject(MapKeyColumnDescriptor)
class MapKeyColumnDescriptorSpec extends Specification {

    def "name returns displayName, fieldKey returns underlying key"() {
        given:
        def descriptor = new MapKeyColumnDescriptor("internal_key", "Display Name", null)

        expect:
        descriptor.name() == "Display Name"
        descriptor.fieldKey() == "internal_key"
    }

    def "readValue looks up by underlying key"() {
        given:
        def descriptor = new MapKeyColumnDescriptor("k", "K", null)

        expect:
        descriptor.readValue([k: "value"]) == "value"
        descriptor.readValue([:]) == null
    }

    def "writeValue stringifies the model value"() {
        given:
        def descriptor = new MapKeyColumnDescriptor("age", "AGE", null)

        expect:
        descriptor.writeValue([age: 25] as Map<String, Object>) == "25"
    }

    def "writeValue uses defaultValue when value is null or empty"() {
        given:
        def descriptor = new MapKeyColumnDescriptor("k", "K", "DEFAULT")

        expect:
        descriptor.writeValue([k: null] as Map<String, Object>) == "DEFAULT"
        descriptor.writeValue([k: ""] as Map<String, Object>) == "DEFAULT"
        descriptor.writeValue([:] as Map<String, Object>) == "DEFAULT"
        descriptor.writeValue([k: "actual"] as Map<String, Object>) == "actual"
    }

    def "writeValue returns null when value is missing and no defaultValue"() {
        given:
        def descriptor = new MapKeyColumnDescriptor("k", "K", null)

        expect:
        descriptor.writeValue([k: null] as Map<String, Object>) == null
        descriptor.writeValue([:] as Map<String, Object>) == null
    }

    def "Style and dropdown defaults are empty"() {
        given:
        def descriptor = new MapKeyColumnDescriptor("k", "K", null)

        expect:
        !descriptor.headerStyle().isPresent()
        !descriptor.bodyStyle().isPresent()
        descriptor.validators().isEmpty()
        !descriptor.dropdownItems().isPresent()
        !descriptor.isFinalField()
    }

    def "Constructor rejects null or empty key"() {
        when:
        new MapKeyColumnDescriptor(null, "X", null)
        then:
        thrown(IllegalArgumentException)

        when:
        new MapKeyColumnDescriptor("", "X", null)
        then:
        thrown(IllegalArgumentException)
    }

}
