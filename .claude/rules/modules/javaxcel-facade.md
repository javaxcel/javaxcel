---
name: javaxcel-facade
description: Javaxcel is the sole entry point; all reader/writer creation must go through its factory methods.
globs: ["core/src/main/java/com/github/javaxcel/core/Javaxcel.java", "README.md", "core/README.md"]
alwaysApply: false
---

RULE: `com.github.javaxcel.core.Javaxcel` is the single user-facing factory; direct construction of reader/writer impls is internal only.

WHY: The facade enforces consistent registry injection and defensive copying. Bypassing it lets user code hold a direct reference to internal impls without registry setup.

- `Javaxcel.newInstance()` — creates instance with `DefaultExcelTypeHandlerRegistry`.
- `Javaxcel.newInstance(registry)` — defensive-copies into a new `StrictExcelTypeHandlerRegistry`; user's registry reference is NOT aliased.
- `writer(Workbook, Class<T>)` → `DefaultExcelWriter.forModel(...)` returning `ExcelWriter<T>`.
- `writer(Workbook)` → `DefaultExcelWriter.forMap(...)` returning `ExcelWriter<Map<String, Object>>`.
- `reader(Workbook, Class<T>)` → `DefaultExcelReader.forModel(...)` returning `ExcelReader<T>`.
- `reader(Workbook)` → `DefaultExcelReader.forMap(...)` returning `ExcelReader<Map<String, String>>`.
- `templateWriter(Workbook)` → `DefaultExcelTemplateWriter.create(...)` returning `ExcelTemplateWriter`. The template workbook is consumed in place — its sheets are rewritten with evaluated content.
- `registry` field is package-private + `@VisibleForTesting` — never expose it via getter or widen its visibility.
- Adding a new reader/writer implementation: extend the descriptor / assembler stack under `internal/descriptor` and `internal/assembler`; do not introduce a parallel `*Reader` / `*Writer` class hierarchy.
- The legacy `ModelReader` / `MapReader` / `ModelWriter` / `MapWriter` shim classes are `@Deprecated(forRemoval = true)` thin delegators kept only for source compatibility; never construct them in new code.

DO: Document and demonstrate `Javaxcel.newInstance().writer(wb, MyModel.class)` in READMEs.
DON'T: Show `new ModelWriter(...)` / `new MapWriter(...)` / `new ModelReader(...)` / `new MapReader(...)` / `new DefaultExcelWriter(...)` as user-facing API.
