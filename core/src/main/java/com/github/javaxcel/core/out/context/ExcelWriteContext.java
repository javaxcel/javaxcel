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

package com.github.javaxcel.core.out.context;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;
import lombok.Getter;

import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelModel;
import com.github.javaxcel.core.out.core.ExcelWriter;
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;

/**
 * Context that has objects used on writing Excel file
 *
 * @param <T> type of model
 * @since 0.8.0
 */
@Getter
public class ExcelWriteContext<T> {

    @NotNull
    private final Workbook workbook;

    @NotNull
    private final Class<T> modelType;

    @NotNull
    private final Class<? extends ExcelWriter<T>> writerType;

    /**
     * Strategies for writing Excel file.
     * <p>
     * To prevent {@link NullPointerException} from being thrown,
     * initialize this field with empty map.
     */
    @NotNull
    private Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategyMap;

    private List<T> list;

    @Nullable
    private Sheet sheet;

    @Nullable
    private List<T> chunk;

    /**
     * @see ExcelColumn#headerStyle()
     * @see ExcelModel#headerStyle()
     */
    @Nullable
    private CellStyle[] headerStyles;

    /**
     * @see ExcelColumn#bodyStyle()
     * @see ExcelModel#bodyStyle()
     */
    @Nullable
    private CellStyle[] bodyStyles;

    public ExcelWriteContext(Workbook workbook, Class<T> modelType, Class<? extends ExcelWriter<T>> writerType) {
        Asserts.that(workbook)
                .describedAs("ExcelWriteContext.workbook is not allowed to be null")
                .isNotNull();
        Asserts.that(modelType)
                .describedAs("ExcelWriteContext.modelType is not allowed to be null")
                .isNotNull();
        Asserts.that(writerType)
                .describedAs("ExcelWriteContext.writerType is not allowed to be null")
                .isNotNull()
                .describedAs(
                        "ExcelWriteContext.writerType is type of implementation of ExcelWriter, but it isn't : '{0}'",
                        writerType.getName())
                .is(ExcelWriter.class::isAssignableFrom);

        this.workbook = workbook;
        this.modelType = modelType;
        this.writerType = writerType;
        this.strategyMap = Collections.emptyMap();
    }

    public void setStrategyMap(@NotNull Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategyMap) {
        Asserts.that(strategyMap)
                .describedAs("ExcelWriteContext.strategyMap is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelWriteContext.strategyMap.values is not allowed to contain null: {0}", strategyMap)
                .asValues().doesNotContainNull();

        this.strategyMap = strategyMap;
    }

    public void setList(@NotNull List<T> list) {
        Asserts.that(list)
                .describedAs("ExcelWriteContext.list is not allowed to be null")
                .isNotNull();

        this.list = list;
    }

    public void setChunk(@NotNull List<T> chunk) {
        Asserts.that(chunk)
                .describedAs("ExcelWriteContext.chunk is not allowed to be null")
                .isNotNull();

        this.chunk = chunk;
    }

    public void setSheet(@NotNull Sheet sheet) {
        Asserts.that(chunk)
                .describedAs("ExcelWriteContext.sheet is not allowed to be null")
                .isNotNull();

        this.sheet = sheet;
    }

    public void setHeaderStyles(@NotNull CellStyle[] headerStyles) {
        Asserts.that(headerStyles)
                .describedAs("ExcelWriteContext.headerStyles is not allowed to be null or empty: {0}", headerStyles)
                .isNotNull().isNotEmpty();

        this.headerStyles = headerStyles;
    }

    public void setBodyStyles(@NotNull CellStyle[] bodyStyles) {
        Asserts.that(bodyStyles)
                .describedAs("ExcelWriteContext.bodyStyles is not allowed to be null or empty: {0}", bodyStyles)
                .isNotNull().isNotEmpty();

        this.bodyStyles = bodyStyles;
    }

}
