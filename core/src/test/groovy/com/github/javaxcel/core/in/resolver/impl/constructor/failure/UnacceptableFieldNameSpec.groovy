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

import com.github.javaxcel.core.annotation.ExcelModelCreator.FieldName
import com.github.javaxcel.core.exception.InvalidExcelModelCreatorException
import com.github.javaxcel.core.in.resolver.impl.ExcelModelConstructorResolver
import spock.lang.Specification

class UnacceptableFieldNameSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(TestModel)

        when:
        resolver.resolve()

        then:
        def e = thrown(InvalidExcelModelCreatorException)
        e.message ==~ /^ResolvedParameter\.name must match name of the targeted fields, but it isn't: \(actual: '.+', allowed: \[.+]\)[\s\S]*$/
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class TestModel {
        final BigInteger bigInt
        final BigInteger bigNum

        private TestModel(@FieldName("bigInteger") BigInteger bigInt, @FieldName("bigNumber") BigInteger bigNum) {
            this.bigInt = bigInt
            this.bigNum = bigNum
        }
    }

}
