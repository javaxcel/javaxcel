package com.github.javaxcel.core.out.core.impl

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

import org.apache.poi.xssf.streaming.SXSSFWorkbook

import com.github.pjfanning.xlsx.StreamingReader

import com.github.javaxcel.core.Javaxcel
import com.github.javaxcel.core.util.ExcelUtils
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
        def filePath = path.resolve("maps-${System.currentTimeMillis()}.xlsx")
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

}
