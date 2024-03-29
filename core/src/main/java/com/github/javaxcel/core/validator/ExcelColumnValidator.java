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

package com.github.javaxcel.core.validator;

import org.jetbrains.annotations.Nullable;

import com.github.javaxcel.core.annotation.ExcelValidation;

/**
 * Validator for excel column.
 *
 * @since 0.10.0
 * @see ExcelValidation#validators()
 */
public interface ExcelColumnValidator {

    /**
     * Checks if column value is valid or not.
     *
     * @param cellValue column value from a cell
     */
    void validate(@Nullable String cellValue);

}
