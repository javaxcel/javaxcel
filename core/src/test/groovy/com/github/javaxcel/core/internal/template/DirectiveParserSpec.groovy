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

package com.github.javaxcel.core.internal.template

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import org.apache.poi.ss.util.CellAddress

@Subject(DirectiveParser)
class DirectiveParserSpec extends Specification {

    def "Returns empty optional for null, blank, or non-jxc text"() {
        expect:
        !DirectiveParser.parse(input).isPresent()

        where:
        input << [null, "", "   ", "Hello world", "each items as item", "// comment"]
    }

    @Unroll
    def "Parses 'jxc: each #input' as Each(collection=#collection, var=#var, until=#untilRef)"() {
        when:
        def spec = DirectiveParser.parse("jxc: each $input").orElseThrow()

        then:
        spec instanceof DirectiveSpec.Each
        def each = spec as DirectiveSpec.Each
        each.collectionExpr() == collection
        each.varName() == var
        each.untilAddress() == (untilRef == null ? null : new CellAddress(untilRef))

        where:
        input                                | collection         | var      | untilRef
        "items as item"                      | "items"            | "item"   | null
        "items as item, until: C5"           | "items"            | "item"   | "C5"
        "item.children as child"             | "item.children"    | "child"  | null
        "items.![name] as name"              | "items.![name]"    | "name"   | null
        "items as it , until : AA10"         | "items"            | "it"     | "AA10"
    }

    def "Parses 'jxc: if <expr>' as If"() {
        when:
        def spec = DirectiveParser.parse("jxc: if items.size() > 0").orElseThrow()

        then:
        spec instanceof DirectiveSpec.If
        (spec as DirectiveSpec.If).conditionExpr() == "items.size() > 0"
    }

    def "Is case-insensitive on the jxc: prefix"() {
        expect:
        DirectiveParser.parse("JXC: each items as item").isPresent()
        DirectiveParser.parse("Jxc: if true").isPresent()
    }

    def "Throws IllegalArgumentException for malformed each"() {
        when:
        DirectiveParser.parse("jxc: each items")  // missing 'as <var>'

        then:
        thrown(IllegalArgumentException)
    }

    def "Throws IllegalArgumentException for unknown command"() {
        when:
        DirectiveParser.parse("jxc: foreach items")

        then:
        thrown(IllegalArgumentException)
    }

    def "Throws IllegalArgumentException for empty if body"() {
        when:
        DirectiveParser.parse("jxc: if ")

        then:
        thrown(IllegalArgumentException)
    }

    def "Throws IllegalArgumentException for empty directive body"() {
        when:
        DirectiveParser.parse("jxc:   ")

        then:
        thrown(IllegalArgumentException)
    }

}
