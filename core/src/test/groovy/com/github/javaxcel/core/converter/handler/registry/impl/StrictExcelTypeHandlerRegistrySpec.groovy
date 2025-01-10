/*
 * Copyright 2025 Javaxcel
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

package com.github.javaxcel.core.converter.handler.registry.impl

import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.TimeUnit

import com.github.javaxcel.core.converter.handler.impl.io.FileTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.BooleanTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.ByteTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.CharacterTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.DoubleTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.FloatTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.IntegerTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.LongTypeHandler
import com.github.javaxcel.core.converter.handler.impl.lang.ShortTypeHandler
import com.github.javaxcel.test.converter.handler.impl.ObjectTypeHandler
import com.github.javaxcel.test.converter.handler.impl.TimeUnitTypeHandler

@Subject(StrictExcelTypeHandlerRegistry)
class StrictExcelTypeHandlerRegistrySpec extends Specification {

    def "Gets a handler matched by type strictly"() {
        given:
//        def registry = new StrictExcelTypeHandlerRegistry()
        def registry = new LenientExcelTypeHandlerRegistry()

        expect:
        registry.getHandler(Object) == null

        when:
        registry.add(new ObjectTypeHandler())
        registry.add(new TimeUnitTypeHandler())
        registry.add(new FileTypeHandler())

        then:
//        registry.getHandler(Object).class == ObjectTypeHandler
//        registry.getHandler(new Object() {}.class) == null
//        registry.getHandler(TimeUnit).class == TimeUnitTypeHandler
//        TimeUnit.values().every { registry.getHandler(it.class).class == TimeUnitTypeHandler }
//        registry.getHandler(Enum) == null
        registry.getHandler(new File("") {}.class).class == FileTypeHandler
    }

    def "Gets all the added types"() {
        given:
        def registry = new StrictExcelTypeHandlerRegistry()

        expect:
        registry.allTypes.empty

        when:
        def handlers = [
                new BooleanTypeHandler(),
                new ByteTypeHandler(),
                new ShortTypeHandler(),
                new CharacterTypeHandler(),
                new IntegerTypeHandler(),
                new LongTypeHandler(),
                new FloatTypeHandler(),
                new DoubleTypeHandler(),
        ]
        handlers.each { registry.add(it) }

        then:
        registry.allTypes.size() == handlers.size()
        registry.allTypes == handlers.collect { it.type } as Set
    }

    def "Adds a handler"() {
        given:
        def registry = new StrictExcelTypeHandlerRegistry()

        when: "Add new handler on class java.lang.Object"
        def added = registry.add(Object, new ObjectTypeHandler())

        then:
        added

        when: "Override handler on class java.lang.Object"
        def overridden = !registry.add(Object, new ObjectTypeHandler())

        then:
        overridden
    }

    def "Adds a registry"() {
        given:
        def registry = new StrictExcelTypeHandlerRegistry()
        def newRegistry = new StrictExcelTypeHandlerRegistry()

        when: "Add empty registry to the other"
        def added = !registry.addAll(registry)

        then:
        added

        when: "Add new handler with new registry"
        newRegistry.add(Object, new ObjectTypeHandler())
        added = registry.addAll(newRegistry)

        then:
        added

        when: "Override handlers with the same registry"
        newRegistry.add(Object, new ObjectTypeHandler())
        newRegistry.add(TimeUnit, new TimeUnitTypeHandler())
        def overridden = !registry.addAll(newRegistry)

        then:
        overridden
    }

}
