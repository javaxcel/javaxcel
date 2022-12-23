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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

import static com.github.javaxcel.core.converter.in.support.FieldTypeResolver.Kind.CONCRETE;

public class FieldTypeResolver {

    private static final List<Class<?>> RESOLVABLE_ITERABLE_TYPES = Collections.unmodifiableList(Arrays.asList(
            BlockingDeque.class,
            BlockingQueue.class,
            Deque.class,
            Queue.class,
            NavigableSet.class,
            SortedSet.class,
            Set.class,
            List.class,
            Collection.class,
            Iterable.class
    ));

    public static Class<?> resolveConcreteType(Field field) {
        return resolveConcreteType(field.getGenericType());
    }

    public static Class<?> resolveConcreteType(Type type) {
        TypeResolution resolution;
        do {
            resolution = resolve(type);
            type = resolution.getElementType();

        } while (resolution.getKind() != CONCRETE);

        return (Class<?>) resolution.getCurrentType();
    }

    /**
     * Resolves a resolution of the given type.
     *
     * @param type type to resolve
     * @return resolution of the type
     */
    public static TypeResolution resolve(Type type) {
        Kind kind;
        Type elementType = null;

        while (true) {
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;

                if (clazz.isArray()) {
                    elementType = clazz.getComponentType();
                    kind = Kind.ARRAY;
                } else {
                    // When the type is raw type.
                    if (Iterable.class.isAssignableFrom(clazz)) {
                        elementType = Object.class;
                        kind = Kind.ITERABLE;
                        break;
                    }

                    kind = Kind.CONCRETE;
                }

                break;
            }

            // class Sample<S, C extends Iterable<S>> {
            //     private C c;
            // } ... typeVariable.bounds == [Iterable<S>]
            if (type instanceof TypeVariable) {
                TypeVariable<?> typeVariable = (TypeVariable<?>) type;
                type = typeVariable.getBounds()[0];
                continue;
            }

            // class Sample<S extends Number> {
            //     private S[][] s;
            // } ... genericArrayType.genericComponentType == S[]
            if (type instanceof GenericArrayType) {
                GenericArrayType genericArrayType = (GenericArrayType) type;
                elementType = genericArrayType.getGenericComponentType();
                kind = Kind.ARRAY;
                break;
            }

            // class Sample<S extends Number> {
            //     private Iterable<Sample<Long>> samples;
            // } ... parameterizedType.rawType == Iterable.class
            // ... parameterizedType.actualTypeArguments == [Sample]
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();

                if (rawType instanceof Class && Iterable.class.isAssignableFrom((Class<?>) rawType)) {
                    elementType = parameterizedType.getActualTypeArguments()[0];
                    kind = Kind.ITERABLE;
                    break;
                } else {
                    type = rawType;
                    continue;
                }
            }

            // When type is wildcard type:
            // List<? super java.lang.String>
            // List<? extends java.lang.String>
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

        return new TypeResolution(kind, type, elementType);
    }

    // -------------------------------------------------------------------------------------------------

    public enum Kind {
        ARRAY, ITERABLE, CONCRETE
    }

    public static final class TypeResolution {
        private final Kind kind;
        private final Type currentType;
        private final Type elementType;

        private TypeResolution(Kind kind, Type currentType, @Nullable Type elementType) {
            this.kind = kind;
            this.currentType = currentType;
            this.elementType = elementType;
        }

        public Kind getKind() {
            return this.kind;
        }

        public Type getCurrentType() {
            return this.currentType;
        }

        @Nullable
        public Type getElementType() {
            return this.elementType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeResolution that = (TypeResolution) o;

            return kind == that.kind
                    && currentType.equals(that.currentType)
                    && Objects.equals(elementType, that.elementType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, currentType, elementType);
        }

        @Override
        public String toString() {
            return "TypeResolution(kind=" + kind + ", currentType=" + currentType + ", elementType=" + elementType + ')';
        }
    }

}
