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

package com.github.javaxcel.core.out.strategy.impl;

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

import com.github.javaxcel.core.out.context.ExcelWriteContext;
import com.github.javaxcel.core.out.core.ExcelWriter;
import com.github.javaxcel.core.out.core.impl.MapWriter;
import com.github.javaxcel.core.out.core.impl.ModelWriter;
import com.github.javaxcel.core.out.strategy.AbstractExcelWriteStrategy;

/**
 * Strategy for style of hidden extra rows when writing
 *
 * <p> This strategy will automatically create rows up to the maximum number of rows and hide them.
 * This action takes more time and makes file size bigger than it doesn't.
 *
 * @since 0.8.0
 */
public class HiddenExtraRows extends AbstractExcelWriteStrategy {

    @Override
    public boolean isSupported(ExcelWriteContext<?> context) {
        Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
        return ModelWriter.class.isAssignableFrom(writerType) || MapWriter.class.isAssignableFrom(writerType);
    }

    @Override
    @ExcludeFromGeneratedJacocoReport
    public Object execute(ExcelWriteContext<?> context) {
        throw new UnsupportedOperationException(
                "ExcelWriteStrategy." + getClass().getSimpleName() + " is not supported");
    }

}
