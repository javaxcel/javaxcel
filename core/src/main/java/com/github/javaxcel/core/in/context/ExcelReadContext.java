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

package com.github.javaxcel.core.in.context;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;
import lombok.Getter;

import com.github.javaxcel.core.in.core.ExcelReader;
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy;

/**
 * Context that has objects used on reading Excel file
 *
 * @param <T> type of model
 * @since 0.8.0
 */
@Getter
public class ExcelReadContext<T> {

    @NotNull
    private final Workbook workbook;

    @NotNull
    private final Class<T> modelType;

    @NotNull
    private final Class<? extends ExcelReader<T>> readerType;

    /**
     * Strategies for reading Excel file.
     * <p>
     * To prevent {@link NullPointerException} from being thrown,
     * initialize this field with empty map.
     */
    @NotNull
    private Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> strategyMap;

    private List<T> list;

    /**
     * Header names for reading Excel file.
     * <p>
     * To prevent {@link NullPointerException} from being thrown,
     * initialize this field with empty list.
     */
    @NotNull
    private List<String> headerNames;

    /**
     * THe number of models read.
     */
    private int readCount;

    @Nullable
    private Sheet sheet;

    @Nullable
    private List<T> chunk;

    public ExcelReadContext(Workbook workbook, Class<T> modelType, Class<? extends ExcelReader<T>> readerType) {
        Asserts.that(workbook)
                .describedAs("ExcelReadContext.workbook is not allowed to be null")
                .isNotNull();
        Asserts.that(modelType)
                .describedAs("ExcelReadContext.modelType is not allowed to be null")
                .isNotNull();
        Asserts.that(readerType)
                .describedAs("ExcelReadContext.readerType is not allowed to be null")
                .isNotNull()
                .describedAs(
                        "ExcelReadContext.readerType is type of implementation of ExcelReader, but it isn't : '{0}'",
                        readerType.getName())
                .is(ExcelReader.class::isAssignableFrom);

        this.workbook = workbook;
        this.modelType = modelType;
        this.readerType = readerType;
        this.strategyMap = Collections.emptyMap();
        this.headerNames = Collections.emptyList();
    }

    public void setStrategyMap(@NotNull Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> strategyMap) {
        Asserts.that(strategyMap)
                .describedAs("ExcelReadContext.strategyMap is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadContext.strategyMap.values is not allowed to contain null: {0}", strategyMap)
                .asValues().doesNotContainNull();

        this.strategyMap = strategyMap;
    }

    public void setList(@NotNull List<T> list) {
        Asserts.that(list)
                .describedAs("ExcelReadContext.list is not allowed to be null")
                .isNotNull();

        this.list = list;
    }

    public void setHeaderNames(@NotNull List<String> headerNames) {
        Asserts.that(headerNames)
                .describedAs("ExcelReadContext.headerNames is not allowed to be null or empty: {0}", headerNames)
                .isNotNull().isNotEmpty()
                .describedAs("ExcelReadContext.headerNames cannot have null or blank element: {0}", headerNames)
                .noneMatch(StringUtils::isNullOrBlank)
                .describedAs("ExcelReadContext.headerNames cannot have duplicated elements: {0}", headerNames)
                .doesNotHaveDuplicates();

        this.headerNames = headerNames;
    }

    public void increaseReadCount() {
        this.readCount++;
    }

    public void setChunk(@NotNull List<T> chunk) {
        Asserts.that(chunk)
                .describedAs("ExcelReadContext.chunk is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadContext.chunk is not allowed to contain null: {0}", chunk)
                .doesNotContainNull();

        this.chunk = chunk;
    }

    public void setSheet(@NotNull Sheet sheet) {
        Asserts.that(sheet)
                .describedAs("ExcelReadContext.sheet is not allowed to be null")
                .isNotNull();

        this.sheet = sheet;
    }

}
