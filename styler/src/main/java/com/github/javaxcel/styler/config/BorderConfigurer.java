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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

import static com.github.javaxcel.styler.role.Borders.setBottomStyle;
import static com.github.javaxcel.styler.role.Borders.setLeftStyle;
import static com.github.javaxcel.styler.role.Borders.setRightStyle;
import static com.github.javaxcel.styler.role.Borders.setTopStyle;
import static com.github.javaxcel.styler.role.Borders.setBottomColor;
import static com.github.javaxcel.styler.role.Borders.setLeftColor;
import static com.github.javaxcel.styler.role.Borders.setRightColor;
import static com.github.javaxcel.styler.role.Borders.setTopColor;

public final class BorderConfigurer {

    private final Configurer configurer;

    private final CellStyle cellStyle;

    BorderConfigurer(Configurer configurer) {
        this.configurer = configurer;
        this.cellStyle = configurer.cellStyle;
    }

    public BorderConfigurer top(BorderStyle border, IndexedColors color) {
        setTopStyle(cellStyle, border);
        setTopColor(cellStyle, color);
        return this;
    }

    public BorderConfigurer right(BorderStyle border, IndexedColors color) {
        setRightStyle(cellStyle, border);
        setRightColor(cellStyle, color);
        return this;
    }

    public BorderConfigurer bottom(BorderStyle border, IndexedColors color) {
        setBottomStyle(cellStyle, border);
        setBottomColor(cellStyle, color);
        return this;
    }

    public BorderConfigurer left(BorderStyle border, IndexedColors color) {
        setLeftStyle(cellStyle, border);
        setLeftColor(cellStyle, color);
        return this;
    }

    public BorderConfigurer topAndBottom(BorderStyle border, IndexedColors color) {
        setTopStyle(cellStyle, border);
        setTopColor(cellStyle, color);
        setBottomStyle(cellStyle, border);
        setBottomColor(cellStyle, color);
        return this;
    }

    public BorderConfigurer leftAndRight(BorderStyle border, IndexedColors color) {
        setLeftStyle(cellStyle, border);
        setLeftColor(cellStyle, color);
        setRightStyle(cellStyle, border);
        setRightColor(cellStyle, color);
        return this;
    }

    public BorderConfigurer all(BorderStyle border, IndexedColors color) {
        // Border
        setTopStyle(cellStyle, border);
        setRightStyle(cellStyle, border);
        setBottomStyle(cellStyle, border);
        setLeftStyle(cellStyle, border);

        // Color
        setTopColor(cellStyle, color);
        setRightColor(cellStyle, color);
        setBottomColor(cellStyle, color);
        setLeftColor(cellStyle, color);

        return this;
    }

    public Configurer and() {
        return this.configurer;
    }

}
