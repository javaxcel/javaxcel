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

package com.github.javaxcel.core.in.resolver.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.javaxcel.core.annotation.ExcelModelCreator;
import com.github.javaxcel.core.exception.AmbiguousExcelModelCreatorException;
import com.github.javaxcel.core.exception.InvalidExcelModelCreatorException;
import com.github.javaxcel.core.exception.NoResolvableExcelModelCreatorException;
import com.github.javaxcel.core.in.resolver.AbstractExcelModelExecutableResolver;

/**
 * Resolver for method as model creator
 *
 * @param <T> type of model
 * @since 0.8.0
 */
public class ExcelModelMethodResolver<T> extends AbstractExcelModelExecutableResolver<T, Method> {

    public ExcelModelMethodResolver(Class<T> modelType) {
        super(modelType, Method.class);
    }

    /**
     * Returns methods as candidates.
     *
     * <p> Only searching for public methods that are annotated with
     * {@link ExcelModelCreator}, has static modifier and
     * whose return type is assignable to {@code super.modelType}.
     *
     * @return methods
     */
    @Override
    protected List<Method> getCandidates() {
        List<Method> candidates = new ArrayList<>();
        // Excludes the methods from super classes.
        for (Method candidate : super.modelType.getDeclaredMethods()) {
            if (!candidate.isAnnotationPresent(ExcelModelCreator.class)) {
                continue;
            }

            // Are modifiers of candidate public?
            if (!Modifier.isPublic(candidate.getModifiers())) {
                throw new InvalidExcelModelCreatorException("@ExcelModelCreator is not allowed to be annotated " +
                        "on non-public method; Remove the annotation from the method[%s]", candidate);
            }

            // Are modifiers of candidate static?
            if (!Modifier.isStatic(candidate.getModifiers())) {
                throw new InvalidExcelModelCreatorException("@ExcelModelCreator is not allowed to be annotated " +
                        "on instance method; Remove the annotation from the method[%s]", candidate);
            }

            // Is return type of candidate assignable to model type?
            if (!super.modelType.isAssignableFrom(candidate.getReturnType())) {
                throw new InvalidExcelModelCreatorException(
                        "@ExcelModelCreator is not allowed to be annotated on method " +
                                "whose return type is assignable to model type[%s]; Remove the annotation from the method[%s]",
                        super.modelType, candidate);
            }

            candidates.add(candidate);
        }

        return Collections.unmodifiableList(candidates);
    }

    @Override
    protected Method elect(List<Method> candidates) {
        // Unlike constructor, there may be no method as a candidate.
        if (candidates.isEmpty()) {
            throw new NoResolvableExcelModelCreatorException("Not found method of type[%s] to resolve; " +
                    "Annotate static method you want with @ExcelModelCreator", super.modelType);
        }

        // Many candidates.
        if (candidates.size() > 1) {
            throw new AmbiguousExcelModelCreatorException("Ambiguous methods%s to resolve; " +
                    "Remove @ExcelModelCreator from other methods except the one", candidates);
        }

        // Sole candidate.
        return candidates.get(0);
    }

}
