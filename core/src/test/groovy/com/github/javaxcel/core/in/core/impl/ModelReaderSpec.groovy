/*
 * Copyright 2024 Javaxcel
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

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern

import org.apache.poi.xssf.streaming.SXSSFWorkbook

import com.github.pjfanning.xlsx.StreamingReader

import com.github.javaxcel.core.Javaxcel
import com.github.javaxcel.core.annotation.ExcelColumn
import com.github.javaxcel.core.annotation.ExcelValidation
import com.github.javaxcel.core.exception.ExcelColumnValidationException
import com.github.javaxcel.test.validator.NotNullColumnValidator
import com.github.javaxcel.test.validator.NumericUuidColumnValidator

@Subject(ModelReader)
class ModelReaderSpec extends Specification {

    @TempDir
    private Path path

    def "validates column value with a validator"() {
        given:
        def models = [
                new Model0(name: "John"),
                new Model0(name: "BlackWood"),
                new Model0(name: null),
                new Model0(name: "Johanson"),
        ]

        and:
        def filePath = path.resolve("model0.xlsx")
        Javaxcel.newInstance()
                .writer(new SXSSFWorkbook(), Model0)
                .write(Files.newOutputStream(filePath), models)

        when:
        Javaxcel.newInstance()
                .reader(StreamingReader.builder().open(Files.newInputStream(filePath)), Model0)
                .read()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid column value: null"
    }

    def "validates column value with a regexp"() {
        given:
        def models = [
                new Model1(name: "Alexander"),
                new Model1(name: "McDonald"),
                new Model1(name: "Russell"),
                new Model1(name: null),
        ]

        and:
        def filePath = path.resolve("model1.xlsx")
        Javaxcel.newInstance()
                .writer(new SXSSFWorkbook(), Model1)
                .write(Files.newOutputStream(filePath), models)

        when:
        Javaxcel.newInstance()
                .reader(StreamingReader.builder().open(Files.newInputStream(filePath)), Model1)
                .read()

        then:
        def e = thrown(ExcelColumnValidationException)
        e.message == 'Invalid cell value for regular expression[^([A-Z][a-z]*)+$]: null'
    }

    def "validates column value with validators"() {
        given:
        def models = [
                new Model2(uuid: UUID.fromString("46580218-3485-5342-5113-189583507857")),
                new Model2(uuid: UUID.fromString("89234701-9483-4613-1231-009128734934")),
                new Model2(uuid: UUID.fromString("23470987-8943-1283-7689-713485713479")),
                new Model2(uuid: UUID.fromString("ff499ec8-e976-4c48-a831-2763e719ea65")),
        ]

        and:
        def filePath = path.resolve("model2.xlsx")
        Javaxcel.newInstance()
                .writer(new SXSSFWorkbook(), Model2)
                .write(Files.newOutputStream(filePath), models)

        when:
        Javaxcel.newInstance()
                .reader(StreamingReader.builder().open(Files.newInputStream(filePath)), Model2)
                .read()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid column value $UUID: ff499ec8-e976-4c48-a831-2763e719ea65"
    }

    def "validates column value with validators in wrong order"() {
        given:
        def models = [
                new Model3(uuid: UUID.fromString("46580218-3485-5342-5113-189583507857")),
                new Model3(uuid: UUID.fromString("89234701-9483-4613-1231-009128734934")),
                new Model3(uuid: UUID.fromString("23470987-8943-1283-7689-713485713479")),
                new Model3(uuid: null),
        ]

        and:
        def filePath = path.resolve("model3.xlsx")
        Javaxcel.newInstance()
                .writer(new SXSSFWorkbook(), Model3)
                .write(Files.newOutputStream(filePath), models)

        when:
        Javaxcel.newInstance()
                .reader(StreamingReader.builder().open(Files.newInputStream(filePath)), Model3)
                .read()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid column value $UUID: null"
    }

    // -------------------------------------------------------------------------------------------------

    private static class Model0 {
        @ExcelColumn(
                validation = @ExcelValidation(
                        validators = NotNullColumnValidator
                )
        )
        private String name
    }

    private static class Model1 {
        @ExcelColumn(
                validation = @ExcelValidation(
                        regexp = /^([A-Z][a-z]*)+$/,
                        flags = Pattern.UNICODE_CASE
                )
        )
        private String name
    }

    private static class Model2 {
        @ExcelColumn(
                validation = @ExcelValidation(
                        regexp = /^[a-z0-9-]{36}$/,
                        flags = Pattern.CASE_INSENSITIVE,
                        validators = [NotNullColumnValidator, NumericUuidColumnValidator]
                )
        )
        private UUID uuid
    }

    private static class Model3 {
        @ExcelColumn(
                validation = @ExcelValidation(
                        validators = [NumericUuidColumnValidator, NotNullColumnValidator]
                )
        )
        private UUID uuid
    }

}
