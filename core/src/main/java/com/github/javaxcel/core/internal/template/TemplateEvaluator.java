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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ReflectionUtils;

import com.github.javaxcel.core.internal.util.ExcelUtils;

/**
 * Evaluates a {@link SheetTemplate} AST against a context map, mutating the
 * source sheet so that the result occupies it in-place.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Snapshot every cell's value and style before any structural change.</li>
 *   <li>Clear the sheet.</li>
 *   <li>Walk the AST — for each top-level segment, emit one or more output
 *       rows. Iteration blocks expand by their collection size; conditional
 *       blocks render only when truthy. Nested blocks recurse with a fresh
 *       row-base offset.</li>
 * </ol>
 *
 * @since 0.x
 */
public final class TemplateEvaluator {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private static final ParserContext TEMPLATE_PARSER_CONTEXT = new ParserContext() {
        @Override
        public boolean isTemplate() {
            return true;
        }

        @Override
        public String getExpressionPrefix() {
            return "${";
        }

        @Override
        public String getExpressionSuffix() {
            return "}";
        }
    };

    private final Map<String, Expression> templateCache = new HashMap<>();

    private final Map<String, Expression> spelCache = new HashMap<>();

    /**
     * Evaluates every sheet of {@code workbook} in place.
     */
    public void evaluate(Workbook workbook, Map<String, Object> context) {
        Asserts.that(workbook)
                .describedAs("TemplateEvaluator.workbook is not allowed to be null")
                .isNotNull();
        Asserts.that(context)
                .describedAs("TemplateEvaluator.context is not allowed to be null")
                .isNotNull();

        for (Sheet sheet : ExcelUtils.getSheets(workbook)) {
            evaluateSheet(sheet, context);
        }
    }

    void evaluateSheet(Sheet sheet, Map<String, Object> context) {
        SheetTemplate template = TemplateScanner.scanSheet(sheet);

        // Snapshot every original cell BEFORE mutating the sheet.
        Map<CellAddress, CellSnapshot> snapshots = captureCells(sheet);

        // Clear the sheet — every row is removed; the new content will be
        // written below using the snapshots as the source of styles/values.
        clearRows(sheet);

        // Use a mutable copy as the SpEL root so loop variables can be
        // bound/unbound during iteration.
        Map<String, Object> rootMap = new HashMap<>(context);
        StandardEvaluationContext spelCtx = createSpelContext(rootMap);
        emitNodes(template.nodes(), sheet, snapshots, spelCtx, rootMap, 0, 0);
    }

    static StandardEvaluationContext createSpelContext(Map<String, Object> rootMap) {
        // The context Map serves as the SpEL root object so top-level keys
        // can be referenced as bare identifiers — `${author.name}` resolves
        // to rootMap.get("author").name via MapPropertyAccessor.
        StandardEvaluationContext ctx = new StandardEvaluationContext(rootMap);
        ctx.addPropertyAccessor(new MapPropertyAccessor());
        // Mirror keys as variables so `#name` access also works for callers
        // that prefer explicit variable syntax.
        rootMap.forEach(ctx::setVariable);
        return ctx;
    }

