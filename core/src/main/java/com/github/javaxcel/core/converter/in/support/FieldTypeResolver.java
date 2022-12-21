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

package com.github.javaxcel.core.converter.in.support;

import io.github.imsejin.common.util.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Objects;

public class FieldTypeResolver {

    public static TypeResolution resolve(Type type) {
        Kind kind;

        while (true) {
            if (type instanceof Class) {
                kind = Kind.CONCRETE;
                break;
            }

            if (type instanceof TypeVariable) {
                TypeVariable<?> typeVariable = (TypeVariable<?>) type;
                type = typeVariable.getBounds()[0];
                continue;
            }

            if (type instanceof GenericArrayType) {
                GenericArrayType genericArrayType = (GenericArrayType) type;
                type = genericArrayType.getGenericComponentType();
                kind = Kind.ARRAY;
                break;
            }

            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();

                if (rawType instanceof Class && Iterable.class.isAssignableFrom((Class<?>) rawType)) {
                    type = parameterizedType.getActualTypeArguments()[0];
                    kind = Kind.ITERABLE;
                    break;
                } else {
                    type = rawType;
                    continue;
                }
            }

            if (type instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) type;
                Type[] lowerBounds = wildcardType.getLowerBounds();

                Type boundedType;
                if (ArrayUtils.exists(lowerBounds)) {
                    boundedType = lowerBounds[0];
                } else {
                    boundedType = wildcardType.getUpperBounds()[0];
                }

                type = boundedType;
            }
        }

        return new TypeResolution(type, kind);
    }

    // -------------------------------------------------------------------------------------------------

    public enum Kind {
        ARRAY, ITERABLE, CONCRETE
    }

    public static final class TypeResolution {
        private final Type type;
        private final Kind kind;

        private TypeResolution(Type type, Kind kind) {
            this.type = type;
            this.kind = kind;
        }

        public Type getType() {
            return this.type;
        }

        public Kind getKind() {
            return this.kind;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeResolution that = (TypeResolution) o;

            return type.equals(that.type) && kind == that.kind;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, kind);
        }

        @Override
        public String toString() {
            return "TypeResolution(type=" + type + ", kind=" + kind + ')';
        }
    }

}
