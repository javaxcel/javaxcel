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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

public final class Borders {

    private Borders() {
        throw new UnsupportedOperationException(getClass().getName() + " is not allowed to instantiate");
    }

    public static void drawTop(CellStyle cellStyle, BorderStyle border) {
        cellStyle.setBorderTop(border);
    }

    public static void drawRight(CellStyle cellStyle, BorderStyle border) {
        cellStyle.setBorderRight(border);
    }

    public static void drawBottom(CellStyle cellStyle, BorderStyle border) {
        cellStyle.setBorderBottom(border);
    }

    public static void drawLeft(CellStyle cellStyle, BorderStyle border) {
        cellStyle.setBorderLeft(border);
    }

    public static void drawAll(CellStyle cellStyle, BorderStyle border) {
        cellStyle.setBorderTop(border);
        cellStyle.setBorderRight(border);
        cellStyle.setBorderBottom(border);
        cellStyle.setBorderLeft(border);
    }

    public static void dyeTop(CellStyle cellStyle, IndexedColors color) {
        cellStyle.setTopBorderColor(color.getIndex());
    }

    public static void dyeRight(CellStyle cellStyle, IndexedColors color) {
        cellStyle.setRightBorderColor(color.getIndex());
    }

    public static void dyeBottom(CellStyle cellStyle, IndexedColors color) {
        cellStyle.setBottomBorderColor(color.getIndex());
    }

    public static void dyeLeft(CellStyle cellStyle, IndexedColors color) {
        cellStyle.setLeftBorderColor(color.getIndex());
    }

    public static void dyeAll(CellStyle cellStyle, IndexedColors color) {
        cellStyle.setTopBorderColor(color.getIndex());
        cellStyle.setRightBorderColor(color.getIndex());
        cellStyle.setBottomBorderColor(color.getIndex());
        cellStyle.setLeftBorderColor(color.getIndex());
    }

}
