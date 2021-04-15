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

import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.ModelWriter;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@StopwatchProvider(TimeUnit.MILLISECONDS)
class NoTargetedFieldTest {

    @ParameterizedTest
    @ValueSource(classes = {NoFieldModel.class, AllIgnoredModel.class, ExplicitModel.class})
    @DisplayName("throws NoTargetedFieldException")
    void createModelWriterWithoutTargetField(Class<?> type, Stopwatch stopwatch) {
        // given
        stopwatch.start("create '%s' instance", HSSFWorkbook.class.getSimpleName());
        Workbook workbook = new HSSFWorkbook();
        stopwatch.stop();

        // when & then
        stopwatch.start("create '%s' instance with '%s'", ModelWriter.class.getSimpleName(), type.getSimpleName());
        assertThatThrownBy(() -> ExcelWriterFactory.create(workbook, type))
                .as("When creates ModelWriter with model without targeted field")
                .isExactlyInstanceOf(NoTargetedFieldException.class);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    static class NoFieldModel {
    }

    @Getter
    @Setter
    static class AllIgnoredModel {
        @ExcelIgnore
        private int number;
        @ExcelIgnore
        private Character character;
    }

    @Getter
    @Setter
    @ExcelModel(explicit = true)
    static class ExplicitModel {
        private Long id;
        private String name;
    }

}
