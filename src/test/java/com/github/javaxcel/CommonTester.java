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

package com.github.javaxcel;

import io.github.imsejin.common.tool.Stopwatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class CommonTester {

    protected Stopwatch stopWatch;

    @BeforeEach
    protected void beforeEach() {
        this.stopWatch = new Stopwatch(TimeUnit.SECONDS);
    }

    @AfterEach
    protected void afterEach() {
        System.out.println(this.stopWatch.getStatistics());
    }

    protected <T> List<T> getRandomModels(Class<T> type, int size) {
        List<T> list = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            list.add(TestUtils.randomize(type));
        }

        return list;
    }

}
