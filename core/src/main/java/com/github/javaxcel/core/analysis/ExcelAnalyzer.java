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

package com.github.javaxcel.core.analysis;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Analyzer for preparing the fields to handle for Excel
 *
 * @since 0.9.0
 */
public interface ExcelAnalyzer {

    /**
     * Analyzes the fields to handle for Excel.
     *
     * @param fields    targeted fields
     * @param arguments optional arguments
     * @return analyses of the fields
     */
    List<ExcelAnalysis> analyze(List<Field> fields, Object... arguments);

}
