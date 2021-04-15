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

package com.github.javaxcel.out.modelwriter;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class IgnoreTest {

    @ParameterizedTest
    @ValueSource(classes = {IgnoredModel.class, ExplicitModel.class})
    @DisplayName("@ExcelIgnore")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void writeModelsWithIgnoredFields(Class<?> type, @TempDir Path path, Stopwatch stopwatch) throws IOException {
        String filename = type.getSimpleName().toLowerCase() + ".xlsx";

        // given
        stopwatch.start("create '%s' file", filename);
        File file = new File(path.toFile(), filename);
        @Cleanup OutputStream out = new FileOutputStream(file);
        Workbook workbook = new SXSSFWorkbook();
        stopwatch.stop();

        final int numOfMocks = ExcelUtils.getMaxRows(workbook) / 50;
        stopwatch.start("create %,d mocks", numOfMocks);
        List models = TestUtils.getMocks(type, numOfMocks);
        stopwatch.stop();

        // when
        stopwatch.start("write %,d models", numOfMocks);
        ExcelWriterFactory.create(workbook, type).write(out, models);
        stopwatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file must be created and have content")
                .isNotNull().exists().canRead().isNotEmpty();

        @Cleanup Workbook wb = WorkbookFactory.create(file);
        assertThat(ExcelUtils.getNumOfModels(wb))
                .as("#2 The number of actually written model is %,d", models.size())
                .isEqualTo(models.size());

        final int numOfTargetedFields = FieldUtils.getTargetedFields(type).size();
        assertThat(ExcelUtils.getSheets(wb).stream()
                           .mapToInt(sheet -> sheet.getRow(0).getPhysicalNumberOfCells())
                           .average().orElse(-1))
                .as("#3 The header size of excel file is %,d", numOfTargetedFields)
                .isEqualTo(numOfTargetedFields);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @Getter
    @Setter
    static class IgnoredModel {
        private long id;

        @ExcelIgnore
        private Double width;

        @ExcelIgnore
        private double height;

        private Double weight;
    }

    @Getter
    @Setter
    @ExcelModel(explicit = true)
    static class ExplicitModel {
        @ExcelColumn
        private Long id;

        @ExcelColumn
        private String name;

        private int count;
    }

}
