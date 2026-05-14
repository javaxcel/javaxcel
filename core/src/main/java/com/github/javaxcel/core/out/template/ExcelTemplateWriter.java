/*
 * Copyright 2026 Javaxcel
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

package com.github.javaxcel.core.out.template;

import java.io.OutputStream;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * Template-based Excel writer. Evaluates {@code ${...}} SpEL expressions in
 * cell values and {@code jxc:} directives in cell comments to render the
 * template against a context.
 *
 * <p>Use {@code Javaxcel.newInstance().templateWriter(workbook)} to obtain an
 * instance. The template {@link Workbook} is consumed in-place — its sheets
 * are replaced with the evaluated content.
 *
 * @since 0.x
 */
public interface ExcelTemplateWriter {

    /**
     * Registers a single named variable. Values added with {@code with} are
     * accumulated and used by {@link #write(OutputStream)}.
     *
     * @return this writer (fluent)
     */
    ExcelTemplateWriter with(String name, Object value);

    /**
     * Renders the template using {@code context} as the SpEL root.
     * The argument may be a {@link Map} (used directly) or a POJO (its
     * instance fields become top-level variables via reflection).
     */
    void write(OutputStream out, Object context);

    /**
     * Renders the template using only the variables accumulated through
     * {@link #with(String, Object)}.
     */
    void write(OutputStream out);

}
