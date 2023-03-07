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

package com.github.javaxcel.core.in.resolver.impl.constructor.success

import spock.lang.Specification
import spock.lang.Subject

import java.lang.reflect.Constructor

import com.github.javaxcel.core.in.resolver.impl.ExcelModelConstructorResolver

@Subject(ExcelModelConstructorResolver)
class DifferentParamOrderFromFieldSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(TestModel)

        when:
        def constructor = resolver.resolve()

        then:
        noExceptionThrown()
        constructor != null
        constructor instanceof Constructor
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class TestModel {
        final Integer id
        final String title
        final URL url

        TestModel(String title, URL url, Integer id) {
            this.id = id
            this.title = title
            this.url = url
        }
    }

}
