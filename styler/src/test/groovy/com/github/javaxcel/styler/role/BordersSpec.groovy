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
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.model.StylesTable
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import spock.lang.Specification

import static org.apache.poi.ss.SpreadsheetVersion.EXCEL2007
import static org.apache.poi.ss.SpreadsheetVersion.EXCEL97

@SuppressWarnings("GroovyAssignabilityCheck")
class BordersSpec extends Specification {

    def "Sets border style to the cell style"() {
        when:
        Borders.setTopStyle(cellStyle, border)
        Borders.setRightStyle(cellStyle, border)
        Borders.setBottomStyle(cellStyle, border)
        Borders.setLeftStyle(cellStyle, border)

        then:
        cellStyle.borderTop == border
        cellStyle.borderRight == border
        cellStyle.borderBottom == border
        cellStyle.borderLeft == border

        where:
        [border, cellStyle] << [
                EnumSet.allOf(BorderStyle),
                [EXCEL97, EXCEL2007].collect { newCellStyle(it) },
        ].combinations()
    }

    def "Sets border color to the cell style"() {
        when:
        Borders.setTopColor(cellStyle, color)
        Borders.setRightColor(cellStyle, color)
        Borders.setBottomColor(cellStyle, color)
        Borders.setLeftColor(cellStyle, color)

        then:
        assert cellStyle.topBorderColor == color.index
        assert cellStyle.rightBorderColor == color.index
        assert cellStyle.bottomBorderColor == color.index
        assert cellStyle.leftBorderColor == color.index

        where:
        [color, cellStyle] << [
                EnumSet.allOf(IndexedColors),
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
