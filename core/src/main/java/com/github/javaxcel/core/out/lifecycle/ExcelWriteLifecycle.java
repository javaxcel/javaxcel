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

package com.github.javaxcel.core.out.lifecycle;

import com.github.javaxcel.core.out.context.ExcelWriteContext;
import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

/**
 * Lifecycle of Excel writer
 *
 * @param <T> type of model
 * @since 0.8.0
 */
@ExcludeFromGeneratedJacocoReport
public interface ExcelWriteLifecycle<T> {

    default void prepare(ExcelWriteContext<T> context) {
    }

    default void preWriteSheet(ExcelWriteContext<T> context) {
    }

    default void postWriteSheet(ExcelWriteContext<T> context) {
    }

    default void complete(ExcelWriteContext<T> context) {
    }

}
