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

package com.github.javaxcel.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;
import io.github.imsejin.common.util.ReflectionUtils;
import io.github.imsejin.common.util.StringUtils;

import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelModel;

import static java.util.stream.Collectors.*;

/**
 * Utilities for reflection on {@link Field}.
 */
public final class FieldUtils {

    @ExcludeFromGeneratedJacocoReport
    private FieldUtils() {
        throw new UnsupportedOperationException(getClass().getName() + " is not allowed to instantiate");
    }

    /**
     * Gets the targeted fields depending on the policy.
     *
     * <p> If the targeting policy depended on {@link ExcelModel#includeSuper()} is
     * {@code true}, this returns its own declared fields.
     * Otherwise, this returns its own declared fields including super classes' fields.
     *
     * <p> This doesn't return the fields on {@link ExcelColumn} whose {@link ExcelColumn#ignored()} is {@code true}.
     *
     * <p> If {@link ExcelModel#onlyExplicitlyAnnotated()} on the model class is {@code true},
     * this excludes the fields not annotated with {@link ExcelColumn}.
     *
     * @param type type of the object
     * @return targeted fields
     * @see ExcelModel#includeSuper()
     * @see ExcelModel#onlyExplicitlyAnnotated()
     */
    public static List<Field> getTargetedFields(Class<?> type) {
        // Gets the fields depending on the policies.
        ExcelModel excelModel = type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = excelModel == null || !excelModel.includeSuper()
                ? Arrays.stream(type.getDeclaredFields())
                : ReflectionUtils.getInheritedFields(type).stream();

        // Excludes the synthetic fields.
        Predicate<Field> filter = it -> !it.isSynthetic();
        // Excludes the static fields.
        filter = filter.and(it -> !Modifier.isStatic(it.getModifiers()));
        // Excludes the fields to be ignored.
        filter = filter.and(it -> !Optional.ofNullable(it.getAnnotation(ExcelColumn.class))
                .map(ExcelColumn::ignored)
                .orElse(false));
        // Excludes the fields which is not annotated with @ExcelColumn.
        if (excelModel != null && excelModel.onlyExplicitlyAnnotated()) {
            filter = filter.and(it -> it.isAnnotationPresent(ExcelColumn.class));
        }

        return stream.filter(filter).collect(toList());
    }

    public static Method resolveGetter(Field field) {
        String fieldName = field.getName();

        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0));
        if (fieldName.length() > 1) {
            getterName += fieldName.substring(1);
        }

        Method getter = ReflectionUtils.getDeclaredMethod(field.getDeclaringClass(), getterName);
        if (getter.getReturnType() != field.getType()) {
            throw new IllegalArgumentException(
                    String.format("Return type[%s] of getter for field[%s] doesn't match the field type[%s]: %s",
                            getter.getReturnType().getName(), field, field.getType(), getter));
        }
        if (!Modifier.isPublic(getter.getModifiers())) {
            throw new IllegalArgumentException(
                    String.format("Getter for field[%s] is not public: %s", field, getter));
        }

        return getter;
    }

    public static Method resolveSetter(Field field) {
        String fieldName = field.getName();

        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0));
        if (fieldName.length() > 1) {
            setterName += fieldName.substring(1);
        }

        Method setter = ReflectionUtils.getDeclaredMethod(field.getDeclaringClass(), setterName, field.getType());
        if (!Modifier.isPublic(setter.getModifiers())) {
            throw new IllegalArgumentException(
                    String.format("Setter for field[%s] is not public: %s", field, setter));
        }

        return setter;
    }

    /**
     * Converts fields to header names.
     *
     * <p> This checks whether the field is annotated with {@link ExcelColumn} or not.
     * If {@link ExcelColumn#name()} is not null and not empty,
     * this returns a header name defined in the field, otherwise returns name of the field.
     *
     * @param fields           targeted fields
     * @param ignoreAnnotation whether {@link ExcelColumn#name()} is ignored
     * @return list of {@link ExcelColumn#name()} or {@link Field#getName()}
     */
    public static List<String> toHeaderNames(List<Field> fields, boolean ignoreAnnotation) {
        List<String> headerNames = new ArrayList<>();
        for (Field field : fields) {
            headerNames.add(toHeaderName(field, ignoreAnnotation));
        }

        return headerNames;
    }

    /**
     * Converts field to header name.
     *
     * <p> This checks whether the field is annotated with {@link ExcelColumn} or not.
     * If {@link ExcelColumn#name()} is not null and not blank,
     * this returns a header name defined in the field, otherwise returns name of the field.
     *
     * @param field            targeted field
     * @param ignoreAnnotation whether {@link ExcelColumn#name()} is ignored
     * @return {@link ExcelColumn#name()} or {@link Field#getName()}
     */
    public static String toHeaderName(Field field, boolean ignoreAnnotation) {
        if (ignoreAnnotation) {
            return field.getName();
        }

        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
        return annotation == null || StringUtils.isNullOrBlank(annotation.name())
                ? field.getName() : annotation.name();
    }

    /**
     * Converts fields to a map.
     *
     * @param model  model in list
     * @param fields targeted fields
     * @param <T>    type of the object
     * @return {@link Map} in which key is the model's field name and value is the model's field value
     * @see Field#getName()
     * @see ReflectionUtils#getFieldValue(Object, Field)
     */
    public static <T> Map<String, Object> toMap(T model, List<Field> fields) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            map.put(field.getName(), ReflectionUtils.getFieldValue(model, field));
        }

        return map;
    }

}
