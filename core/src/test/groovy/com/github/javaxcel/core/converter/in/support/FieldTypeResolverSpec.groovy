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
import com.github.javaxcel.test.converter.in.support.FieldTypeResolver_TestModel_1
import com.github.javaxcel.test.converter.in.support.FieldTypeResolver_TestModel_2
import spock.lang.Specification
import spock.lang.Subject

import java.lang.reflect.Field
import java.lang.reflect.Type

import static com.github.javaxcel.core.converter.in.support.FieldTypeResolver.Kind.ARRAY
import static com.github.javaxcel.core.converter.in.support.FieldTypeResolver.Kind.CONCRETE
import static com.github.javaxcel.core.converter.in.support.FieldTypeResolver.Kind.ITERABLE

@Subject(FieldTypeResolver)
class FieldTypeResolverSpec extends Specification {

    def "Resolves a concrete type of the field"() {
        given:
        Field field = FieldTypeResolver_TestModel_1.getDeclaredField(fieldName)

        when:
        Class<?> concreteType = FieldTypeResolver.resolveConcreteType(field)

        then:
        concreteType == expected

        where:
        fieldName                                             | expected
        "concrete"                                            | Long
        "raw"                                                 | FieldTypeResolver_TestModel_1
        "generic"                                             | FieldTypeResolver_TestModel_1
        "generic_array"                                       | FieldTypeResolver_TestModel_1
        "type_variable"                                       | Object
        "type_variable_array"                                 | Object
        "type_variable_2d_array"                              | Object
        "bounded_type_variable"                               | UUID
        "bounded_type_variable_array"                         | UUID
        "bounded_type_variable_2d_array"                      | UUID
        "bounded_iterable_type_variable"                      | Double
        "bounded_iterable_type_variable_array"                | Double
        "bounded_iterable_type_variable_2d_array"             | Double
        "iterable"                                            | Object
        "iterable_unknown"                                    | Object
        "iterable_concrete"                                   | Long
        "iterable_concrete_array"                             | Long
        "iterable_raw"                                        | FieldTypeResolver_TestModel_1
        "iterable_generic"                                    | FieldTypeResolver_TestModel_1
        "iterable_upper_wildcard_concrete"                    | Long
        "iterable_lower_wildcard_concrete"                    | Long
        "iterable_upper_wildcard_generic"                     | FieldTypeResolver_TestModel_1
        "iterable_lower_wildcard_generic"                     | FieldTypeResolver_TestModel_1
        "iterable_type_variable"                              | Object
        "iterable_type_variable_array"                        | Object
        "iterable_upper_wildcard_type_variable"               | Object
        "iterable_lower_wildcard_type_variable"               | Object
        "iterable_upper_wildcard_type_variable_array"         | Object
        "iterable_lower_wildcard_type_variable_array"         | Object
        "iterable_bounded_type_variable"                      | UUID
        "iterable_bounded_type_variable_array"                | UUID
        "iterable_upper_wildcard_bounded_type_variable"       | UUID
        "iterable_lower_wildcard_bounded_type_variable"       | UUID
        "iterable_upper_wildcard_bounded_type_variable_array" | UUID
        "iterable_lower_wildcard_bounded_type_variable_array" | UUID
        "iterable_bounded_iterable_type_variable"             | Double
        "iterable_iterable_generic"                           | FieldTypeResolver_TestModel_1
    }

    def "Resolves a nested type from the type"() {
        given:
        Field field = FieldTypeResolver_TestModel_2.getDeclaredField(fieldName)
        Type type = field.genericType

        when:
        List<TypeResolution> resolutions = []
        do {
            TypeResolution resolution = FieldTypeResolver.resolve(type)
            resolutions.add(resolution)
            type = resolution.elementType

            // If a kind of resolution is array or iterable,
            // an element type of the resolution is always null.
            if (resolution.kind != CONCRETE) {
                assert resolution.elementType != null
            }

        } while (resolutions.last().kind != CONCRETE)

        then:
        !resolutions.isEmpty()

        and: "Last resolution has a concrete type"
        resolutions.last().currentType == concreteType
        resolutions.last().elementType == null

        and: "Checks the kind of all resolutions"
        resolutions.size() == kinds.size()
        resolutions*.kind == kinds

        and: "Checks the current type of all resolutions"
        resolutions.size() == types.size()
        resolutions*.currentType.typeName == types

        where:
        fieldName                                    || concreteType | kinds                                               | types
        "integer"                                    || Integer      | [CONCRETE]                                          | ["java.lang.Integer"]
        "object"                                     || Object       | [CONCRETE]                                          | ["java.lang.Object"]
        "bigInteger"                                 || BigInteger   | [CONCRETE]                                          | ["java.math.BigInteger"]
        "map"                                        || Map          | [CONCRETE]                                          | ["java.util.Map"]
        "array_string"                               || String       | [ARRAY, CONCRETE]                                   | ["java.lang.String[]", "java.lang.String"]
        "array_array_bigInteger"                     || BigInteger   | [ARRAY, ARRAY, CONCRETE]                            | ["B[][]", "B[]", "java.math.BigInteger"]
        "array_queue_string"                         || String       | [ARRAY, ITERABLE, CONCRETE]                         | ["C[]", "java.util.Queue<java.lang.String>", "java.lang.String"]
        "array_iterable_array_iterable_array_locale" || Locale       | [ARRAY, ITERABLE, ARRAY, ITERABLE, ARRAY, CONCRETE] | ["java.lang.Iterable<? extends F[]>[]", "java.lang.Iterable<? extends F[]>", "F[]", "java.lang.Iterable<? extends java.util.Locale[]>", "java.util.Locale[]", "java.util.Locale"]
        "iterable"                                   || Object       | [ITERABLE, CONCRETE]                                | ["java.lang.Iterable", "java.lang.Object"]
        "iterable_long"                              || Long         | [ITERABLE, CONCRETE]                                | ["java.lang.Iterable<java.lang.Long>", "java.lang.Long"]
        "deque_object"                               || Object       | [ITERABLE, CONCRETE]                                | ["java.util.Deque<?>", "java.lang.Object"]
        "list_object"                                || Object       | [ITERABLE, CONCRETE]                                | ["java.util.List<A>", "java.lang.Object"]
        "set_bigInteger"                             || BigInteger   | [ITERABLE, CONCRETE]                                | ["java.util.Set<B>", "java.math.BigInteger"]
        "collection_bigInteger"                      || BigInteger   | [ITERABLE, CONCRETE]                                | ["java.util.Collection<B>", "java.math.BigInteger"]
        "iterable_iterable_uuid"                     || UUID         | [ITERABLE, ITERABLE, CONCRETE]                      | ["java.lang.Iterable<E>", "java.lang.Iterable<? super java.util.UUID>", "java.util.UUID"]
    }

}
