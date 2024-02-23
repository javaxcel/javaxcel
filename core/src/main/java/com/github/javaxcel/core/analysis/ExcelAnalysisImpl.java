/*
 * Copyright 2023 Javaxcel
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

package com.github.javaxcel.core.analysis;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;
import lombok.Getter;
import lombok.ToString;

import com.github.javaxcel.core.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.core.validator.ExcelColumnValidator;

/**
 * @since 0.9.0
 */
@Getter
@ToString
public final class ExcelAnalysisImpl implements ExcelAnalysis {

    private final Field field;

    private int flags;

    private DefaultMeta defaultMeta;

    @Nullable
    private ExcelTypeHandler<?> handler;

    private List<ExcelColumnValidator> validators;

    public ExcelAnalysisImpl(Field field) {
        this.field = field;
    }

    public void addFlags(int flags) {
        this.flags |= flags;
    }

    public void setDefaultMeta(DefaultMeta defaultMeta) {
        this.defaultMeta = Objects.requireNonNull(defaultMeta,
                () -> getClass().getSimpleName() + ".defaultMeta cannot be null");
    }

    public void setHandler(ExcelTypeHandler<?> handler) {
        this.handler = Objects.requireNonNull(handler,
                () -> getClass().getSimpleName() + ".handler cannot be null");
    }

    public void setValidators(List<ExcelColumnValidator> validators) {
        Asserts.that(validators)
                .describedAs("{0}.validators cannot be null", getClass().getSimpleName())
                .isNotNull()
                .describedAs("{0}.validators cannot contain null", getClass().getSimpleName())
                .doesNotContainNull();

        this.validators = validators;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @ToString
    public static final class DefaultMetaImpl implements DefaultMeta {
        public static final DefaultMetaImpl EMPTY = new DefaultMetaImpl(null, Source.NONE);

        @Nullable
        private final String value;

        @NotNull
        private final Source source;

        public DefaultMetaImpl(@Nullable String value, @NotNull Source source) {
            this.value = value;
            this.source = source;
        }
    }

}
