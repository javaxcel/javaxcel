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

import com.github.javaxcel.core.out.context.ExcelWriteContext;
import com.github.javaxcel.core.out.core.ExcelWriter;
import com.github.javaxcel.core.out.core.impl.MapWriter;
import com.github.javaxcel.core.out.core.impl.ModelWriter;
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;

/**
 * Strategy for style of resizing width of columns when writing
 *
 * <p> This strategy will make all columns fit their content.
 *
 * @since 0.8.0
 */
public class AutoResizedColumns implements ExcelWriteStrategy {

    private final boolean manual;

    /**
     * Strategy for style of resizing column width.
     *
     * @param manual whether resize columns manually or not
     * @since 0.9.1
     */
    public AutoResizedColumns(boolean manual) {
        this.manual = manual;
    }

    @Override
    public boolean isSupported(ExcelWriteContext<?> context) {
        Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
        return ModelWriter.class.isAssignableFrom(writerType) || MapWriter.class.isAssignableFrom(writerType);
    }

    @Override
    public Object execute(ExcelWriteContext<?> context) {
        return this.manual;
    }

}
