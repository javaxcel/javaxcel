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

package com.github.javaxcel.core.in.resolver;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.StringUtils;

import com.github.javaxcel.core.annotation.ExcelModelCreator.FieldName;
import com.github.javaxcel.core.exception.InvalidExcelModelCreatorException;

import lombok.Getter;

import static java.util.stream.Collectors.*;

/**
 * Resolver for parameter name of model creator
 *
 * @since 0.8.0
 */
public class ExcelModelExecutableParameterNameResolver {

    private final Executable executable;

    private final List<Parameter> parameters;

    public ExcelModelExecutableParameterNameResolver(Executable executable) {
        Asserts.that(executable).isNotNull();

        this.executable = executable;
        this.parameters = Collections.unmodifiableList(Arrays.asList(executable.getParameters()));
    }

    public List<ResolvedParameter> resolve() {
        if (CollectionUtils.isNullOrEmpty(this.parameters)) {
            return Collections.emptyList();
        }

        List<ResolvedParameter> resolvedParameters = this.parameters.stream()
                .map(MethodParameter::forParameter).map(ResolvedParameter::new).collect(toList());

        Asserts.that(resolvedParameters)
                .describedAs("Failed to discover parameter names of {0}[{1}]",
                        this.executable.getClass().getSimpleName().toLowerCase(), this.executable)
                .isNotNull()
                .isNotEmpty()
                .doesNotContainNull()
                .hasSameSizeAs(this.parameters);

        return Collections.unmodifiableList(resolvedParameters);
    }

    // -------------------------------------------------------------------------------------------------

    public static final class ResolvedParameter {
        private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

        @Getter
        private final String name;
        @Getter
        private final boolean annotated;
        private final MethodParameter methodParameter;

        private ResolvedParameter(MethodParameter methodParameter) {
            FieldName annotation = methodParameter.getParameterAnnotation(FieldName.class);
            boolean annotated = annotation != null;

            String name;
            if (annotated) {
                name = annotation.value();
                if (StringUtils.isNullOrBlank(name)) {
                    Executable executable = methodParameter.getExecutable();
                    String executableName = executable.getClass().getSimpleName().toLowerCase();

                    throw new InvalidExcelModelCreatorException(
                            "@FieldName.value is not allowed to be blank[%s]; Specify the proper value or detach the annotation from that parameter[%s] of %s[%s]",
                            name, methodParameter.getParameter(), executableName, executable);
                }
            } else {
                methodParameter.initParameterNameDiscovery(DISCOVERER);
                name = methodParameter.getParameterName();
            }

            this.name = name;
            this.annotated = annotated;
            this.methodParameter = methodParameter;
        }

        public Class<?> getType() {
            return this.methodParameter.getParameterType();
        }

        public int getIndex() {
            return this.methodParameter.getParameterIndex();
        }

        public Executable getDeclaringExecutable() {
            return this.methodParameter.getExecutable();
        }

        @Override
        public String toString() {
            if (isAnnotated()) {
                this.methodParameter.initParameterNameDiscovery(DISCOVERER);
                String originalName = this.methodParameter.getParameterName();
                return "@FieldName('" + this.name + "') " + getType().getName() + ' ' + originalName;
            } else {
                return getType().getName() + ' ' + this.name;
            }
        }
    }

}
