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
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

public final class Fonts {

    @ExcludeFromGeneratedJacocoReport
    private Fonts() {
        throw new UnsupportedOperationException(getClass().getName() + " is not allowed to instantiate");
    }

    public static void setName(CellStyle cellStyle, Font font, String name) {
        font.setFontName(name);
        cellStyle.setFont(font);
    }

    public static void setSize(CellStyle cellStyle, Font font, int size) {
        font.setFontHeightInPoints((short) size);
        cellStyle.setFont(font);
    }

    public static void setColor(CellStyle cellStyle, Font font, IndexedColors color) {
        font.setColor(color.getIndex());
        cellStyle.setFont(font);
    }

    public static void bold(CellStyle cellStyle, Font font) {
        font.setBold(true);
        cellStyle.setFont(font);
    }

    public static void italic(CellStyle cellStyle, Font font) {
        font.setItalic(true);
        cellStyle.setFont(font);
    }

    public static void strikeout(CellStyle cellStyle, Font font) {
        font.setStrikeout(true);
        cellStyle.setFont(font);
    }

    public static void setUnderline(CellStyle cellStyle, Font font, Underline underline) {
        font.setUnderline(underline.value);
        cellStyle.setFont(font);
    }

    public static void setOffset(CellStyle cellStyle, Font font, Offset offset) {
        font.setTypeOffset(offset.value);
        cellStyle.setFont(font);
    }

    public enum Offset {
        /**
         * No offset.
         */
        NONE(Font.SS_NONE),

        /**
         * Superscript.
         */
        SUPER(Font.SS_SUPER),

        /**
         * Subscript.
         */
        SUB(Font.SS_SUB);

        private final short value;

        Offset(short value) {
            this.value = value;
        }

        public short getValue() {
            return this.value;
        }
    }

    public enum Underline {
        /**
         * No underline.
         */
        NONE(Font.U_NONE),

        /**
         * Single underline.
         */
        SINGLE(Font.U_SINGLE),

        /**
         * Single underline for accounting.
         */
        SINGLE_ACCOUNTING(Font.U_SINGLE_ACCOUNTING),

        /**
         * Double underline.
         */
        DOUBLE(Font.U_DOUBLE),

        /**
         * Double underline for accounting.
         */
        DOUBLE_ACCOUNTING(Font.U_DOUBLE_ACCOUNTING);

        private final byte value;

        Underline(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return this.value;
        }
    }

}
