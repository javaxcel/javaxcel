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

import com.github.javaxcel.core.in.core.ExcelReader;
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy;

/**
 * Context that has objects used on reading Excel file
 *
 * @param <T> type of model
 * @since 0.8.0
 */
public class ExcelReadContext<T> {

    private final Workbook workbook;
    private final Class<T> modelType;
    private final Class<? extends ExcelReader<T>> readerType;

    /**
     * Strategies for reading Excel file.
     * <p>
     * To prevent {@link NullPointerException} from being thrown,
     * initialize this field with empty map.
     */
    private Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> strategyMap = Collections.emptyMap();
    private List<T> list;

    /**
     * Header names for reading Excel file.
     * <p>
     * To prevent {@link NullPointerException} from being thrown,
     * initialize this field with empty list.
     */
    private List<String> headerNames = Collections.emptyList();

    /**
     * THe number of models read.
     */
    private int readCount;

    private Sheet sheet;
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
    }

    @NotNull
    public Workbook getWorkbook() {
        return this.workbook;
    }

    @NotNull
    public Class<T> getModelType() {
        return this.modelType;
    }

    @NotNull
    public Class<? extends ExcelReader<?>> getReaderType() {
        return this.readerType;
    }

    @NotNull
    public Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> getStrategyMap() {
        return this.strategyMap;
    }

    public void setStrategyMap(Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> strategyMap) {
        Asserts.that(strategyMap)
                .describedAs("ExcelReadContext.strategyMap is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadContext.strategyMap.values is not allowed to contain null: {0}", strategyMap)
                .asValues().doesNotContainNull();

        this.strategyMap = strategyMap;
    }

    @Nullable
    public List<T> getList() {
        return this.list;
    }

    public void setList(List<T> list) {
        Asserts.that(list)
                .describedAs("ExcelReadContext.list is not allowed to be null")
                .isNotNull();

        this.list = list;
    }

    @NotNull
    public List<String> getHeaderNames() {
        return this.headerNames;
    }

    public void setHeaderNames(List<String> headerNames) {
        Asserts.that(headerNames)
                .describedAs("ExcelReadContext.headerNames is not allowed to be null or empty: {0}", headerNames)
                .isNotNull().isNotEmpty()
                .describedAs("ExcelReadContext.headerNames cannot have null or blank element: {0}", headerNames)
                .noneMatch(StringUtils::isNullOrBlank)
                .describedAs("ExcelReadContext.headerNames cannot have duplicated elements: {0}", headerNames)
                .doesNotHaveDuplicates();

        this.headerNames = headerNames;
    }

    public int getReadCount() {
        return this.readCount;
    }

    public void increaseReadCount() {
        this.readCount++;
    }

    @Nullable
    public List<T> getChunk() {
        return this.chunk;
    }

    public void setChunk(List<T> chunk) {
        Asserts.that(chunk)
                .describedAs("ExcelReadContext.chunk is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadContext.chunk is not allowed to contain null: {0}", chunk)
                .doesNotContainNull();

        this.chunk = chunk;
    }

    @NotNull
    public Sheet getSheet() {
        return this.sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

}
