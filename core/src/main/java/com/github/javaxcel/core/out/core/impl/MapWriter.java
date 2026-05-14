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

package com.github.javaxcel.core.out.core.impl;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import com.github.javaxcel.core.out.core.ExcelWriter;
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;

/**
 * Excel writer for {@link Map} — kept as a thin compatibility shim that
 * delegates to {@link DefaultExcelWriter}.
 *
 * @deprecated Construct via {@link com.github.javaxcel.core.Javaxcel#writer(Workbook)}
 *             instead. This class will be removed in a future major release.
 */
@Deprecated(forRemoval = true, since = "0.10.0")
public class MapWriter implements ExcelWriter<Map<String, Object>> {

    private final DefaultExcelWriter<Map<String, Object>> delegate;

    public MapWriter(Workbook workbook) {
        this.delegate = DefaultExcelWriter.forMap(workbook);
    }

    @Override
    public ExcelWriter<Map<String, Object>> options(ExcelWriteStrategy... strategies) {
        this.delegate.options(strategies);
        return this;
    }

    @Override
    public void write(OutputStream out, List<Map<String, Object>> list) {
        this.delegate.write(out, list);
    }

}