    /**
     * Recursively emits a list of template nodes to the sheet.
     *
     * @param nodes        nodes to emit (top-level for the sheet, or block children)
     * @param sheet        target sheet (also the source of snapshots for static cells)
     * @param snapshots    captured cell snapshots keyed by original address
     * @param spelCtx      SpEL evaluation context (mutated as variables are bound)
     * @param outRowStart  output row index where the FIRST source row should land
     * @param srcRowOrigin source row index that maps to {@code outRowStart}
     * @return number of output rows consumed by this segment
     */
    int emitNodes(List<TemplateNode> nodes, Sheet sheet, Map<CellAddress, CellSnapshot> snapshots,
            StandardEvaluationContext spelCtx, Map<String, Object> rootMap, int outRowStart, int srcRowOrigin) {
        // Group consecutive cell nodes on the same source row, separated by blocks.
        List<Segment> segments = buildSegments(nodes);
        int outRow = outRowStart;
        int prevSrcRow = srcRowOrigin - 1;

        for (Segment seg : segments) {
            // Pad the gap between the previous segment's source row and this one's start.
            int gap = seg.startRow() - (prevSrcRow + 1);
            if (gap > 0) {
                outRow += gap;
            }

            if (seg instanceof RowSegment row) {
                emitCellRow(row.cells(), sheet, snapshots, spelCtx, outRow);
                outRow++;
                prevSrcRow = row.row;
            } else if (seg instanceof BlockSegment blockSeg) {
                int height = blockSeg.height();
                if (blockSeg.block instanceof TemplateNode.IterationBlock iter) {
                    Iterable<?> items = resolveIterable(evaluateSpel(iter.collectionExpr(), spelCtx));
                    int firstSrcRow = iter.range().getFirstRow();
                    Object savedRoot = rootMap.get(iter.varName());
                    boolean hadKey = rootMap.containsKey(iter.varName());
                    Object savedVar = spelCtx.lookupVariable(iter.varName());
                    for (Object item : items) {
                        spelCtx.setVariable(iter.varName(), item);
                        rootMap.put(iter.varName(), item);
                        // Use the actual consumed output rows so a nested each that
                        // expands beyond its source height doesn't collide with the
                        // next outer iteration.
                        int consumed = emitNodes(iter.children(), sheet, snapshots, spelCtx, rootMap,
                                outRow, firstSrcRow);
                        outRow += Math.max(consumed, height);
                    }
                    // Restore previous binding (supports nested each with same var name in outer scope).
                    spelCtx.setVariable(iter.varName(), savedVar);
                    if (hadKey) {
                        rootMap.put(iter.varName(), savedRoot);
                    } else {
                        rootMap.remove(iter.varName());
                    }
                } else {
                    TemplateNode.ConditionalBlock cond = (TemplateNode.ConditionalBlock) blockSeg.block;
                    boolean truthy = isTruthy(evaluateSpel(cond.conditionExpr(), spelCtx));
                    if (truthy) {
                        int consumed = emitNodes(cond.children(), sheet, snapshots, spelCtx, rootMap,
                                outRow, cond.range().getFirstRow());
                        outRow += Math.max(consumed, height);
                    }
                }
                prevSrcRow = blockSeg.lastSrcRow();
            }
        }

        return outRow - outRowStart;
    }

    private void emitCellRow(List<TemplateNode> cells, Sheet sheet, Map<CellAddress, CellSnapshot> snapshots,
            StandardEvaluationContext spelCtx, int outRow) {
        Row row = sheet.getRow(outRow);
        if (row == null) {
            row = sheet.createRow(outRow);
        }
        for (TemplateNode node : cells) {
            CellAddress addr = nodeAddress(node);
            CellSnapshot snap = snapshots.get(addr);
            if (snap == null) {
                continue;
            }
            Cell out = row.getCell(addr.getColumn());
            if (out == null) {
                out = row.createCell(addr.getColumn());
            }
            if (snap.style != null) {
                out.setCellStyle(snap.style);
            }
            if (node instanceof TemplateNode.SubstitutionCell sub) {
                Object value = evaluate(sub.text(), spelCtx);
                writeTypedValue(out, value);
            } else {
                writeSnapshotValue(out, snap);
            }
        }
    }

    private CellAddress nodeAddress(TemplateNode node) {
        if (node instanceof TemplateNode.StaticCell sc) {
            return sc.coord();
        }
        if (node instanceof TemplateNode.SubstitutionCell sb) {
            return sb.coord();
        }
        throw new IllegalStateException("Cell node expected, got: " + node.getClass().getName());
    }

