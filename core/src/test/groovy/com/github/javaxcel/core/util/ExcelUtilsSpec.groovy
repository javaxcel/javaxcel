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

package com.github.javaxcel.core.util

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import io.github.imsejin.common.tool.RandomString

import static com.github.javaxcel.core.util.ExcelUtils.*

@Subject(ExcelUtils)
class ExcelUtilsSpec extends Specification {

    @TempDir
    private Path tempPath

    def "Gets an instance of workbook"() {
        given:
        def createWorkbookPath = { Workbook workbook ->
            def extension = workbook instanceof HSSFWorkbook ? EXCEL_97_EXTENSION : EXCEL_2007_EXTENSION
            def fileName = "${new RandomString().nextString(8)}.$extension"
            def tempFile = tempPath.resolve(fileName)
            workbook.write(Files.newOutputStream(tempFile))
            tempFile
        }

        when: "Writes Excel 97 file"
        def workbookPath = createWorkbookPath(new HSSFWorkbook())
        def workbook = getWorkbook(workbookPath.toFile())

        then:
        workbook != null
        workbook instanceof HSSFWorkbook

        when: "Writes Excel 2007 file"
        workbookPath = createWorkbookPath(new XSSFWorkbook())
        workbook = getWorkbook(workbookPath.toFile())

        then:
        workbook != null
        workbook instanceof XSSFWorkbook

        when: "Writes empty file"
        workbookPath = tempPath.resolve("${new RandomString().nextString(8)}.xlsx")
        Files.createFile(workbookPath)
        getWorkbook(workbookPath.toFile())

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "The supplied file was empty (zero bytes long)"
    }

    def "Gets the number of rows on sheet by sheet"() {
        given:
        def url = Thread.currentThread().contextClassLoader.getResource("spreadsheets/products.xlsx")
        def file = Paths.get(url.toURI()).toFile()
        def workbook = getWorkbook(file)
        def sheets = getSheets(workbook)

        when:
        def rowCounts = sheets.collect { getNumOfRows(it) }

        then:
        rowCounts == [32768, 65536, 16384, 8192]

        cleanup:
        workbook.close()
    }

    def "Gets the number of rows on all sheets by workbook"() {
        given:
        def url = Thread.currentThread().contextClassLoader.getResource("spreadsheets/products.xlsx")
        def file = Paths.get(url.toURI()).toFile()
        def workbook = getWorkbook(file)

        when:
        def rowCount = getNumOfRows(workbook)

        then:
        rowCount == 122880

        cleanup:
        workbook.close()
    }

    def "Gets the number of rows on all sheets by path"() {
        given:
        def url = Thread.currentThread().contextClassLoader.getResource("spreadsheets/products.xlsx")
        def path = Paths.get(url.toURI())

        when:
        def rowCount = getNumOfRows(path)

        then:
        rowCount == 122880
    }

}
