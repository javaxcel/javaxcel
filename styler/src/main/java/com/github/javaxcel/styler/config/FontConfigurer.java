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

package com.github.javaxcel.styler.config;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.github.javaxcel.styler.role.Fonts;

public final class FontConfigurer {

    private final Configurer configurer;

    private final CellStyle cellStyle;

    private final Font font;

    FontConfigurer(Configurer configurer) {
        this.configurer = configurer;
        this.cellStyle = configurer.cellStyle;
        this.font = configurer.font;
    }

    public FontConfigurer name(String name) {
        Fonts.setName(cellStyle, font, name);
        return this;
    }

    public FontConfigurer size(int size) {
        Fonts.setSize(cellStyle, font, size);
        return this;
    }

    public FontConfigurer color(IndexedColors color) {
        Fonts.setColor(cellStyle, font, color);
        return this;
    }

    public FontConfigurer bold() {
        Fonts.bold(cellStyle, font);
        return this;
    }

    public FontConfigurer italic() {
        Fonts.italic(cellStyle, font);
        return this;
    }

    public FontConfigurer strikeout() {
        Fonts.strikeout(cellStyle, font);
        return this;
    }

    public FontConfigurer underline(Fonts.Underline underline) {
        Fonts.setUnderline(cellStyle, font, underline);
        return this;
    }

    public FontConfigurer offset(Fonts.Offset offset) {
        Fonts.setOffset(cellStyle, font, offset);
        return this;
    }

    public Configurer and() {
        return this.configurer;
    }

}
