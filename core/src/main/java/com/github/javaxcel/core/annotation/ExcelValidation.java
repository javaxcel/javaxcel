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

package com.github.javaxcel.core.annotation;

import java.util.regex.Pattern;

import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.MagicConstant;

import com.github.javaxcel.core.in.core.ExcelReader;
import com.github.javaxcel.core.validator.ExcelColumnValidator;

/**
 * Excel validation for column.
 *
 * <p> This is applied only {@link ExcelReader}.
 *
 * @since 0.10.0
 */
public @interface ExcelValidation {

    /**
     * Validators for cell value when reading each cell.
     *
     * <p> This is applied only {@link ExcelReader}.
     *
     * @return column validators
     */
    Class<? extends ExcelColumnValidator>[] validators() default {};

    /**
     * Regular expression for cell value when reading each cell.
     *
     * @return regular expression
     */
    @Language("RegExp")
    String regexp() default "";

    /**
     * Flags of regular expression for cell value when reading each cell.
     *
     * @return flags of regular expression
     */
    @MagicConstant(valuesFromClass = Pattern.class)
    int flags() default 0;

}