    private List<Segment> buildSegments(List<TemplateNode> nodes) {
        // Split nodes into row-grouped cell segments and standalone block segments.
        List<TemplateNode> sorted = new ArrayList<>(nodes);
        sorted.sort((a, b) -> {
            int ar = startRow(a);
            int br = startRow(b);
            if (ar != br) {
                return Integer.compare(ar, br);
            }
            return Integer.compare(startCol(a), startCol(b));
        });

        List<Segment> segments = new ArrayList<>();
        List<TemplateNode> currentRow = new ArrayList<>();
        int currentRowIdx = -1;

        for (TemplateNode node : sorted) {
            if (node instanceof TemplateNode.IterationBlock || node instanceof TemplateNode.ConditionalBlock) {
                if (!currentRow.isEmpty()) {
                    segments.add(new RowSegment(currentRowIdx, List.copyOf(currentRow)));
                    currentRow.clear();
                    currentRowIdx = -1;
                }
                segments.add(new BlockSegment(node));
                continue;
            }
            int row = startRow(node);
            if (currentRowIdx == -1 || row == currentRowIdx) {
                currentRow.add(node);
                currentRowIdx = row;
            } else {
                segments.add(new RowSegment(currentRowIdx, List.copyOf(currentRow)));
                currentRow.clear();
                currentRow.add(node);
                currentRowIdx = row;
            }
        }
        if (!currentRow.isEmpty()) {
            segments.add(new RowSegment(currentRowIdx, List.copyOf(currentRow)));
        }
        return segments;
    }

    private int startRow(TemplateNode node) {
        if (node instanceof TemplateNode.StaticCell sc) {
            return sc.coord().getRow();
        }
        if (node instanceof TemplateNode.SubstitutionCell sb) {
            return sb.coord().getRow();
        }
        if (node instanceof TemplateNode.IterationBlock ib) {
            return ib.range().getFirstRow();
        }
        TemplateNode.ConditionalBlock cb = (TemplateNode.ConditionalBlock) node;
        return cb.range().getFirstRow();
    }

    private int startCol(TemplateNode node) {
        if (node instanceof TemplateNode.StaticCell sc) {
            return sc.coord().getColumn();
        }
        if (node instanceof TemplateNode.SubstitutionCell sb) {
            return sb.coord().getColumn();
        }
        if (node instanceof TemplateNode.IterationBlock ib) {
            return ib.range().getFirstColumn();
        }
        TemplateNode.ConditionalBlock cb = (TemplateNode.ConditionalBlock) node;
        return cb.range().getFirstColumn();
    }

    /**
     * Evaluates a cell-template string with {@code ${...}} delimiters.
     * Mixed text and expressions return a {@link String}; a single full-cell
     * expression returns its native typed value.
     */
    private Object evaluate(String expression, StandardEvaluationContext spelCtx) {
        Expression compiled = templateCache.computeIfAbsent(expression,
                expr -> EXPRESSION_PARSER.parseExpression(expr, TEMPLATE_PARSER_CONTEXT));
        return compiled.getValue(spelCtx);
    }

    /**
     * Evaluates a raw SpEL expression — used by directive bodies
     * ({@code each <collectionExpr>}, {@code if <conditionExpr>}). No
     * {@code ${...}} delimiters are expected here.
     */
    private Object evaluateSpel(String expression, StandardEvaluationContext spelCtx) {
        Expression compiled = spelCache.computeIfAbsent(expression, EXPRESSION_PARSER::parseExpression);
        return compiled.getValue(spelCtx);
    }

