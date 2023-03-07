/*
 * Copyright 2022 Javaxcel
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

package com.github.javaxcel.styler.config

import spock.lang.Specification

import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment

import com.github.javaxcel.styler.role.Fonts.Offset
import com.github.javaxcel.styler.role.Fonts.Underline

class ConfigurerSpec extends Specification {

    def "Customizes style with configurer"() {
        given:
        def cellStyle = Mock(CellStyle)
        def font = Mock(Font)
        def configurer = new Configurer(cellStyle, font)

        when:
        configurer
                .alignment()
                .horizontal(HorizontalAlignment.CENTER)
                .vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.GREY_25_PERCENT)
                .border()
                .all(BorderStyle.THIN, IndexedColors.BLACK)
                .and()
                .font()
                .name("Courier New")
                .size(16)
                .color(IndexedColors.WHITE)
                .bold()
                .italic()
                .strikeout()
                .underline(Underline.SINGLE)
                .offset(Offset.NONE)
                .and()

        then:
        noExceptionThrown()
    }

}
