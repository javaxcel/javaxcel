package com.github.javaxcel.core.out.core.impl


import spock.lang.Specification
import spock.lang.Subject
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

import org.apache.poi.hssf.usermodel.HSSFWorkbook
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
        def workbook = new HSSFWorkbook()

        and:
        // To create multiple sheets, generates models as many
        // as the amount exceeds the maximum number of rows per sheet.
        def mockCount = ExcelUtils.getMaxRows(workbook) * 1.1
        List<Map<String, Object>> maps = (0..<mockCount).collect { TestUtils.randomizeMap(keys) }

        and:
        def filePath = path.resolve("map-writer-sheet-rotation.xls")
        def out = Files.newOutputStream(filePath)

        when:
        Javaxcel.newInstance()
                .writer(workbook)
                .options(new SheetName("Sheet-Rotation"), new KeyNames(keys))
                .write(out, maps)

        then:
        def sheets = ExcelUtils.getSheets(ExcelUtils.getWorkbook(filePath.toFile()))
        sheets.size() == 2
        sheets*.sheetName == (1..sheets.size()).collect { "Sheet-Rotation" + it }

        and:
        def columns = sheets.collectMany { sheet -> sheet[0].collect { cell -> cell.stringCellValue } }.unique()
        columns == keys

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

}
