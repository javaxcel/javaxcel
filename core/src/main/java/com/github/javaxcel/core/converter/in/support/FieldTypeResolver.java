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

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;
import io.github.imsejin.common.util.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import static com.github.javaxcel.core.converter.in.support.FieldTypeResolver.Kind.CONCRETE;

/**
 * Resolver for type of field
 *
 * @since 0.9.0
 */
public class FieldTypeResolver {

    /**
     * Resolves a concrete type from the field.
     *
     * @param field field of model
     * @return concrete type
     */
    public static Class<?> resolveConcreteType(Field field) {
        return resolveConcreteType(field.getGenericType());
    }

    /**
     * Resolves a concrete type from the type.
     *
     * @param type generic type of field
     * @return concrete type
     */
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
        Class<?> iterableType = null;

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
                        iterableType = clazz;
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
                    iterableType = (Class<?>) rawType;
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

        return new TypeResolution(kind, type, elementType, iterableType);
    }

    // -------------------------------------------------------------------------------------------------

    /**
     * Kind of resolved type
     *
     * @since 0.9.0
     */
    public enum Kind {
        ARRAY, ITERABLE, CONCRETE
    }

    /**
     * Resolution of type
     *
     * @since 0.9.0
     */
    public static final class TypeResolution {
        private final Kind kind;
        private final Type currentType;
        private final Type elementType;
        private final Class<? extends Iterable<?>> iterableType;

        @SuppressWarnings("unchecked")
        private TypeResolution(Kind kind, Type currentType, @Nullable Type elementType, @Nullable Class<?> iterableType) {
            this.kind = kind;
            this.currentType = currentType;
            this.elementType = elementType;
            this.iterableType = (Class<? extends Iterable<?>>) iterableType;
        }

        /**
         * Returns the kind of resolved type.
         *
         * @return kind of type
         */
        public Kind getKind() {
            return this.kind;
        }

        /**
         * Returns the resolved type.
         *
         * @return resolved type
         */
        public Type getCurrentType() {
            return this.currentType;
        }

        /**
         * Returns the type of element.
         *
         * @return type of element or {@code null} if the kind is {@code CONCRETE}
         */
        @Nullable
        public Type getElementType() {
            return this.elementType;
        }

        /**
         * Returns the specific type of iterable.
         *
         * @return specific type of iterable or {@code null} if the kind is not {@code ITERABLE}
         */
        @Nullable
        public Class<? extends Iterable<?>> getIterableType() {
            return this.iterableType;
        }

        @Override
        @ExcludeFromGeneratedJacocoReport
        public String toString() {
            return "TypeResolution(kind=" + kind + ", currentType=" + currentType + ", elementType=" + elementType + ", iterableType=" + iterableType + ')';
        }
    }

}
