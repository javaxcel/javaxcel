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

import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Geometry helpers for {@link CellRangeAddress} used by {@link TemplateScanner}.
 */
final class DirectiveScannerSupport {

    private DirectiveScannerSupport() {
    }

    /** Number of cells covered by the range (inclusive). */
    static int area(CellRangeAddress range) {
        return (range.getLastRow() - range.getFirstRow() + 1)
                * (range.getLastColumn() - range.getFirstColumn() + 1);
    }

    /**
     * {@code true} iff {@code outer} contains {@code inner} entirely and
     * the two ranges are not identical.
     */
    static boolean strictlyContains(CellRangeAddress outer, CellRangeAddress inner) {
        if (outer.getFirstRow() == inner.getFirstRow()
                && outer.getLastRow() == inner.getLastRow()
                && outer.getFirstColumn() == inner.getFirstColumn()
                && outer.getLastColumn() == inner.getLastColumn()) {
            return false;
        }
        return outer.getFirstRow() <= inner.getFirstRow()
                && outer.getLastRow() >= inner.getLastRow()
                && outer.getFirstColumn() <= inner.getFirstColumn()
                && outer.getLastColumn() >= inner.getLastColumn();
    }

}
