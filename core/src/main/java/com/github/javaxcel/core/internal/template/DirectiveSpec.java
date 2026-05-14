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

import org.apache.poi.ss.util.CellAddress;
import org.jspecify.annotations.Nullable;

/**
 * Parsed form of a {@code jxc:} directive embedded in an Excel cell comment.
 *
 * <p>Directives drive template iteration and conditional rendering:
 * <pre>{@code
 * jxc: each <SpEL collection> as <varName> [, until: <cellRef>]
 * jxc: if   <SpEL boolean expression>
 * }</pre>
 *
 * <p>Two concrete shapes:
 * <ul>
 *   <li>{@link Each} — iterate the collection, exposing each element as a variable
 *       within the optional {@code until}-bounded block.</li>
 *   <li>{@link If} — render the block only when the SpEL boolean evaluates truthy.</li>
 * </ul>
 *
 * @since 0.x
 */
public sealed interface DirectiveSpec permits DirectiveSpec.Each, DirectiveSpec.If {

    /** Iteration directive: replicate the block for each element of {@code collectionExpr}. */
    record Each(String collectionExpr, String varName, @Nullable CellAddress untilAddress) implements DirectiveSpec {
    }

    /** Conditional directive: render the block iff {@code conditionExpr} evaluates to a truthy value. */
    record If(String conditionExpr) implements DirectiveSpec {
    }

}
