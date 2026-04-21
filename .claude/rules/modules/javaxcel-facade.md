---
name: javaxcel-facade
description: Javaxcel is the sole entry point; all reader/writer creation must go through its factory methods.
globs: ["core/src/main/java/com/github/javaxcel/core/Javaxcel.java", "README.md", "core/README.md"]
alwaysApply: false
---

RULE: `com.github.javaxcel.core.Javaxcel` is the single user-facing factory; direct construction of reader/writer impls is internal only.

WHY: The facade enforces consistent registry injection and defensive copying. Bypassing it lets user code hold a direct reference to internal impls without registry setup.

- `Javaxcel.newInstance()` — creates instance with `DefaultExcelTypeHandlerRegistry` (Javaxcel.java:51-53).
- `Javaxcel.newInstance(registry)` — defensive-copies into a new `StrictExcelTypeHandlerRegistry` (Javaxcel.java:55-61); user's registry reference is NOT aliased.
- `writer(Workbook, Class<T>)` → `ModelWriter` (Javaxcel.java:71-73).
- `writer(Workbook)` → `MapWriter` (Javaxcel.java:81-83).
- `reader(Workbook, Class<T>)` → `ModelReader` (Javaxcel.java:93-95).
- `reader(Workbook)` → `MapReader` (Javaxcel.java:103-105).
- `registry` field is package-private + `@VisibleForTesting` (Javaxcel.java:44-45) — never expose it via getter or widen its visibility.
- Adding a new reader/writer implementation requires a corresponding facade method, not just a public constructor.

DO: Document and demonstrate `Javaxcel.newInstance().writer(wb, MyModel.class)` in READMEs.
DON'T: Show `new ModelWriter(...)` / `new MapWriter(...)` / `new ModelReader(...)` / `new MapReader(...)` as user-facing API.
