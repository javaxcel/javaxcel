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

package com.github.javaxcel.core.core.modelwriter;

import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.imsejin.common.tool.Stopwatch;

import com.github.javaxcel.core.TestUtils;
import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelModel;
import com.github.javaxcel.core.exception.NoTargetedFieldException;
import com.github.javaxcel.core.junit.annotation.StopwatchProvider;
import com.github.javaxcel.core.out.core.impl.ModelWriter;

import static org.assertj.core.api.Assertions.*;

@StopwatchProvider(TimeUnit.MILLISECONDS)
class NoTargetedFieldTest {

    @ParameterizedTest
    @ValueSource(classes = {NoFieldModel.class, AllIgnoredModel.class, ExplicitModel.class})
    @DisplayName("When creates ModelWriter with model type without targeted field")
    void test(Class<?> type, Stopwatch stopwatch) {
        // given
        stopwatch.start("create '%s' instance", HSSFWorkbook.class.getSimpleName());
        Workbook workbook = new HSSFWorkbook();
        stopwatch.stop();

        // when & then
        stopwatch.start("create '%s' instance with '%s'", ModelWriter.class.getSimpleName(), type.getSimpleName());
        assertThatThrownBy(() -> TestUtils.JAVAXCEL.writer(workbook, type))
                .as("When creates ModelWriter with model type without targeted field")
                .isExactlyInstanceOf(NoTargetedFieldException.class);
    }

    // -------------------------------------------------------------------------------------------------

    private static class NoFieldModel {
    }

    private static class AllIgnoredModel {
        @ExcelColumn(ignored = true)
        private int number;
        @ExcelColumn(ignored = true)
        private Character character;
    }

    @ExcelModel(onlyExplicitlyAnnotated = true)
    private static class ExplicitModel {
        private Long id;
        private String name;
    }

}
