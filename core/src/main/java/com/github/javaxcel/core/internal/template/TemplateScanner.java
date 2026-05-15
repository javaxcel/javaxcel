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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.xmlbeans.XmlCursor;
import org.jspecify.annotations.Nullable;

import com.github.javaxcel.core.internal.util.ExcelUtils;

/**
 * Scans a template {@link Workbook} into a list of {@link SheetTemplate} ASTs.
 *
 * <p>Algorithm per sheet:
 * <ol>
 *   <li>Collect every cell with a {@code jxc:} directive comment along with
 *       its computed range (single row by default, or {@code A:until}).</li>
 *   <li>Sort directives by range area ascending, then by row/column for stable
 *       ordering. The deepest (smallest) block is the parent of any cell that
 *       lies within multiple ranges.</li>
 *   <li>Walk every cell. Choose the smallest range that contains it; the cell
 *       node attaches to that range's child list. Cells outside any range
 *       attach to the sheet's top-level node list.</li>
 *   <li>Wire blocks themselves into their enclosing parent block (or sheet
 *       root) by the same containment rule.</li>
 * </ol>
 *
 * <p>As a side effect of identifying a {@code jxc:} directive, the directive
 * comment is removed from its source cell during the same iteration step. This
 * is required so the rendered output workbook does not carry template metadata,
 * and it has to happen during the scan (not in a later pass) because sheets
 * backed by {@link org.apache.poi.xssf.streaming.SXSSFWorkbook SXSSFWorkbook}
 * flush rows older than the sliding window to a temporary file — a later pass
 * would not be able to re-open those rows to remove the comment. Non-directive
 * (user-authored) comments are left untouched.
 *
 * @since 0.x
 */
