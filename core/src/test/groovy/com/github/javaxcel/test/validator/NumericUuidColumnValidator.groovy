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

package com.github.javaxcel.test.validator

import java.util.regex.Pattern

import com.github.javaxcel.core.validator.ExcelColumnValidator

class NumericUuidColumnValidator implements ExcelColumnValidator {

    private static final Pattern PATTERN = ~/^[0-9]{8}(-[0-9]{4}){3}-[0-9]{12}$/

    @Override
    void validate(String cellValue) {
        def value = String.valueOf(cellValue)

        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid column value $UUID: $cellValue")
        }
    }

}
