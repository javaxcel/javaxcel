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

package com.github.javaxcel.core.in.strategy.impl;

import io.github.imsejin.common.assertion.Asserts;

import com.github.javaxcel.core.in.context.ExcelReadContext;
import com.github.javaxcel.core.in.core.ExcelReader;
import com.github.javaxcel.core.in.core.impl.MapReader;
import com.github.javaxcel.core.in.core.impl.ModelReader;
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy;

/**
 * Strategy for limitation of the number of models when reading
 *
 * @since 0.8.0
 */
public class Limit implements ExcelReadStrategy {

    private final int value;

    /**
     * Strategy for limitation of the number of models.
     *
     * @param value limit for the number of models
     */
    public Limit(int value) {
        Asserts.that(value)
                .describedAs("ExcelReadStrategy.Limit.value is not allowed to be negative")
                .isZeroOrPositive();

        this.value = value;
    }

    @Override
    public boolean isSupported(ExcelReadContext<?> context) {
        Class<? extends ExcelReader<?>> writerType = context.getReaderType();
        return ModelReader.class.isAssignableFrom(writerType) || MapReader.class.isAssignableFrom(writerType);
    }

    @Override
    public Object execute(ExcelReadContext<?> context) {
        return this.value;
    }

}
