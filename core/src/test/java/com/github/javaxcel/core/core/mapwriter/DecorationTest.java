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

package com.github.javaxcel.core.core.mapwriter;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;

import com.github.javaxcel.core.TestUtils;
import com.github.javaxcel.core.core.MapWriterTester;
import com.github.javaxcel.core.internal.style.DefaultHeaderStyleConfig;
import com.github.javaxcel.core.junit.annotation.StopwatchProvider;
import com.github.javaxcel.core.out.strategy.impl.AutoResizedColumns;
import com.github.javaxcel.core.out.strategy.impl.BodyStyles;
import com.github.javaxcel.core.out.strategy.impl.HeaderStyles;
import com.github.javaxcel.core.out.strategy.impl.HiddenExtraColumns;
import com.github.javaxcel.core.out.strategy.impl.HiddenExtraRows;
import com.github.javaxcel.core.out.strategy.impl.SheetName;
import com.github.javaxcel.core.util.ExcelUtils;
import com.github.javaxcel.styler.ExcelStyleConfig;

import static com.github.javaxcel.core.TestUtils.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @see AutoResizedColumns
 * @see HiddenExtraRows
 * @see HiddenExtraColumns
 * @see HeaderStyles
 * @see BodyStyles
 */
@StopwatchProvider
class DecorationTest extends MapWriterTester {

    private static final ExcelStyleConfig[] rainbowHeader = DefaultHeaderStyleConfig.getRainbowHeader();

    @Test
    @StopwatchProvider(TimeUnit.MILLISECONDS)
    void fail(Stopwatch stopwatch) throws Exception {
        // given
        stopwatch.start("create '%s' instance", SXSSFWorkbook.class.getSimpleName());
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        stopwatch.stop();

        // when & then
        stopwatch.start("set unmatched header style");
        assertThatThrownBy(() -> TestUtils.JAVAXCEL.writer(workbook)
                .options(new HeaderStyles(Arrays.asList(rainbowHeader)))
                .write(null, TestUtils.getRandomMaps(10, rainbowHeader.length - 1)))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("headerStyles.size must be 1 or equal to keys.size");
        stopwatch.stop();

        stopwatch.start("set unmatched body style");
        assertThatThrownBy(() -> TestUtils.JAVAXCEL.writer(workbook)
                .options(new BodyStyles(Arrays.asList(rainbowHeader)))
                .write(null, TestUtils.getRandomMaps(10, rainbowHeader.length + 1)))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("bodyStyles.size must be 1 or equal to keys.size");
    }

    @Test
    void succeed(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        String filename = getClass().getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_97_EXTENSION;
        File file = new File(path.toFile(), filename);

        run(file, stopwatch, 1024);
    }

    @Override
    protected ThenModel whenCreateModels(GivenModel givenModel, WhenModel whenModel) {
        List<Map<String, Object>> models = TestUtils.getRandomMaps(whenModel.getMockCount(), rainbowHeader.length);
        return new ThenModel(models);
    }

    @Override
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        // java.lang.RuntimeException: Column info records are out of order
        TestUtils.JAVAXCEL.writer(whenModel.getWorkbook())
                .options(new SheetName("Rainbow"),
                        new AutoResizedColumns(true),
                        new HiddenExtraRows(),
                        new HiddenExtraColumns(),
                        new HeaderStyles(new DefaultHeaderStyleConfig()),
                        new HeaderStyles(Arrays.asList(rainbowHeader)),
                        new BodyStyles(new DefaultHeaderStyleConfig()),
                        new BodyStyles(Arrays.asList(rainbowHeader)))
                .write(whenModel.getOutputStream(), thenModel.getModels());
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();

        assertNotEmptyFile(file, "#1 Excel file must be created and have content");

        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);
        int maxModels = ExcelUtils.getMaxRows(workbook) - 1;
        assertThat(ExcelUtils.getNumOfModels(file))
                .as("#2 The number of actually written maps is %,d", maxModels)
                .isEqualTo(maxModels);
    }

}
