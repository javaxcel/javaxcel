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

package com.github.javaxcel.core.converter.handler;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;

import io.github.imsejin.common.assertion.Asserts;

/**
 * Handler for type to help you to implement easily.
 *
 * @param <T> type of object to handle
 * @since 0.8.0
 */
@Getter
public abstract class AbstractExcelTypeHandler<T> implements ExcelTypeHandler<T> {

    @NotNull
    private final Class<T> type;

    /**
     * Instantiates a new type handler.
     *
     * @param type handled type
     */
    protected AbstractExcelTypeHandler(Class<T> type) {
        Asserts.that(type)
                .describedAs("AbstractExcelTypeHandler.type is not allowed to be null")
                .isNotNull();

        this.type = type;
    }

}
