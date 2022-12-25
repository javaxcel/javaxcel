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

package com.github.javaxcel.core.in.resolver.impl.method.failure

import com.github.javaxcel.core.exception.NoResolvableExcelModelCreatorException
import com.github.javaxcel.core.in.resolver.impl.ExcelModelMethodResolver
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.AccessMode

@Subject(ExcelModelMethodResolver)
class NoAnnotatedMethodSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelMethodResolver<>(TestModel)

        when:
        resolver.resolve()

        then:
        def e = thrown(NoResolvableExcelModelCreatorException)
        e.message ==~ /^Not found method of type\[.+] to resolve; Annotate static method you want with @ExcelModelCreator$/
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class TestModel {
        final AccessMode accessMode

        private TestModel(AccessMode accessMode) {
            this.accessMode = accessMode
        }

        static TestModel withRead() {
            with(AccessMode.READ)
        }

        static TestModel withWrite() {
            with(AccessMode.WRITE)
        }

        static TestModel withExecute() {
            with(AccessMode.EXECUTE)
        }

        static TestModel with(AccessMode accessMode) {
            new TestModel(accessMode)
        }
    }

}
