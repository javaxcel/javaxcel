/*
 * Copyright 2026 Javaxcel
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

package com.github.javaxcel.core.out.template.impl

import spock.lang.Specification
import spock.lang.Subject

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.ClientAnchor
import org.apache.poi.ss.usermodel.Comment
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Drawing
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.github.javaxcel.core.Javaxcel

@Subject(DefaultExcelTemplateWriter)
class DefaultExcelTemplateWriterSpec extends Specification {

    def "Builder-style with(name, value) accumulates variables"() {
        given:
        def template = new XSSFWorkbook()
        def sheet = template.createSheet()
        sheet.createRow(0).createCell(0).setCellValue('${greeting}, ${name}!')

        and:
        def out = new ByteArrayOutputStream()

        when:
        Javaxcel.newInstance()
                .templateWriter(template)
                .with("greeting", "Hello")
                .with("name", "World")
                .write(out)

        then:
        def result = WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray()))
        result.getSheetAt(0).getRow(0).getCell(0).getStringCellValue() == "Hello, World!"

        cleanup:
        result?.close()
    }

    def "Map context renders SpEL expressions"() {
        given:
        def template = new XSSFWorkbook()
        def sheet = template.createSheet()
        sheet.createRow(0).createCell(0).setCellValue('${author.name}')
        sheet.createRow(1).createCell(0).setCellValue('${total}')

        and:
        def out = new ByteArrayOutputStream()

        when:
        Javaxcel.newInstance()
                .templateWriter(template)
                .write(out, [author: [name: "Carol"], total: 99])

        then:
        def result = WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray()))
        result.getSheetAt(0).getRow(0).getCell(0).getStringCellValue() == "Carol"
        result.getSheetAt(0).getRow(1).getCell(0).getNumericCellValue() == 99.0d

        cleanup:
        result?.close()
    }

    def "POJO context renders via reflection"() {
        given:
        def template = new XSSFWorkbook()
        def sheet = template.createSheet()
        sheet.createRow(0).createCell(0).setCellValue('${title}')
        sheet.createRow(1).createCell(0).setCellValue('${total}')

        and:
        def out = new ByteArrayOutputStream()
        def pojo = new Invoice(title: "INV-001", total: 1234)

        when:
        Javaxcel.newInstance()
                .templateWriter(template)
                .write(out, pojo)

        then:
        def result = WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray()))
        result.getSheetAt(0).getRow(0).getCell(0).getStringCellValue() == "INV-001"
        result.getSheetAt(0).getRow(1).getCell(0).getNumericCellValue() == 1234.0d

        cleanup:
        result?.close()
    }

    def "Each + aggregate row — invoice-like template round-trip"() {
        given:
        def template = new XSSFWorkbook()
        def sheet = template.createSheet("Invoice")
        // Row 0: header
        sheet.createRow(0).createCell(0).setCellValue('Invoice for ${author.name}')
        // Row 1: each (single-row block A2:B2)
        def each = sheet.createRow(1).createCell(0)
        each.setCellValue('${item.name}')
        addDirectiveComment(template, sheet, each, "jxc: each items as item, until: B2")
        sheet.getRow(1).createCell(1).setCellValue('${item.price}')
        // Row 2: footer
        sheet.createRow(2).createCell(0).setCellValue('Total items: ${items.size()}')

        and:
        def out = new ByteArrayOutputStream()
        def context = [
                author: [name: "Bob"],
                items : [
                        [name: "Widget", price: 10],
                        [name: "Gadget", price: 25],
                        [name: "Doohickey", price: 7],
                ],
        ]

        when:
        Javaxcel.newInstance()
                .templateWriter(template)
                .write(out, context)

        then:
        def result = WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray()))
        def s = result.getSheetAt(0)
        s.getRow(0).getCell(0).getStringCellValue() == "Invoice for Bob"
        s.getRow(1).getCell(0).getStringCellValue() == "Widget"
        s.getRow(1).getCell(1).getNumericCellValue() == 10.0d
        s.getRow(2).getCell(0).getStringCellValue() == "Gadget"
        s.getRow(2).getCell(1).getNumericCellValue() == 25.0d
        s.getRow(3).getCell(0).getStringCellValue() == "Doohickey"
        s.getRow(3).getCell(1).getNumericCellValue() == 7.0d
        s.getRow(4).getCell(0).getStringCellValue() == "Total items: 3"

        cleanup:
        result?.close()
    }

    def "Nested each — items with children"() {
        given:
        def template = new XSSFWorkbook()
        def sheet = template.createSheet()
        // Outer each: rows 0-1 (block A1:A2)
        def outer = sheet.createRow(0).createCell(0)
        outer.setCellValue('${item.name}')
        addDirectiveComment(template, sheet, outer, "jxc: each items as item, until: A2")
        // Inner each on row 1 only — replicates row 1 per child of current item (block A2:A2)
        def inner = sheet.createRow(1).createCell(0)
        inner.setCellValue('  - ${child.name}')
        addDirectiveComment(template, sheet, inner, "jxc: each item.children as child, until: A2")

        and:
        def out = new ByteArrayOutputStream()
        def context = [
                items: [
                        [name: "Group A", children: [[name: "a1"], [name: "a2"]]],
                        [name: "Group B", children: [[name: "b1"]]],
                ],
        ]

        when:
        Javaxcel.newInstance()
                .templateWriter(template)
                .write(out, context)

        then:
        def result = WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray()))
        def s = result.getSheetAt(0)
        // Outer iteration 1: Group A, then a1, a2
        s.getRow(0).getCell(0).getStringCellValue() == "Group A"
        s.getRow(1).getCell(0).getStringCellValue() == "  - a1"
        s.getRow(2).getCell(0).getStringCellValue() == "  - a2"
        // Outer iteration 2: Group B, then b1
        s.getRow(3).getCell(0).getStringCellValue() == "Group B"
        s.getRow(4).getCell(0).getStringCellValue() == "  - b1"

        cleanup:
        result?.close()
    }

    def "Javaxcel.templateWriter rejects null workbook"() {
        when:
        Javaxcel.newInstance().templateWriter(null)

        then:
        thrown(IllegalArgumentException)
    }

    private static void addDirectiveComment(Workbook workbook, Sheet sheet, Cell cell, String text) {
        CreationHelper helper = workbook.getCreationHelper()
        Drawing drawing = sheet.createDrawingPatriarch()
        ClientAnchor anchor = helper.createClientAnchor()
        anchor.setCol1(cell.getColumnIndex())
        anchor.setCol2(cell.getColumnIndex() + 1)
        anchor.setRow1(cell.getRowIndex())
        anchor.setRow2(cell.getRowIndex() + 1)
        Comment comment = drawing.createCellComment(anchor)
        comment.setString(helper.createRichTextString(text))
        cell.setCellComment(comment)
    }

    static class Invoice {
        String title
        Integer total
    }

}
