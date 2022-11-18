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
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.model.StylesTable
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import spock.lang.Specification

class AlignmentsSpec extends Specification {

    def "Sets horizontal alignment to the cell style"() {
        given:
        def cellStyle = cellStyleImpl as CellStyle

        expect:
        HorizontalAlignment.values().each {
            // when
            Alignments.setHorizontal(cellStyle, it)

            // then
            assert cellStyle.alignment == it
        }

        where:
        cellStyleImpl << [
                HSSFCellStyle.newInstance(0 as short, new ExtendedFormatRecord(), null),
                new XSSFCellStyle(new StylesTable()),
        ]
    }

    def "Sets vertical alignment to the cell style"() {
        given:
        def cellStyle = cellStyleImpl as CellStyle

        expect:
        VerticalAlignment.values().each {
            // when
            Alignments.setVertical(cellStyle, it)

            // then
            assert cellStyle.verticalAlignment == it
        }

        where:
        cellStyleImpl << [
                HSSFCellStyle.newInstance(0 as short, new ExtendedFormatRecord(), null),
                new XSSFCellStyle(new StylesTable()),
        ]
    }

}
