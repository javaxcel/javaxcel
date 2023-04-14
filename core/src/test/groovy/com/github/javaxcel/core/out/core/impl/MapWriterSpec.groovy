package com.github.javaxcel.core.out.core.impl

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import com.github.pjfanning.xlsx.StreamingReader

import io.github.imsejin.common.util.StringUtils

import com.github.javaxcel.core.Javaxcel
import com.github.javaxcel.core.out.strategy.impl.DefaultValue
import com.github.javaxcel.core.out.strategy.impl.KeyNames
import com.github.javaxcel.core.out.strategy.impl.SheetName
import com.github.javaxcel.core.util.ExcelUtils
import com.github.javaxcel.core.util.ObjectUtils
import com.github.javaxcel.test.util.TestUtils

@Subject(MapWriter)
class MapWriterSpec extends Specification {

    @TempDir
    private Path path

    def "Writes maps into Excel file"() {
        given:
        def keys = ["A", "B", "C", "D", "E", "F"]
        def mockCount = 1024
        List<Map<String, Object>> maps = (0..<mockCount).collect { TestUtils.randomizeMap(keys) }

        and:
        def filePath = path.resolve("map-writer-basic.xlsx")
        def out = Files.newOutputStream(filePath)

        when:
        Javaxcel.newInstance()
                .writer(new SXSSFWorkbook())
                .write(out, maps)

        then: "Excel files exists"
        Files.isRegularFile(filePath)

        and: "The file has rows as many as the number of maps except a header"
        def workbook = StreamingReader.builder().open(filePath.toFile())
        ExcelUtils.getNumOfModels(workbook) == mockCount

        and: "The header names are the same as the keys of map, but order of header is not guaranteed"
        workbook[0][0].collect { it.stringCellValue }.toSet() == keys.toSet()

        cleanup:
        out.close()
    }

    def "Writes maps into each sheet with rotation"() {
        given:
        def keys = ["ALPHA", "BETA", "GAMMA"]
        def hSSFWorkbook = new HSSFWorkbook()

        and:
        // To create multiple sheets, generates models as many
        // as the amount exceeds the maximum number of rows per sheet.
        def mockCount = ExcelUtils.getMaxRows(hSSFWorkbook) * 1.1
        List<Map<String, Object>> maps = (0..<mockCount).collect { TestUtils.randomizeMap(keys) }

        and:
        def filePath = path.resolve("map-writer-sheet-rotation.xls")
        def out = Files.newOutputStream(filePath)

        when:
        Javaxcel.newInstance()
                .writer(hSSFWorkbook)
                .options(new SheetName("Sheet-Rotation"))
                .write(out, maps)

        then:
        def workbook = ExcelUtils.getWorkbook(filePath.toFile())
        workbook.numberOfSheets == 2
        workbook*.sheetName == (1..workbook.numberOfSheets).collect { "Sheet-Rotation" + it }

        and:
        def headers = workbook.collect { sheet -> sheet[0].collect { it.stringCellValue }.toSet() }
        headers == [keys.toSet()] * workbook.numberOfSheets

        cleanup:
        out.close()
    }

    def "Writes maps with default value option"() {
        given:
        def defaultValue = "(default)"
        def maps = [
                [alpha: null, beta: 64],
                [alpha: "John", beta: null],
                [alpha: "", beta: 8],
        ]

        and:
        def filePath = path.resolve("map-writer-default-value.xlsx")
        def out = Files.newOutputStream(filePath)

        when:
        Javaxcel.newInstance()
                .writer(new SXSSFWorkbook())
                .options(new DefaultValue(defaultValue))
                .write(out, maps)

        then:
        def workbook = StreamingReader.builder().open(filePath.toFile())
        ExcelUtils.getNumOfModels(workbook) == maps.size()

        and: "There is no empty value on cell"
        def cellValues = workbook.collectMany { sheet -> sheet.collectMany { row -> row.collect { it.stringCellValue } } }
        cellValues.every { !StringUtils.isNullOrEmpty(it) }

        and: "Empty values must be converted as the given default value"
        def emptyMapValueCount = maps.collectMany { it.values() }.count { ObjectUtils.isNullOrEmptyCharSequence(it) }
        def emptyCellValueCount = cellValues.count { it == defaultValue }
        emptyMapValueCount == emptyCellValueCount

        cleanup:
        out.close()
    }

    def "Writes maps with key names option"() {
        given:
        def hssfWorkbook = new HSSFWorkbook()

        and:
        // To create multiple sheets, generates models as many
        // as the amount exceeds the maximum number of rows per sheet.
        def mockCount = ExcelUtils.getMaxRows(hssfWorkbook) * 1.1
        List<Map<String, Object>> maps = (0..<mockCount).collect {
            TestUtils.randomizeMap(4) {
                "FIELD_" + (it + 1)
            }
        }

        and:
        def filePath = path.resolve("map-writer-key-names.xls")
        def out = Files.newOutputStream(filePath)

        when:
        Javaxcel.newInstance()
                .writer(hssfWorkbook)
                .options(keyNames)
                .write(out, maps)

        then: "All header names at each sheet must be sorted"
        def workbook = ExcelUtils.getWorkbook(filePath.toFile())
        def headers = workbook.collect { sheet -> sheet[0].collect { it.stringCellValue } }
        workbook.numberOfSheets == headers.size()

        and:
        headers == [expected] * workbook.numberOfSheets

        cleanup:
        out.close()

        where:
        keyNames                                                                          | expected
        new KeyNames((1..4).collect { "FIELD_" + it })                                    | (1..4).collect { "FIELD_" + it }
        new KeyNames((1..4).collect { "FIELD_" + it }, (1..4).collect { "column_" + it }) | (1..4).collect { "column_" + it }
    }

    def "Fails to write maps with key names option"() {
        given:
        def workbook = Mock(Workbook)

        when:
        Javaxcel.newInstance()
                .writer(workbook)
                .options(keyNames.call())
                .write(null, maps)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.startsWith(message)

        where:
        maps                 | keyNames                                      | message
        []                   | { new KeyNames([]) }                          | "keyOrders is not allowed to be null or empty"
        [[a: 1, b: 2]]       | { new KeyNames(["A", "B", "C"], ["a", "b"]) } | "newKeyNames.size is not equal to keyOrders.size"
        [[A: 1, B: 2]]       | { new KeyNames(["A", "B", "C"]) }             | "MapWriter.keys is not equal to keyMap.orders.size"
        [[A: 1, b: 2, C: 3]] | { new KeyNames(["A", "B", "C"]) }             | "MapWriter.keys is at variance with keyMap.orders.keySet"
    }

}
