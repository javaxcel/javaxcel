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

import com.github.javaxcel.core.out.strategy.impl.DefaultValue
import com.github.javaxcel.core.out.strategy.impl.KeyNames

@Subject(MapDescriptorFactory)
class MapDescriptorFactorySpec extends Specification {

    def "forWrite collects keys from list with no strategies"() {
        given:
        def list = [
                [a: 1, b: 2],
                [b: 3, c: 4],
        ]

        when:
        def descriptors = MapDescriptorFactory.forWrite(list, [:])

        then:
        descriptors.collect { it.name() } == ["a", "b", "c"]
        descriptors.every { !it.bodyStyle().isPresent() }
    }

    def "forWrite applies KeyNames.orders to reorder columns"() {
        given:
        def list = [[width: 1, depth: 2, height: 3]]
        def keyNames = new KeyNames(["depth", "width", "height"])

        when:
        def descriptors = MapDescriptorFactory.forWrite(
                list, [(KeyNames): keyNames])

        then:
        descriptors.collect { it.name() } == ["depth", "width", "height"]
        descriptors.collect { it.fieldKey() } == ["depth", "width", "height"]
    }

    def "forWrite applies KeyNames.names to rename columns"() {
        given:
        def list = [[width: 1, depth: 2, height: 3]]
        def keyNames = new KeyNames(["width", "depth", "height"], ["WIDTH", "DEPTH", "HEIGHT"])

        when:
        def descriptors = MapDescriptorFactory.forWrite(
                list, [(KeyNames): keyNames])

        then:
        descriptors.collect { it.name() } == ["WIDTH", "DEPTH", "HEIGHT"]
        descriptors.collect { it.fieldKey() } == ["width", "depth", "height"]
    }

    def "forWrite applies DefaultValue strategy for null/empty values"() {
        given:
        def list = [[a: null]]
        def defaultValue = new DefaultValue("--")

        when:
        def descriptors = MapDescriptorFactory.forWrite(
                list, [(DefaultValue): defaultValue])

        then:
        descriptors.size() == 1
        descriptors[0].writeValue([a: null] as Map<String, Object>) == "--"
    }

    def "forWrite rejects empty key set"() {
        when:
        MapDescriptorFactory.forWrite([[:] as Map<String, Object>], [:])

        then:
        thrown(IllegalArgumentException)
    }

    def "forRead returns one descriptor per header name, treating header as both name and key"() {
        when:
        def descriptors = MapDescriptorFactory.forRead(["alpha", "beta", "gamma"])

        then:
        descriptors.collect { it.name() } == ["alpha", "beta", "gamma"]
        descriptors.collect { it.fieldKey() } == ["alpha", "beta", "gamma"]
        descriptors[0].readValue([alpha: "x"] as Map<String, String>) == "x"
    }

    def "forRead descriptor throws when writeValue is invoked"() {
        given:
        def descriptors = MapDescriptorFactory.forRead(["k"])

        when:
        descriptors[0].writeValue([k: "x"] as Map<String, String>)

        then:
        thrown(UnsupportedOperationException)
    }

}
