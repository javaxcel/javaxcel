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

package com.github.javaxcel.core.out.template.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import io.github.imsejin.common.assertion.Asserts;

import com.github.javaxcel.core.exception.WritingExcelException;
import com.github.javaxcel.core.internal.template.TemplateEvaluator;
import com.github.javaxcel.core.out.template.ExcelTemplateWriter;

/**
 * Single template engine implementation. Holds the template {@link Workbook}
 * and accumulated variables, defers all evaluation to
 * {@link TemplateEvaluator}.
 *
 * @since 0.x
 */
public final class DefaultExcelTemplateWriter implements ExcelTemplateWriter {

    private final Workbook templateWorkbook;

    private final Map<String, Object> variables = new LinkedHashMap<>();

    private DefaultExcelTemplateWriter(Workbook templateWorkbook) {
        this.templateWorkbook = templateWorkbook;
    }

    /**
     * Constructs a writer bound to the given template workbook. The workbook
     * is mutated in place by {@link #write(OutputStream)} — callers should
     * treat it as consumed after a write.
     */
    public static DefaultExcelTemplateWriter create(Workbook templateWorkbook) {
        Asserts.that(templateWorkbook)
                .describedAs("DefaultExcelTemplateWriter.templateWorkbook is not allowed to be null")
                .isNotNull();
        return new DefaultExcelTemplateWriter(templateWorkbook);
    }

    @Override
    public ExcelTemplateWriter with(String name, Object value) {
        Asserts.that(name)
                .describedAs("ExcelTemplateWriter.with: name is not allowed to be null or empty")
                .isNotNull()
                .isNotEmpty();
        this.variables.put(name, value);
        return this;
    }

    @Override
    public void write(OutputStream out, Object context) {
        Asserts.that(out)
                .describedAs("ExcelTemplateWriter.out is not allowed to be null")
                .isNotNull();
        Map<String, Object> ctx = TemplateEvaluator.toContextMap(context);
        // Builder variables override / merge with the explicit context map.
        Map<String, Object> merged = new LinkedHashMap<>(ctx);
        merged.putAll(this.variables);
        evaluateAndWrite(merged, out);
    }

    @Override
    public void write(OutputStream out) {
        Asserts.that(out)
                .describedAs("ExcelTemplateWriter.out is not allowed to be null")
                .isNotNull();
        evaluateAndWrite(new LinkedHashMap<>(this.variables), out);
    }

    private void evaluateAndWrite(Map<String, Object> context, OutputStream out) {
        new TemplateEvaluator().evaluate(this.templateWorkbook, context);
        try {
            this.templateWorkbook.write(out);
        } catch (IOException e) {
            throw new WritingExcelException(e);
        }
    }

}
