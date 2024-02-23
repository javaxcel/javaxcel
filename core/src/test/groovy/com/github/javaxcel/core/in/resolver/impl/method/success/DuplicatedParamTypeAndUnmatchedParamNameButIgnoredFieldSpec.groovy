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

import com.github.javaxcel.core.annotation.ExcelColumn
import com.github.javaxcel.core.annotation.ExcelModel
import com.github.javaxcel.core.annotation.ExcelModelCreator
import com.github.javaxcel.core.in.resolver.impl.ExcelModelMethodResolver

@Subject(ExcelModelMethodResolver)
class DuplicatedParamTypeAndUnmatchedParamNameButIgnoredFieldSpec extends Specification {

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
        type << [IgnoredField, ExplicitField]
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class IgnoredField {
        final List<Integer> integers
        @ExcelColumn(ignored = true)
        final List<String> strings

        IgnoredField(List<Integer> integers, List<String> strings) {
            this.integers = integers
            this.strings = strings
        }

        @ExcelModelCreator
        static IgnoredField of(List<Integer> numbers) {
            new IgnoredField(numbers, Collections.emptyList())
        }
    }

    @SuppressWarnings("unused")
    @ExcelModel(onlyExplicitlyAnnotated = true)
    private static class ExplicitField {
        @ExcelColumn
        final List<Integer> integers
        final List<String> strings

        ExplicitField(List<Integer> integers, List<String> strings) {
            this.integers = integers
            this.strings = strings
        }

        @ExcelModelCreator
        static ExplicitField of(List<Integer> numbers) {
            new ExplicitField(numbers, Collections.emptyList())
        }
    }

}
