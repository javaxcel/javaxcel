/*
 * Copyright 2022 Javaxcel
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

package com.github.javaxcel.core.in.resolver.impl.method.success

import com.github.javaxcel.core.annotation.ExcelModelCreator
import com.github.javaxcel.core.in.resolver.impl.ExcelModelMethodResolver
import io.github.imsejin.common.tool.RandomString
import spock.lang.Specification

import java.lang.reflect.Method

class ManyMethodsFromParentsSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelMethodResolver<>(type)

        when:
        def method = resolver.resolve()

        then:
        noExceptionThrown()
        method != null
        method instanceof Method

        where:
        type << [Parent, Child]
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class GrandParent {
        @ExcelModelCreator
        static GrandParent randomGrandParent() {
            new GrandParent()
        }

        @ExcelModelCreator
        static Parent randomParent() {
            new Parent(new RandomString().nextString(8))
        }

        @ExcelModelCreator
        static Child randomChild() {
            new Child(new RandomString().nextString(16))
        }
    }

    @SuppressWarnings("unused")
    private static class Parent extends GrandParent {
        final String name

        private Parent(String name) {
            this.name = name
        }

        @ExcelModelCreator
        static Child fromChild(String name) {
            Child.from(name)
        }
    }

    @SuppressWarnings("unused")
    private static class Child extends Parent {
        final String nickname

        private Child(String name) {
            super(name)
            this.nickname = name.toUpperCase(Locale.US)
        }

        @ExcelModelCreator
        static Child from(String name) {
            new Child(name)
        }
    }

}
