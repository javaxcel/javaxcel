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

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

import com.github.javaxcel.core.in.context.ExcelReadContext;
import com.github.javaxcel.core.in.core.ExcelReader;
import com.github.javaxcel.core.in.core.impl.ModelReader;
import com.github.javaxcel.core.in.strategy.AbstractExcelReadStrategy;

/**
 * Strategy for setting value into property of model through setter
 *
 * @since 0.9.0
 */
public class UseSetters extends AbstractExcelReadStrategy {

    @Override
    public boolean isSupported(ExcelReadContext<?> context) {
        Class<? extends ExcelReader<?>> writerType = context.getReaderType();
        return ModelReader.class.isAssignableFrom(writerType);
    }

    @Override
    @ExcludeFromGeneratedJacocoReport
    public Object execute(ExcelReadContext<?> context) {
        throw new UnsupportedOperationException(
                "ExcelReadStrategy." + getClass().getSimpleName() + " is not supported");
    }

}
