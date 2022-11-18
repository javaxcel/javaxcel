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

package com.github.javaxcel.styler.role

import org.apache.poi.hssf.record.ExtendedFormatRecord
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.model.StylesTable
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import spock.lang.Specification

class BackgroundsSpec extends Specification {

    def "Sets background color to the cell style"() {
        given:
        def cellStyle = cellStyleImpl as CellStyle

        expect:
        IndexedColors.values().each {
            // when
            Backgrounds.setColor(cellStyle, it)

            // then
            assert cellStyle.fillForegroundColor == it.index
        }

        where:
        cellStyleImpl << [
                HSSFCellStyle.newInstance(0 as short, new ExtendedFormatRecord(), null),
                new XSSFCellStyle(new StylesTable()),
        ]
    }

    def "Sets background pattern to the cell style"() {
        given:
        def cellStyle = cellStyleImpl as CellStyle

        expect:
        FillPatternType.values().each {
            // when
            Backgrounds.setPattern(cellStyle, it)

            // then
            assert cellStyle.fillPattern == it
        }

        where:
        cellStyleImpl << [
                HSSFCellStyle.newInstance(0 as short, new ExtendedFormatRecord(), null),
                new XSSFCellStyle(new StylesTable()),
        ]
    }

}
