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

import spock.lang.Specification
import spock.lang.Subject

import java.lang.reflect.Method

import com.github.javaxcel.core.annotation.ExcelModelCreator
import com.github.javaxcel.core.in.resolver.impl.ExcelModelMethodResolver

@Subject(ExcelModelMethodResolver)
class NoParamSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelMethodResolver<>(TestModel)

        when:
        def method = resolver.resolve()

        then:
        noExceptionThrown()
        method != null
        method instanceof Method
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class TestModel {
        static final TestModel INSTANCE = new TestModel()

        @ExcelModelCreator
        static TestModel getInstance() {
            INSTANCE
        }
    }

}
