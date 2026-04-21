---
name: context-lifecycle
description: Reader/writer state flows through context objects via lifecycle hooks; never call Workbook/Sheet directly from a top-level impl.
globs: ["core/src/main/java/**/in/core/**", "core/src/main/java/**/out/core/**"]
alwaysApply: false
---

RULE: All mutable state for a read/write operation lives in `ExcelReadContext<T>` / `ExcelWriteContext<T>`. The abstract base classes own the `final read()` / `final write()` methods and call lifecycle hooks in order. Subclasses implement hooks, not the top-level method.

WHY: `AbstractExcelReader.read()` (lines 145-187) is `final` — it drives the lifecycle and cannot be overridden. The same pattern applies to `AbstractExcelWriter.write()`. Overriding the top-level method would bypass strategy application, sheet iteration, and lifecycle callbacks.

**Read lifecycle (ExcelReadLifecycle<T>):**
1. `prepare(context)` — called once before any sheet
2. `preReadSheet(context)` — called before each sheet
3. `readHeader(context)` — abstract; return header names for current sheet
4. `readBody(context)` — abstract; return model list for current sheet
5. `postReadSheet(context)` — called after each sheet
6. `complete(context)` — called once after all sheets

**Write lifecycle (ExcelWriteLifecycle<T>):**
1. `prepare(context)`
2. `preWriteSheet(context)`
3. `writeHeader(context)` — abstract
4. `writeBody(context)` — abstract
5. `postWriteSheet(context)`
6. `complete(context)`

All lifecycle methods have `default` (no-op) implementations; override only what the impl needs.

**Rules:**
- Extend `AbstractExcelReader` / `AbstractExcelWriter` and implement `readHeader`+`readBody` (or write equivalents).
- Do not override `read()` or `write()`.
- Do not access `Workbook` or `Sheet` directly from a top-level impl class — obtain them from `context.getWorkbook()` / `context.getSheet()`.
- State that must persist across lifecycle calls goes into context, not into instance fields of the impl.

**Do:**
```java
public class MyReader<T> extends AbstractExcelReader<T> {
    @Override protected List<String> readHeader(ExcelReadContext<T> context) { ... }
    @Override protected List<T> readBody(ExcelReadContext<T> context) { ... }
}
```

**Don't:**
```java
// Never override the final lifecycle driver
@Override public List<T> read() { ... }
```
