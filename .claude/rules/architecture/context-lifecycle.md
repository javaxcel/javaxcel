---
name: context-lifecycle
description: Reader/writer state flows through context objects; the engines own the final read/write lifecycle.
globs: ["core/src/main/java/**/in/core/**", "core/src/main/java/**/out/core/**"]
alwaysApply: false
---

RULE: All mutable state for a read/write operation lives in `ExcelReadContext<T>` / `ExcelWriteContext<T>`. The single engines (`DefaultExcelReader` / `DefaultExcelWriter`) own the `final read()` / `final write()` methods that drive sheet iteration. Adding a new reader/writer is done by composing a different `List<ColumnDescriptor<T>>` (and an `Assembler` for reads), not by extending the engines.

WHY: `DefaultExcelReader.read()` and `DefaultExcelWriter.write()` are `final` — they drive the lifecycle and cannot be overridden. The legacy abstract-class lifecycle was replaced in 0.x: column-level behavior is now expressed by `ColumnDescriptor<T>` (in `core/internal/descriptor/`) instead of subclass hooks.

**Engine entry points:**
- `DefaultExcelReader.forModel(workbook, type, registry)` — model-based read
- `DefaultExcelReader.forMap(workbook)` — map-based read
- `DefaultExcelWriter.forModel(workbook, type, registry)` — model-based write
- `DefaultExcelWriter.forMap(workbook)` — map-based write

**Read flow** (inside `DefaultExcelReader.read()`):
1. Resolve `Limit` and `KeyNames` strategies up-front.
2. Build descriptors (Model: `ModelDescriptorFactory.forRead`; Map: per-sheet from header row).
3. For each sheet: read rows as `Map<String, String>`, run validators per column, hand off to `ModelAssembler<T>` (or pass-through `MapAssembler`).

**Write flow** (inside `DefaultExcelWriter.write()`):
1. Build descriptors (Model: `ModelDescriptorFactory.forWrite`; Map: `MapDescriptorFactory.forWrite`).
2. Resolve header/body styles (`HeaderStyles`/`BodyStyles` strategy override → descriptor styles fallback). Identity-based `CellStyle` cache shares one workbook style across descriptors that point at the same `ExcelStyleConfig` instance.
3. Partition list by `ExcelUtils.getMaxRows(workbook) - 1` → one chunk per sheet.
4. Per sheet: create header row, body rows via `descriptor.writeValue(model)`, apply Filter/EnumDropdown/AutoResizedColumns/HiddenExtra*, then save.

**Rules:**
- Do not override `read()` or `write()`; both are `final`.
- Do not access `Workbook` or `Sheet` directly from a top-level impl class — obtain them from `context.getWorkbook()` / `context.getSheet()`.
- Per-column behavior (read/write conversion, styles, validators, dropdowns) goes into a `ColumnDescriptor<T>` impl, not a subclass.
- The deprecated shims (`ModelReader`, `MapReader`, `ModelWriter`, `MapWriter`) are thin facade-compat classes that delegate to a `DefaultExcel*`; never inherit from them.

**Do:**
```java
ExcelReader<MyDto> reader = Javaxcel.newInstance().reader(workbook, MyDto.class);
List<MyDto> rows = reader.options(new Limit(100)).read();
```

**Don't:**
```java
// AbstractExcelReader/Writer no longer exist; do not try to extend them.
public class MyReader<T> extends AbstractExcelReader<T> { /* compile error */ }

// Never override the final lifecycle driver
@Override public List<T> read() { ... }
```
