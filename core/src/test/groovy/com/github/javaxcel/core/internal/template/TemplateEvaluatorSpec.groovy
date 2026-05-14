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
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@Subject(TemplateEvaluator)
class TemplateEvaluatorSpec extends Specification {

    def "Substitutes expression cells with values from the context"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet()
        sheet.createRow(0).createCell(0).setCellValue('Hello ${name}!')
        sheet.createRow(1).createCell(0).setCellValue('Total: ${total}')
        sheet.createRow(2).createCell(0).setCellValue('${count}')

        when:
        new TemplateEvaluator().evaluate(workbook, [name: "Alice", total: 42, count: 7])

        then: "Mixed text + expression returns String"
        sheet.getRow(0).getCell(0).getStringCellValue() == "Hello Alice!"
        sheet.getRow(1).getCell(0).getStringCellValue() == "Total: 42"

        and: "Single full-cell expression preserves native typed value"
        sheet.getRow(2).getCell(0).getNumericCellValue() == 7.0d

        cleanup:
        workbook.close()
    }

    def "Static cells are preserved verbatim"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet()
        sheet.createRow(0).createCell(0).setCellValue("Plain text")
        sheet.createRow(1).createCell(0).setCellValue(123.45)
        sheet.createRow(2).createCell(0).setCellValue(true)

        when:
        new TemplateEvaluator().evaluate(workbook, [:])

        then:
        sheet.getRow(0).getCell(0).getStringCellValue() == "Plain text"
        sheet.getRow(1).getCell(0).getNumericCellValue() == 123.45d
        sheet.getRow(2).getCell(0).getBooleanCellValue() == true

        cleanup:
        workbook.close()
    }

    def "each: replicates the row for each item in the collection"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet()
        def directiveCell = sheet.createRow(0).createCell(0)
        directiveCell.setCellValue('${item.id}')
        addDirectiveComment(workbook, sheet, directiveCell, "jxc: each items as item")
        sheet.getRow(0).createCell(1).setCellValue('${item.name}')

        and:
        def items = [
                new Item(id: 1, name: "Alpha"),
                new Item(id: 2, name: "Beta"),
                new Item(id: 3, name: "Gamma"),
        ]

        when:
        new TemplateEvaluator().evaluate(workbook, [items: items])

        then:
        sheet.getRow(0).getCell(0).getNumericCellValue() == 1.0d
        sheet.getRow(0).getCell(1).getStringCellValue() == "Alpha"
        sheet.getRow(1).getCell(0).getNumericCellValue() == 2.0d
        sheet.getRow(1).getCell(1).getStringCellValue() == "Beta"
        sheet.getRow(2).getCell(0).getNumericCellValue() == 3.0d
        sheet.getRow(2).getCell(1).getStringCellValue() == "Gamma"

        cleanup:
        workbook.close()
    }

    def "Top-level static row + each + aggregate row stack correctly"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet()
        // Row 0: header (static)
        sheet.createRow(0).createCell(0).setCellValue("Header")
        // Row 1: each block
        def each = sheet.createRow(1).createCell(0)
        each.setCellValue('${item}')
        addDirectiveComment(workbook, sheet, each, "jxc: each items as item")
        // Row 2: aggregate
        sheet.createRow(2).createCell(0).setCellValue('count: ${items.size()}')

        when:
        new TemplateEvaluator().evaluate(workbook, [items: ["a", "b", "c"]])

        then:
        sheet.getRow(0).getCell(0).getStringCellValue() == "Header"
        sheet.getRow(1).getCell(0).getStringCellValue() == "a"
        sheet.getRow(2).getCell(0).getStringCellValue() == "b"
        sheet.getRow(3).getCell(0).getStringCellValue() == "c"
        sheet.getRow(4).getCell(0).getStringCellValue() == "count: 3"

        cleanup:
        workbook.close()
    }

    def "if: skips block when condition is false — subsequent row shifts up"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet()
        def cell = sheet.createRow(0).createCell(0)
        cell.setCellValue('Visible only when there are items: ${items.size()}')
        addDirectiveComment(workbook, sheet, cell, "jxc: if items.size() > 0")
        sheet.createRow(1).createCell(0).setCellValue("Always visible")

        when:
        new TemplateEvaluator().evaluate(workbook, [items: []])

        then: "Conditional row dropped → 'Always visible' shifts up to row 0"
        sheet.getRow(0).getCell(0).getStringCellValue() == "Always visible"
        sheet.getRow(1) == null

        cleanup:
        workbook.close()
    }

    def "if: emits block when condition is true"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet()
        def cell = sheet.createRow(0).createCell(0)
        cell.setCellValue('Total items: ${items.size()}')
        addDirectiveComment(workbook, sheet, cell, "jxc: if items.size() > 0")
        sheet.createRow(1).createCell(0).setCellValue("After")

        when:
        new TemplateEvaluator().evaluate(workbook, [items: ["a", "b"]])

        then:
        sheet.getRow(0).getCell(0).getStringCellValue() == "Total items: 2"
        sheet.getRow(1).getCell(0).getStringCellValue() == "After"

        cleanup:
        workbook.close()
    }

    def "Map context is accessible via dot notation"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet()
        sheet.createRow(0).createCell(0).setCellValue('${author.name}')

        when:
        new TemplateEvaluator().evaluate(workbook, [author: [name: "Bob"]])

        then:
        sheet.getRow(0).getCell(0).getStringCellValue() == "Bob"

        cleanup:
        workbook.close()
    }

    def "POJO context via toContextMap"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet()
        sheet.createRow(0).createCell(0).setCellValue('${total}')

        and:
        def pojo = new Invoice(total: 999)

        when:
        def ctx = TemplateEvaluator.toContextMap(pojo)
        new TemplateEvaluator().evaluate(workbook, ctx)

        then:
        sheet.getRow(0).getCell(0).getNumericCellValue() == 999.0d

        cleanup:
        workbook.close()
    }

    def "Empty collection in each produces zero output rows"() {
        given:
        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet()
        def each = sheet.createRow(0).createCell(0)
        each.setCellValue('${item}')
        addDirectiveComment(workbook, sheet, each, "jxc: each items as item")
        sheet.createRow(1).createCell(0).setCellValue("After")

        when:
        new TemplateEvaluator().evaluate(workbook, [items: []])

        then: "After-row is shifted up to row 0"
        sheet.getRow(0).getCell(0).getStringCellValue() == "After"

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

    static class Item {
        Integer id
        String name
    }

    static class Invoice {
        Integer total
    }

}
