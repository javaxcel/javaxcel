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
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import com.github.javaxcel.core.in.core.ExcelReader;
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy;

/**
 * Excel reader for {@link Map} — kept as a thin compatibility shim that
 * delegates to {@link DefaultExcelReader}.
 *
 * @deprecated Construct via {@link com.github.javaxcel.core.Javaxcel#reader(Workbook)}
 *             instead. This class will be removed in a future major release.
 */
@Deprecated(forRemoval = true, since = "0.10.0")
public class MapReader implements ExcelReader<Map<String, String>> {

    private final DefaultExcelReader<Map<String, String>> delegate;

    public MapReader(Workbook workbook) {
        this.delegate = DefaultExcelReader.forMap(workbook);
    }

    @Override
    public ExcelReader<Map<String, String>> options(ExcelReadStrategy... strategies) {
        this.delegate.options(strategies);
        return this;
    }

    @Override
    public List<Map<String, String>> read() {
        return this.delegate.read();
    }

}
