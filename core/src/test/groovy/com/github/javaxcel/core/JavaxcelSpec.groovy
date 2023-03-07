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

package com.github.javaxcel.core

import spock.lang.Specification
import spock.lang.Subject

import com.github.javaxcel.core.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.core.converter.handler.registry.impl.ExcelTypeHandlerRegistryImpl
import com.github.javaxcel.test.converter.handler.impl.TimeUnitTypeHandler

@Subject(Javaxcel)
class JavaxcelSpec extends Specification {

    def "Creates an instance"() {
        given:
        def defaultRegistry = new DefaultExcelTypeHandlerRegistry()

        when:
        def javaxcel = Javaxcel.newInstance()

        then:
        javaxcel.@registry.allTypes == defaultRegistry.allTypes
    }

    def "Creates an instance with registry of handlers"() {
        given:
        def registry = new ExcelTypeHandlerRegistryImpl()
        registry.add(new TimeUnitTypeHandler())

        when:
        def javaxcel = Javaxcel.newInstance(registry)

        then:
        javaxcel.@registry.allTypes.size() == 1
    }

}
