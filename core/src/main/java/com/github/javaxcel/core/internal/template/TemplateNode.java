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

package com.github.javaxcel.core.internal.template;

import java.util.List;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Recursive AST node produced by {@link TemplateScanner} for a single sheet.
 *
 * <p>Four shapes:
 * <ul>
 *   <li>{@link StaticCell} — a cell whose value is copied verbatim.</li>
 *   <li>{@link SubstitutionCell} — a cell whose value contains one or more
 *       {@code ${...}} expressions.</li>
 *   <li>{@link IterationBlock} — a rectangular region replicated for each
 *       element of {@code collectionExpr}; child nodes are evaluated with a
 *       loop variable in scope.</li>
 *   <li>{@link ConditionalBlock} — a rectangular region rendered only when
 *       {@code conditionExpr} evaluates truthy.</li>
 * </ul>
 *
 * @since 0.x
 */
public sealed interface TemplateNode
        permits TemplateNode.StaticCell, TemplateNode.SubstitutionCell,
                TemplateNode.IterationBlock, TemplateNode.ConditionalBlock {

    record StaticCell(CellAddress coord) implements TemplateNode {
    }

    record SubstitutionCell(CellAddress coord, String text) implements TemplateNode {
    }

    record IterationBlock(CellRangeAddress range, String collectionExpr, String varName,
                          List<TemplateNode> children) implements TemplateNode {
    }

    record ConditionalBlock(CellRangeAddress range, String conditionExpr,
                            List<TemplateNode> children) implements TemplateNode {
    }

}
