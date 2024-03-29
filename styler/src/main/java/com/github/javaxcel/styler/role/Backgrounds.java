/*
 * Copyright 2020 Javaxcel
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

package com.github.javaxcel.styler.role;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

public final class Backgrounds {

    @ExcludeFromGeneratedJacocoReport
    private Backgrounds() {
        throw new UnsupportedOperationException(getClass().getName() + " is not allowed to instantiate");
    }

    public static void setColor(CellStyle cellStyle, IndexedColors color) {
        cellStyle.setFillForegroundColor(color.getIndex());
    }

    public static void setPattern(CellStyle cellStyle, FillPatternType pattern) {
        cellStyle.setFillPattern(pattern);
    }

}
