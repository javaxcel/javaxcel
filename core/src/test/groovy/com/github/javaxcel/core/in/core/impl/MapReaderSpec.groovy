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
import com.github.javaxcel.core.util.ExcelUtils
import io.github.imsejin.common.tool.RandomString
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

@Subject(MapReader)
class MapReaderSpec extends Specification {

    @TempDir
    private Path path

    def "test"() {
        given:
        def keys = ["race", "name", "height", "weight", "eyesight", "favoriteFood"]
        def filePath = path.resolve("maps-${System.currentTimeMillis()}.xlsx")

        and: "Creates excel file"
        def outStream = Files.newOutputStream(filePath)
        def workbook = new HSSFWorkbook()

        and: "Creates mocks"
        def mockCount = ExcelUtils.getMaxRows(workbook) + 10_000
        List<Map<String, Object>> maps = (0..mockCount).collect { getRandomMap(keys) }

        and: "Writes excel file with mocks"
        Javaxcel.newInstance().writer(workbook)
                .options(new HeaderNames(keys))
                .write(outStream, maps)

        when: "Reads excel file"
        def inputStream = Files.newInputStream(filePath)
        def newWorkbook = new HSSFWorkbook(inputStream)
        List<Map<String, String>> actual = Javaxcel.newInstance().reader(newWorkbook).read()

        then: "Number of mocks is equal to number of models loaded by ExcelReader"
        maps.size() == actual.size()

        and: "Keys of all the mocks are equal to the given keys"
        List<String> actualKeys = actual.collectMany { it.keySet() }.unique().sort(false)
        actualKeys == keys.sort(false)

        and: "Mocks are equal to the models loaded by ExcelReader"
        maps == actual

        cleanup:
        outStream.close()
        inputStream.close()
    }

    // -------------------------------------------------------------------------------------------------

    private static Map<String, Object> getRandomMap(List<String> keys) {
        Map<String, Object> map = [:]
        def randomString = new RandomString()
        keys.each { map.put(it, randomString.nextString(it.length())) }

        map
    }

}
