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
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

import static com.github.javaxcel.styler.role.Backgrounds.drawPattern;
import static com.github.javaxcel.styler.role.Backgrounds.dye;

public class Configurer {

    // Allows detail configurers to access it.
    final CellStyle cellStyle;

    // Allows detail configurers to access it.
    final Font font;

    public Configurer(CellStyle cellStyle, Font font) {
        if (cellStyle == null) throw new IllegalArgumentException("Configurer.cellStyle is not allowed to be null");
        if (font == null) throw new IllegalArgumentException("Configurer.font is not allowed to be null");

        this.cellStyle = cellStyle;
        this.font = font;
    }

    /**
     * Configures alignments.
     *
     * @return alignment configurer
     */
    public AlignmentConfigurer alignment() {
        return new AlignmentConfigurer(this);
    }

    /**
     * Configures background.
     *
     * @param pattern background pattern
     * @param color   background color
     * @return configurer
     */
    public Configurer background(FillPatternType pattern, IndexedColors color) {
        drawPattern(cellStyle, pattern);
        dye(cellStyle, color);
        return this;
    }

    /**
     * Configures borders.
     *
     * @return border configurer
     */
    public BorderConfigurer border() {
        return new BorderConfigurer(this);
    }

    /**
     * Configures fonts.
     *
     * @return font configurer
     */
    public FontConfigurer font() {
        return new FontConfigurer(this);
    }

}