    private static Iterable<?> resolveIterable(Object collection) {
        if (collection == null) {
            return List.of();
        }
        if (collection instanceof Iterable<?> it) {
            return it;
        }
        if (collection.getClass().isArray()) {
            int len = Array.getLength(collection);
            List<Object> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                list.add(Array.get(collection, i));
            }
            return list;
        }
        throw new IllegalArgumentException("Cannot iterate over: " + collection.getClass().getName());
    }

    private static boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.doubleValue() != 0.0;
        }
        if (value instanceof CharSequence cs) {
            return !cs.isEmpty();
        }
        if (value instanceof Collection<?> c) {
            return !c.isEmpty();
        }
        if (value instanceof Map<?, ?> m) {
            return !m.isEmpty();
        }
        return true;
    }

    private static void writeTypedValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (value instanceof Boolean b) {
            cell.setCellValue(b);
        } else if (value instanceof Date d) {
            cell.setCellValue(d);
        } else if (value instanceof LocalDate ld) {
            cell.setCellValue(ld);
        } else if (value instanceof LocalDateTime ldt) {
            cell.setCellValue(ldt);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private static void writeSnapshotValue(Cell cell, CellSnapshot snap) {
        switch (snap.type) {
            case STRING -> cell.setCellValue((String) snap.value);
            case NUMERIC -> cell.setCellValue((double) snap.value);
            case BOOLEAN -> cell.setCellValue((boolean) snap.value);
            case FORMULA -> cell.setCellFormula((String) snap.value);
            case BLANK, _NONE, ERROR -> cell.setBlank();
            default -> {
                if (snap.value != null) {
                    cell.setCellValue(snap.value.toString());
                }
            }
        }
    }

    private static Map<CellAddress, CellSnapshot> captureCells(Sheet sheet) {
        Map<CellAddress, CellSnapshot> map = new LinkedHashMap<>();
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell == null) {
                    continue;
                }
                map.put(cell.getAddress(), CellSnapshot.from(cell));
            }
        }
        return map;
    }

    private static void clearRows(Sheet sheet) {
        List<Row> rows = new ArrayList<>();
        sheet.iterator().forEachRemaining(rows::add);
        for (Row row : rows) {
            sheet.removeRow(row);
        }
    }

    /** Captured state of an original template cell. */
    static final class CellSnapshot {
        final CellAddress address;
        final CellType type;
        final Object value;
        final CellStyle style;

        private CellSnapshot(CellAddress address, CellType type, Object value, CellStyle style) {
            this.address = address;
            this.type = type;
            this.value = value;
            this.style = style;
        }

        static CellSnapshot from(Cell cell) {
            CellType type = cell.getCellType();
            Object value;
            switch (type) {
                case STRING -> value = cell.getStringCellValue();
                case NUMERIC -> value = cell.getNumericCellValue();
                case BOOLEAN -> value = cell.getBooleanCellValue();
                case FORMULA -> value = cell.getCellFormula();
                default -> value = null;
            }
            return new CellSnapshot(cell.getAddress(), type, value, cell.getCellStyle());
        }
    }

    // Internal segment types for ordering within emitNodes ---------------------------------------

    sealed interface Segment {
        int startRow();
    }

    record RowSegment(int row, List<TemplateNode> cells) implements Segment {
        @Override
        public int startRow() {
            return row;
        }
    }

    static final class BlockSegment implements Segment {
        final TemplateNode block;

        BlockSegment(TemplateNode block) {
            this.block = block;
        }

        @Override
        public int startRow() {
            if (block instanceof TemplateNode.IterationBlock ib) {
                return ib.range().getFirstRow();
            }
            TemplateNode.ConditionalBlock cb = (TemplateNode.ConditionalBlock) block;
            return cb.range().getFirstRow();
        }

        int height() {
            if (block instanceof TemplateNode.IterationBlock ib) {
                return ib.range().getLastRow() - ib.range().getFirstRow() + 1;
            }
            TemplateNode.ConditionalBlock cb = (TemplateNode.ConditionalBlock) block;
            return cb.range().getLastRow() - cb.range().getFirstRow() + 1;
        }

        int lastSrcRow() {
            if (block instanceof TemplateNode.IterationBlock ib) {
                return ib.range().getLastRow();
            }
            TemplateNode.ConditionalBlock cb = (TemplateNode.ConditionalBlock) block;
            return cb.range().getLastRow();
        }
    }

    /**
     * Reflection-based POJO → Map converter for context normalization.
     * Map values pass through unchanged; everything else gets every accessible
     * instance field projected as a {@code Map} entry.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toContextMap(Object contextOrMap) {
        if (contextOrMap == null) {
            return Map.of();
        }
        if (contextOrMap instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        Class<?> type = contextOrMap.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                field.trySetAccessible();
                map.putIfAbsent(field.getName(), ReflectionUtils.getFieldValue(contextOrMap, field));
            }
            type = type.getSuperclass();
        }
        return map;
    }

}
