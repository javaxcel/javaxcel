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

package com.github.javaxcel.core.core.modelreader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.imsejin.common.tool.RandomString;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import com.github.javaxcel.core.TestUtils;
import com.github.javaxcel.core.annotation.ExcelModelCreator;
import com.github.javaxcel.core.core.ModelReaderTester;
import com.github.javaxcel.core.junit.annotation.StopwatchProvider;
import com.github.javaxcel.core.util.ExcelUtils;

import static org.assertj.core.api.Assertions.*;

@StopwatchProvider
class FinalFieldTest extends ModelReaderTester {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<FinalFieldModel> type = FinalFieldModel.class;
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_97_EXTENSION;
        File file = path.resolve(filename).toFile();

        run(file, type, stopwatch);
    }

    @Override
    protected WhenModel givenCreateMocks(GivenModel givenModel) {
        RandomString randomString = new RandomString(TestUtils.getRandom());
        List<FinalFieldModel> mocks = new ArrayList<>();

        for (int i = 0; i < givenModel.getMockCount(); i++) {
            int number = TestUtils.getRandom().nextInt();
            String text = randomString.nextString(8);
            mocks.add(new FinalFieldModel(number, text));
        }

        return new WhenModel(mocks);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        List<FinalFieldModel> products = (List<FinalFieldModel>) thenModel.getModels();
        List<FinalFieldModel> mocks = whenModel.getMocks();

        assertThat(products)
                .as("#1 The number of loaded models is %,d", mocks.size())
                .hasSameSizeAs(mocks)
                .as("#2 Each loaded model is not equal to each mock")
                .isNotEqualTo(mocks)
                .as("#3 All the loaded models aren't changed on final field")
                .containsOnly(new FinalFieldModel());
    }

    // -------------------------------------------------------------------------------------------------

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class FinalFieldModel {
        private static final int DEFAULT_NUMBER = 100;

        private static final String DEFAULT_TEXT = "TEXT";

        private final int number;

        private final String text;

        @ExcelModelCreator
        public FinalFieldModel() {
            this.number = DEFAULT_NUMBER;
            this.text = DEFAULT_TEXT;
        }
    }

}
