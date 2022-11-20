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
import org.apache.poi.ss.SpreadsheetVersion
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.model.StylesTable
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import spock.lang.Specification

import static org.apache.poi.ss.SpreadsheetVersion.EXCEL2007
import static org.apache.poi.ss.SpreadsheetVersion.EXCEL97

@SuppressWarnings("GroovyAssignabilityCheck")
class BackgroundsSpec extends Specification {

    def "Sets background color to the cell style"() {
        when:
        Backgrounds.setColor(cellStyle, color)

        then:
        cellStyle.fillForegroundColor == color.index

        where:
        [color, cellStyle] << [
                EnumSet.allOf(IndexedColors),
                [EXCEL97, EXCEL2007].collect { newCellStyle(it) },
        ].combinations()
    }

    def "Sets background pattern to the cell style"() {
        when:
        Backgrounds.setPattern(cellStyle, pattern)

        then:
        cellStyle.fillPattern == pattern

        where:
        [pattern, cellStyle] << [
                EnumSet.allOf(FillPatternType),
                [EXCEL97, EXCEL2007].collect { newCellStyle(it) },
        ].combinations()
    }

    // -------------------------------------------------------------------------------------------------

    private static CellStyle newCellStyle(SpreadsheetVersion spreadsheetVersion) {
        switch (spreadsheetVersion) {
            case EXCEL97:
                return HSSFCellStyle.newInstance(0 as short, new ExtendedFormatRecord(), null)
            case EXCEL2007:
                return new XSSFCellStyle(new StylesTable())
        }
    }

}
