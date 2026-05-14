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

package com.github.javaxcel.core.internal.template

import spock.lang.Specification
import spock.lang.Subject

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.ClientAnchor
import org.apache.poi.ss.usermodel.Comment
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Drawing
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@Subject(TemplateScanner)
class TemplateScannerSpec extends Specification {

    def "Sheet with no directives produces only static / substitution cells"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet("plain")
        def row = sheet.createRow(0)
        row.createCell(0).setCellValue("static")
        row.createCell(1).setCellValue('Hello ${name}!')
        row.createCell(2).setCellValue(42)

        when:
        def template = TemplateScanner.scanSheet(sheet)

        then:
        template.sheetName() == "plain"
        template.nodes().size() == 3
        template.nodes()[0] instanceof TemplateNode.StaticCell
        template.nodes()[1] instanceof TemplateNode.SubstitutionCell
        (template.nodes()[1] as TemplateNode.SubstitutionCell).text() == 'Hello ${name}!'
        template.nodes()[2] instanceof TemplateNode.StaticCell

        cleanup:
        workbook.close()
    }

    def "Single 'each' directive on a row produces one IterationBlock containing the row's cells"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet("each")
        def row = sheet.createRow(0)
        def directiveCell = row.createCell(0)
        directiveCell.setCellValue('${item.id}')
        addDirectiveComment(workbook, sheet, directiveCell, "jxc: each items as item")
        row.createCell(1).setCellValue('${item.name}')

        when:
        def template = TemplateScanner.scanSheet(sheet)

        then:
        template.nodes().size() == 1
        def block = template.nodes()[0] as TemplateNode.IterationBlock
        block.collectionExpr() == "items"
        block.varName() == "item"
        block.children().size() == 2
        block.children()[0] instanceof TemplateNode.SubstitutionCell
        block.children()[1] instanceof TemplateNode.SubstitutionCell

        cleanup:
        workbook.close()
    }

    def "Nested each — outer block contains inner block"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet("nested")
        // Outer each at A1, until C3 — covers rows 1-3
        def outerCell = sheet.createRow(0).createCell(0)
        outerCell.setCellValue('${item.id}')
        addDirectiveComment(workbook, sheet, outerCell, "jxc: each items as item, until: C3")
        sheet.getRow(0).createCell(1).setCellValue('${item.name}')
        sheet.getRow(0).createCell(2).setCellValue("static")

        // Inner each at A2, until C2 — single row inside outer block
        def innerCell = sheet.createRow(1).createCell(0)
        innerCell.setCellValue('${child.id}')
        addDirectiveComment(workbook, sheet, innerCell, "jxc: each item.children as child, until: C2")
        sheet.getRow(1).createCell(1).setCellValue('${child.name}')

        // Footer row inside outer block
        sheet.createRow(2).createCell(0).setCellValue("subtotal")

        when:
        def template = TemplateScanner.scanSheet(sheet)

        then: "Outer block holds inner block + outer-only cells"
        template.nodes().size() == 1
        def outer = template.nodes()[0] as TemplateNode.IterationBlock
        outer.collectionExpr() == "items"
        // outer.children should contain: inner block + outer's own cells (row 0 cells, row 2 cell)
        outer.children().any { it instanceof TemplateNode.IterationBlock }

        and: "Inner block has its own children"
        def inner = outer.children().find { it instanceof TemplateNode.IterationBlock } as TemplateNode.IterationBlock
        inner.collectionExpr() == "item.children"
        inner.varName() == "child"
        inner.children().size() == 2

        cleanup:
        workbook.close()
    }

    def "'if' directive produces ConditionalBlock"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet("conditional")
        def cell = sheet.createRow(0).createCell(0)
        cell.setCellValue('Total: ${items.size()}')
        addDirectiveComment(workbook, sheet, cell, "jxc: if items.size() > 0")

        when:
        def template = TemplateScanner.scanSheet(sheet)

        then:
        template.nodes().size() == 1
        def block = template.nodes()[0] as TemplateNode.ConditionalBlock
        block.conditionExpr() == "items.size() > 0"
        block.children().size() == 1
        block.children()[0] instanceof TemplateNode.SubstitutionCell

        cleanup:
        workbook.close()
    }

    def "scan(workbook) produces a SheetTemplate per sheet"() {
        given:
        def workbook = new XSSFWorkbook()
        def s1 = workbook.createSheet("first")
        s1.createRow(0).createCell(0).setCellValue("a")
        def s2 = workbook.createSheet("second")
        s2.createRow(0).createCell(0).setCellValue("b")

        when:
        def templates = TemplateScanner.scan(workbook)

        then:
        templates.size() == 2
        templates*.sheetName() == ["first", "second"]

        cleanup:
        workbook.close()
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

}
