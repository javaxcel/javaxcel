/*
 * Copyright 2021 Javaxcel
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

package com.github.javaxcel.core.in.core.impl;

import java.util.List;

import com.github.javaxcel.core.in.core.DefaultExcelReader;
import org.apache.poi.ss.usermodel.Workbook;

import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.in.core.ExcelReader;
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy;

/**
 * Excel reader for model — kept as a thin compatibility shim that delegates to
 * {@link DefaultExcelReader}.
 *
 * @param <T> type of model
 * @deprecated Construct via {@link com.github.javaxcel.core.Javaxcel#reader(Workbook, Class)}
 *             instead. This class will be removed in a future major release.
 */
@Deprecated(forRemoval = true, since = "0.10.0")
public class ModelReader<T> implements ExcelReader<T> {

    private final DefaultExcelReader<T> delegate;

    public ModelReader(Workbook workbook, Class<T> modelType, ExcelTypeHandlerRegistry registry) {
        this.delegate = DefaultExcelReader.forModel(workbook, modelType, registry);
    }

    @Override
    public ExcelReader<T> options(ExcelReadStrategy... strategies) {
        this.delegate.options(strategies);
        return this;
    }

    @Override
    public List<T> read() {
        return this.delegate.read();
    }

}