public final class TemplateScanner {

    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\$\\{[^}]+}");

    private TemplateScanner() {
    }

    /**
     * Scans every sheet of {@code workbook} and returns one
     * {@link SheetTemplate} per sheet.
     *
     * <p><strong>This call mutates the input workbook:</strong> every cell
     * comment recognised as a {@code jxc:} directive is removed from its source
     * cell. See the class-level Javadoc for the SXSSF rationale.
     */
    public static List<SheetTemplate> scan(Workbook workbook) {
        List<SheetTemplate> templates = new ArrayList<>();
        for (Sheet sheet : ExcelUtils.getSheets(workbook)) {
            templates.add(scanSheet(sheet));
        }
        return Collections.unmodifiableList(templates);
    }

    static SheetTemplate scanSheet(Sheet sheet) {
        List<DirectiveBlock> directives = collectDirectives(sheet);
        // Smallest first so that nested blocks "win" containment checks.
        directives.sort(Comparator
                .<DirectiveBlock>comparingInt(b -> DirectiveScannerSupport.area(b.range))
                .thenComparingInt(b -> b.range.getFirstRow())
                .thenComparingInt(b -> b.range.getFirstColumn()));

        List<TemplateNode> rootNodes = new ArrayList<>();

        // Wire each block into its enclosing parent (or root).
        for (int i = 0; i < directives.size(); i++) {
            DirectiveBlock block = directives.get(i);
            DirectiveBlock parent = findEnclosing(directives, i + 1, block.range);
            if (parent == null) {
                rootNodes.add(block.toNode());
            } else {
                parent.children.add(block.toNode());
            }
        }

        // Walk all cells; attach static / substitution cells to the deepest block (or root).
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell == null) {
                    continue;
                }
                CellAddress addr = cell.getAddress();
                @Nullable DirectiveBlock owner = findOwning(directives, addr);
                List<TemplateNode> bucket = owner == null ? rootNodes : owner.children;
                @Nullable TemplateNode cellNode = toCellNode(cell);
                if (cellNode != null) {
                    bucket.add(cellNode);
                }
            }
        }

        return new SheetTemplate(sheet.getSheetName(), Collections.unmodifiableList(rootNodes));
    }

    /**
     * Each directive becomes a {@link DirectiveBlock} carrying its range plus
     * a mutable child list to be filled during the cell walk.
     */
    private static List<DirectiveBlock> collectDirectives(Sheet sheet) {
        List<DirectiveBlock> blocks = new ArrayList<>();
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell == null) {
                    continue;
                }
                Comment comment = cell.getCellComment();
                if (comment == null) {
                    continue;
                }
                String text = comment.getString() == null ? null : comment.getString().getString();
                DirectiveParser.parse(text).ifPresent(spec -> {
                    CellRangeAddress range = computeRange(sheet, cell, spec);
                    blocks.add(new DirectiveBlock(spec, range, new ArrayList<>()));
                    // Consume the directive comment immediately so it is not
                    // carried into the rendered output. Doing it here — while
                    // the cell is still in the current sliding window — is the
                    // only safe point for SXSSF-backed sheets.
                    eraseDirectiveComment(cell);
                });
            }
        }
        return blocks;
    }

    /**
     * Fully detaches the directive comment from the workbook.
     *
     * <p>{@link Cell#removeCellComment()} alone is not sufficient for
     * {@code SXSSFCell} — it clears only the cell-local property and leaves the
     * underlying entry in the {@code commentsTable} (and the VML drawing shape)
     * intact. On serialisation that orphan re-attaches to whichever cell now
     * occupies the same coordinate. Dropping the {@code CTComment} XML element
     * directly removes the entry from the serialised {@code commentsTable},
     * which is the source of truth on reopen.
     */
    private static void eraseDirectiveComment(Cell cell) {
        Comment comment = cell.getCellComment();
        if (comment instanceof XSSFComment xc) {
            XmlCursor cursor = xc.getCTComment().newCursor();
            try {
                cursor.removeXml();
            } finally {
                cursor.dispose();
            }
        }
        cell.removeCellComment();
    }

    private static CellRangeAddress computeRange(Sheet sheet, Cell directiveCell, DirectiveSpec spec) {
        int firstRow = directiveCell.getRowIndex();
        int firstCol = directiveCell.getColumnIndex();
        @Nullable CellAddress until = spec instanceof DirectiveSpec.Each each ? each.untilAddress() : null;
        if (until != null) {
            return new CellRangeAddress(firstRow, until.getRow(), firstCol, until.getColumn());
        }
        // Default range: from the directive cell to the last cell of the same row.
        Row row = directiveCell.getRow();
        int lastCol = Math.max(firstCol, row.getLastCellNum() - 1);
        return new CellRangeAddress(firstRow, firstRow, firstCol, lastCol);
    }

    /**
     * Among directives sorted ascending by area, find the smallest one whose
     * range strictly contains the given block's range and is *not* equal to it.
     */
    private static @Nullable DirectiveBlock findEnclosing(
            List<DirectiveBlock> directives, int startIndex, CellRangeAddress inner) {
        for (int i = startIndex; i < directives.size(); i++) {
            DirectiveBlock candidate = directives.get(i);
            if (DirectiveScannerSupport.strictlyContains(candidate.range, inner)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Among directives sorted ascending by area, find the smallest range
     * containing the given cell address (any range — including equal range
     * registers a cell to that block).
     */
    private static @Nullable DirectiveBlock findOwning(List<DirectiveBlock> directives, CellAddress addr) {
        for (DirectiveBlock candidate : directives) {
            if (candidate.range.isInRange(addr.getRow(), addr.getColumn())) {
                return candidate;
            }
        }
        return null;
    }

    private static @Nullable TemplateNode toCellNode(Cell cell) {
        // Only STRING-typed cells can carry ${...} — everything else is static
        // and gets copied verbatim.
        if (cell.getCellType() != CellType.STRING) {
            return new TemplateNode.StaticCell(cell.getAddress());
        }
        String value = cell.getStringCellValue();
        if (value != null && SUBSTITUTION_PATTERN.matcher(value).find()) {
            return new TemplateNode.SubstitutionCell(cell.getAddress(), value);
        }
        return new TemplateNode.StaticCell(cell.getAddress());
    }

    private static final class DirectiveBlock {
        final DirectiveSpec spec;
        final CellRangeAddress range;
        final List<TemplateNode> children;

        DirectiveBlock(DirectiveSpec spec, CellRangeAddress range, List<TemplateNode> children) {
            this.spec = spec;
            this.range = range;
            this.children = children;
        }

        TemplateNode toNode() {
            if (spec instanceof DirectiveSpec.Each each) {
                return new TemplateNode.IterationBlock(
                        range, each.collectionExpr(), each.varName(), Collections.unmodifiableList(children));
            }
            DirectiveSpec.If cond = (DirectiveSpec.If) spec;
            return new TemplateNode.ConditionalBlock(
                    range, cond.conditionExpr(), Collections.unmodifiableList(children));
        }
    }

}
