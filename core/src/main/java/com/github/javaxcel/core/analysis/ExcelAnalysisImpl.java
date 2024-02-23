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
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

import com.github.javaxcel.core.converter.handler.ExcelTypeHandler;

/**
 * @since 0.9.0
 */
public final class ExcelAnalysisImpl implements ExcelAnalysis {

    private final Field field;

    private int flags;

    private DefaultMeta defaultMeta;

    private ExcelTypeHandler<?> handler;

    public ExcelAnalysisImpl(Field field) {
        this.field = field;
    }

    @Override
    public Field getField() {
        return this.field;
    }

    @Override
    public int getFlags() {
        return this.flags;
    }

    public void addFlags(int flags) {
        this.flags |= flags;
    }

    @Override
    public DefaultMeta getDefaultMeta() {
        return this.defaultMeta;
    }

    public void setDefaultMeta(DefaultMeta defaultMeta) {
        this.defaultMeta = Objects.requireNonNull(defaultMeta,
                () -> getClass().getSimpleName() + ".defaultMeta cannot be null");
    }

    @Nullable
    @Override
    public ExcelTypeHandler<?> getHandler() {
        return this.handler;
    }

    public void setHandler(ExcelTypeHandler<?> handler) {
        this.handler = Objects.requireNonNull(handler,
                () -> getClass().getSimpleName() + ".handler cannot be null");
    }

    @Override
    @ExcludeFromGeneratedJacocoReport
    public String toString() {
        return "ExcelAnalysisImpl(field=" + field + ", flags=" + flags + ", defaultMeta=" + defaultMeta + ", handler="
                + handler + ')';
    }

    // -------------------------------------------------------------------------------------------------

    public static final class DefaultMetaImpl implements DefaultMeta {
        public static final DefaultMetaImpl EMPTY = new DefaultMetaImpl(null, Source.NONE);

        private final String value;

        private final Source source;

        public DefaultMetaImpl(@Nullable String value, Source source) {
            this.value = value;
            this.source = source;
        }

        @Nullable
        @Override
        public String getValue() {
            return this.value;
        }

        @Override
        public Source getSource() {
            return this.source;
        }

        @Override
        @ExcludeFromGeneratedJacocoReport
        public String toString() {
            return "DefaultMetaImpl(value=" + value + ", source=" + source + ')';
        }
    }

}
