/*
 * Copyright 2020 Javaxcel
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

package com.github.javaxcel.core.converter.in;

import com.github.javaxcel.core.analysis.ExcelAnalysis;
import com.github.javaxcel.core.analysis.in.ExcelReadAnalyzer;
import com.github.javaxcel.core.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.converter.in.support.FieldTypeResolver;
import com.github.javaxcel.core.converter.in.support.FieldTypeResolver.Kind;
import com.github.javaxcel.core.converter.in.support.FieldTypeResolver.TypeResolution;
import com.github.javaxcel.core.converter.in.support.StringArraySplitter;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ArrayUtils;
import io.github.imsejin.common.util.ClassUtils;
import io.github.imsejin.common.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReadHandlerConverter implements ExcelReadConverter {

    private static final StringArraySplitter SPLITTER = new StringArraySplitter(", ");

    private final ExcelTypeHandlerRegistry registry;

    private final Map<Field, ExcelAnalysis> analysisMap;

    public ExcelReadHandlerConverter(Iterable<ExcelAnalysis> analyses, ExcelTypeHandlerRegistry registry) {
        Asserts.that(analyses)
                .describedAs("ExcelReadHandlerConverter.analyses is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadHandlerConverter.analyses is not allowed to be empty")
                .is(them -> them.iterator().hasNext());
        Asserts.that(registry)
                .describedAs("ExcelReadHandlerConverter.registry is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadHandlerConverter.registry.allTypes is not allowed to be null")
                .isNot(it -> it.getAllTypes() == null);

        this.registry = registry;

        Map<Field, ExcelAnalysis> analysisMap = new HashMap<>();
        for (ExcelAnalysis analysis : analyses) {
            Field field = analysis.getField();
            analysisMap.put(field, analysis);
        }

        this.analysisMap = Collections.unmodifiableMap(analysisMap);
    }

    @Override
    public boolean supports(Field field) {
        ExcelAnalysis analysis = this.analysisMap.get(field);
        return analysis.hasFlag(ExcelReadAnalyzer.HANDLER);
    }

    @Nullable
    @Override
    public Object convert(Map<String, String> variables, Field field) {
        Type type = field.getGenericType();
        String value = variables.get(field.getName());

        return handleInternal(field, type, value);
    }

    private Object handleInternal(Field field, Type type, String value) {
        // When cell value is null or empty.
        if (StringUtils.isNullOrEmpty(value)) {
            ExcelAnalysis analysis = this.analysisMap.get(field);
            String defaultValue = analysis.getDefaultMeta().getValue();

            if (StringUtils.isNullOrEmpty(defaultValue)) {
                // When you don't explicitly define default value.
                if (type instanceof Class) {
                    return ClassUtils.initialValueOf((Class<?>) type);
                }

                // Returns null as default value, because initial value of the type is always null
                // if the type is not a Class.
                return null;
            } else {
                // Converts again with the default value.
                return handleInternal(field, type, defaultValue);
            }
        }

        TypeResolution resolution = FieldTypeResolver.resolve(type);
        type = resolution.getCurrentType();

        switch (resolution.getKind()) {
            case ARRAY:
                Class<?> concreteType = FieldTypeResolver.resolveConcreteType(type);

                // Finds dimension of the array by Type.typeName that has string "[]" when it is array.
                String typeName = resolution.getCurrentType().getTypeName();
                int dimension = StringUtils.countOf(typeName, "[]");

                // Supports multidimensional array type.
                return handleArray(field, concreteType, dimension, value);
            case ITERABLE:
                // Supports nested iterable type.
                return handleIterable(field, resolution, value);
            case CONCRETE:
                return handleConcrete(field, (Class<?>) type, value);
            default:
                throw new AssertionError("Never throw");
        }
    }

    private Object handleArray(Field field, Class<?> concreteType, int dimension, String value) {
        Asserts.that(concreteType)
                .describedAs("Mixed array and iterable is not supported: {0}", field)
                .thrownBy(UnsupportedOperationException::new)
                .isNotNull()
                .isNot(Iterable.class::isAssignableFrom);
        Asserts.that(dimension)
                .describedAs("Dimension of an array must be positive: {0}", dimension)
                .isNotNull()
                .isPositive();

        Class<?> componentType = ArrayUtils.resolveArrayType(concreteType, dimension - 1);
        String[] strings = SPLITTER.shallowSplit(value);

        // To solve that ClassCastException(primitive array doesn't be assignable to Object array),
        // we use java.lang.reflect.Array API instead of casting primitive array to Object array.
        Object array = Array.newInstance(componentType, strings.length);

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];

            // Regards an empty string as null.
            if (StringUtils.isNullOrEmpty(string)) {
                Array.set(array, i, null);
                continue;
            }

            Object element;
            if (componentType.isArray()) {
                element = string.isEmpty() ? null : handleArray(field, concreteType, dimension - 1, string);
            } else {
                // TODO: why...? Allows empty string to handler for non-array type.
                element = handleConcrete(field, componentType, string);
            }

            Array.set(array, i, element);
        }

        return array;
    }

    private Iterable<?> handleIterable(Field field, TypeResolution resolution, String value) {
        Asserts.that(resolution)
                .describedAs("It is not a type of java.lang.Iterable: {0}", field)
                .isNotNull()
                .returns(Kind.ITERABLE, TypeResolution::getKind);

        resolution = FieldTypeResolver.resolve(resolution.getElementType());
        Kind kind = resolution.getKind();

        Asserts.that(kind)
                .describedAs("Mixed array and iterable is not supported: {0}", field)
                .thrownBy(UnsupportedOperationException::new)
                .isNotNull()
                .isNotEqualTo(Kind.ARRAY);

        String[] strings = SPLITTER.shallowSplit(value);

        // TODO: implement an IterableSupplier.
        List<Object> list = new ArrayList<>(strings.length);

        for (String string : strings) {
            // Regards an empty string as null.
            if (StringUtils.isNullOrEmpty(string)) {
                list.add(null);
                continue;
            }

            Object element;
            if (kind == Kind.ITERABLE) {
                element = string.isEmpty() ? null : handleIterable(field, resolution, string);
            } else {
                // Allows empty string to handler for non-array type.
                Class<?> clazz = (Class<?>) resolution.getCurrentType();
                element = handleConcrete(field, clazz, string);
            }

            list.add(element);
        }

        return list;
    }

    private Object handleConcrete(Field field, Class<?> type, String value) {
        // Resolves a handler of the type.
        ExcelTypeHandler<?> handler = this.registry.getHandler(type);

        if (handler == null) {
            // When there is no handler for the type.
            if (!ClassUtils.isEnumOrEnumConstant(type)) {
                return ClassUtils.initialValueOf(type);
            }

            // When there is no handler for the specific enum type, use EnumTypeHandler as default.
            handler = this.registry.getHandler(Enum.class);
        }

        try {
            // Converts string to the type of field.
            return handler.read(value, field);
        } catch (Exception e) {
            String message = String.format("Failed to convert %s(String) to %s", value, type.getSimpleName());
            throw new RuntimeException(message, e);
        }
    }

}
