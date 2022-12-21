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

package com.github.javaxcel.core.converter.in.support

import com.github.javaxcel.core.converter.in.support.FieldTypeResolver.TypeResolution
import spock.lang.Specification

import static com.github.javaxcel.core.converter.in.support.FieldTypeResolver.Kind.ARRAY
import static com.github.javaxcel.core.converter.in.support.FieldTypeResolver.Kind.CONCRETE
import static com.github.javaxcel.core.converter.in.support.FieldTypeResolver.Kind.ITERABLE

class FieldTypeResolverSpec extends Specification {

    def "Resolves a type of field"() {
        given:
        def field = Model.getDeclaredField(fieldName)
        def type = field.genericType

        when:
        def actual = [] as List<TypeResolution>
        kinds.size().times {
            def resolution = FieldTypeResolver.resolve(type)
            actual.add(resolution)
        }

        then:
        !actual.isEmpty()
        actual.size() == kinds.size()
        actual*.kind == kinds
        actual.last().type == actualType

        where:
        fieldName                                    || actualType | resolvedCount | kinds
        "integer"                                    || Integer    | 1             | [CONCRETE]
        "object"                                     || Object     | 1             | [CONCRETE]
        "bigInteger"                                 || BigInteger | 1             | [CONCRETE]
        "array_string"                               || String     | 1             | [ARRAY, CONCRETE]
        "array_array_bigInteger"                     || BigInteger | 1             | [ARRAY, ARRAY, CONCRETE]
        "array_queue_string"                         || String     | 1             | [ARRAY, ITERABLE, CONCRETE]
        "array_iterable_array_iterable_array_locale" || Locale     | 1             | [ARRAY, ITERABLE, ARRAY, ITERABLE, ARRAY, CONCRETE]
        "iterable"                                   || Object     | 1             | [ITERABLE, CONCRETE]
        "iterable_long"                              || Long       | 1             | [ITERABLE, CONCRETE]
        "list_object"                                || Object     | 1             | [ITERABLE, CONCRETE]
        "set_bigInteger"                             || BigInteger | 1             | [ITERABLE, CONCRETE]
        "collection_bigInteger"                      || BigInteger | 1             | [ITERABLE, CONCRETE]
        "iterable_iterable_uuid"                     || UUID       | 1             | [ITERABLE, ITERABLE, CONCRETE]
    }

    // -------------------------------------------------------------------------------------------------

    private static class Model<
            A,
            B extends BigInteger,
            C extends Queue<String>,
            D extends Collection<B>,
            E extends Iterable<? super UUID>,
            F extends Iterable<? extends Locale[]>> {
        private Integer integer
        private A object
        private B bigInteger

        private String[] array_string
        private B[][] array_array_bigInteger
        private C[] array_queue_string
        private Iterable<? extends F[]>[] array_iterable_array_iterable_array_locale

        private Iterable iterable
        private Iterable<Long> iterable_long
        private List<A> list_object
        private Set<B> set_bigInteger
        private D collection_bigInteger
        private Iterable<E> iterable_iterable_uuid
    }

}
