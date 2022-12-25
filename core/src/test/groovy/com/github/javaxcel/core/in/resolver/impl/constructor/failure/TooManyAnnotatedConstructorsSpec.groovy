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

package com.github.javaxcel.core.in.resolver.impl.constructor.failure

import com.github.javaxcel.core.annotation.ExcelModelCreator
import com.github.javaxcel.core.exception.AmbiguousExcelModelCreatorException
import com.github.javaxcel.core.in.resolver.impl.ExcelModelConstructorResolver
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.AccessMode

@Subject(ExcelModelConstructorResolver)
class TooManyAnnotatedConstructorsSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(TestModel)

        when:
        resolver.resolve()

        then:
        def e = thrown(AmbiguousExcelModelCreatorException)
        e.message ==~ /^Ambiguous constructors\[.+] to resolve; Remove @ExcelModelCreator from other constructors except the one$/
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class TestModel {
        final AccessMode accessMode

        @ExcelModelCreator
        TestModel(String modeName) {
            this(AccessMode.valueOf(modeName))
        }

        @ExcelModelCreator
        private TestModel(AccessMode accessMode) {
            this.accessMode = accessMode
        }
    }

}
