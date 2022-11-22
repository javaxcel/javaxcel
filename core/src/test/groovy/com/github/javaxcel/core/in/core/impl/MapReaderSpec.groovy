/*
 * Copyright 2021 Javaxcel
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

package com.github.javaxcel.core.in.core.impl

import com.github.javaxcel.core.Javaxcel
import com.github.javaxcel.core.out.strategy.impl.HeaderNames
import com.github.javaxcel.core.out.strategy.impl.SheetName
import com.github.javaxcel.core.util.ExcelUtils
import com.github.javaxcel.test.util.TestUtils
import io.github.imsejin.common.tool.RandomString
import io.github.imsejin.common.tool.Stopwatch
import lombok.Cleanup
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

import static java.util.stream.Collectors.toList

class MapReaderSpec extends Specification {

    @TempDir
    Path path

    def "test"() {
        given:
        def keys = ["A", "B", "C", "D", "E", "F"]
        def workbook = new SXSSFWorkbook()
        def numOfMocks = 1024
        def maps = (0..<numOfMocks).collect { TestUtils.randomizeMap(keys) }
        def filePath = path.resolve("maps-${System.currentTimeMillis()}.xlsx")
        def out = new FileOutputStream(filePath.toFile())

        when:
        Javaxcel.newInstance()
                .writer(workbook)
                .write(out, maps)

        and:
        workbook = ExcelUtils.getWorkbook(filePath.toFile())

        then:
        Files.exists(filePath)
        ExcelUtils.getNumOfModels(workbook) == numOfMocks
        workbook[0][0].collect { it.stringCellValue } == keys

        cleanup:
        out.close()
    }

    def "test1"() {
        given:
        def stopwatch = new Stopwatch(TimeUnit.SECONDS)
        def file = new File(path.toFile(), "maps.xlsx")
        def keys = ["race", "name", "height", "weight", "eyesight", "favoriteFood"]

        // Create excel file
        stopwatch.start("create '%s' file", file.name)
        @Cleanup def out = new FileOutputStream(file)
        @Cleanup def workbook = new HSSFWorkbook()
        stopwatch.stop()

        // Create mocks
        def numOfMocks = ExcelUtils.getMaxRows(workbook) + 10_000
        stopwatch.start("create %,d mocks", numOfMocks)
        def maps = (0..numOfMocks).collect { getRandomMap keys }
        stopwatch.stop()

        // Write excel file with mocks
        stopwatch.start("write %,d maps", numOfMocks)
        Javaxcel.newInstance().writer(workbook)
                .options(new SheetName("Maps"), new HeaderNames(keys))
                .write(out, maps)
        stopwatch.stop()

        when: "Read excel file"
        stopwatch.start("read %,d maps", numOfMocks)
        @Cleanup def newWorkbook = new HSSFWorkbook(new FileInputStream(file))
        def actual = Javaxcel.newInstance().reader(newWorkbook).read()
        stopwatch.stop()
        println stopwatch.statistics

        then: "Number of mocks is equal to number of models loaded by ExcelReader"
        maps.size() == actual.size()

        then: "Keys of all the mocks are equal to the given keys"
        def actualKeys = actual.stream().flatMap({ it.keySet().stream() }).distinct().sorted().collect(toList())
        actualKeys == keys.sort()

        then: "Mocks is equal to the models loaded by ExcelReader"
        maps == actual
    }

    private static Map<String, Object> getRandomMap(List<String> keys) {
        def map = [:] as Map<String, Object>
        def randomString = new RandomString()

        keys.each {
            map.put(it, randomString.nextString(it.length()))
        }

        map
    }

}
