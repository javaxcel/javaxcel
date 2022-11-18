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

import com.github.javaxcel.styler.role.Alignments;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

public final class AlignmentConfigurer {

    private final Configurer configurer;

    private final CellStyle cellStyle;

    AlignmentConfigurer(Configurer configurer) {
        this.configurer = configurer;
        this.cellStyle = configurer.cellStyle;
    }

    public AlignmentConfigurer horizontal(HorizontalAlignment horizontal) {
        Alignments.horizontal(this.cellStyle, horizontal);
        return this;
    }

    public AlignmentConfigurer vertical(VerticalAlignment vertical) {
        Alignments.vertical(this.cellStyle, vertical);
        return this;
    }

    public Configurer and() {
        return this.configurer;
    }

}
