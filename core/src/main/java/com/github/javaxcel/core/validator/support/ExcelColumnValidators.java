/*
 * Copyright 2024 Javaxcel
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

package com.github.javaxcel.core.validator.support;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaxcel.core.analysis.ExcelAnalysis;
import com.github.javaxcel.core.validator.ExcelColumnValidator;

/**
 * Container of validators for each cell value.
 *
 * @since 0.10.0
 */
public final class ExcelColumnValidators {

    private final Map<Field, List<ExcelColumnValidator>> validatorsMap;

    public ExcelColumnValidators(Iterable<ExcelAnalysis> analyses) {
        Map<Field, List<ExcelColumnValidator>> validatorsMap = new HashMap<>();

        for (ExcelAnalysis analysis : analyses) {
            List<ExcelColumnValidator> validators = analysis.getValidators();
            validatorsMap.put(analysis.getField(), validators);
        }

        this.validatorsMap = Collections.unmodifiableMap(validatorsMap);
    }

    public void validate(String cellValue, Field field) {
        List<ExcelColumnValidator> validators = this.validatorsMap.get(field);

        for (ExcelColumnValidator validator : validators) {
            validator.validate(cellValue);
        }
    }

}
