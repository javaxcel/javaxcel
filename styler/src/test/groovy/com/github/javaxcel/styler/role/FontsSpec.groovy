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

import io.github.imsejin.common.tool.RandomString
import org.apache.poi.hssf.record.ExtendedFormatRecord
import org.apache.poi.hssf.record.FontRecord
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFFont
import org.apache.poi.ss.SpreadsheetVersion
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.model.StylesTable
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import spock.lang.Specification

import static org.apache.poi.ss.SpreadsheetVersion.EXCEL2007
import static org.apache.poi.ss.SpreadsheetVersion.EXCEL97

class FontsSpec extends Specification {

    static Random RANDOM = new Random()
    static RandomString RANDOM_STRING = new RandomString()

    def "Sets font name to the cell style"() {
        given:
        def name = RANDOM_STRING.nextString(8)

        when:
        Fonts.setName(cellStyle, font, name)

        then:
        font.fontName == name

        where:
        cellStyle               | font
        newCellStyle(EXCEL97)   | newFont(EXCEL97)
        newCellStyle(EXCEL2007) | newFont(EXCEL2007)
    }

    def "Sets font size to the cell style"() {
        given:
        def size = RANDOM.nextInt(Short.MAX_VALUE / Font.TWIPS_PER_POINT as int)

        when:
        Fonts.setSize(cellStyle, font, size)

        then:
        font.fontHeightInPoints == size as short

        where:
        cellStyle               | font
        newCellStyle(EXCEL97)   | newFont(EXCEL97)
        newCellStyle(EXCEL2007) | newFont(EXCEL2007)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    def "Sets font color to the cell style"() {
        given:
        def color = randomEnum(IndexedColors)

        when:
        Fonts.setColor(cellStyle, font, color)

        then:
        font.color == color.index

        where:
        cellStyle               | font
        newCellStyle(EXCEL97)   | newFont(EXCEL97)
        newCellStyle(EXCEL2007) | newFont(EXCEL2007)
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

    private static Font newFont(SpreadsheetVersion spreadsheetVersion) {
        switch (spreadsheetVersion) {
            case EXCEL97:
                return HSSFFont.newInstance(0 as short, new FontRecord())
            case EXCEL2007:
                return new XSSFFont()
        }
    }

    private static <T extends Enum<T>> T randomEnum(Class<T> type) {
        def values = type.getMethod("values").invoke(null) as T[]
        def ordinal = RANDOM.nextInt(values.length)
        values[ordinal]
    }

}
