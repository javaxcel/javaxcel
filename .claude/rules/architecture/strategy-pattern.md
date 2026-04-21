---
name: strategy-pattern
description: Runtime options use the Strategy pattern; follow the established interface, naming, and dedup contract.
globs: ["core/src/main/java/**/strategy/**"]
alwaysApply: false
---

RULE: All runtime reader/writer options are expressed as Strategy objects, not constructor/setter parameters. Deduplification is by class name, not `equals()`/`hashCode()`.

WHY: Strategies are user-visible API; the class-name dedup in `AbstractExcelReader.options()` (lines 127-130) and its writer mirror ensure only one instance of each strategy type is active.

**Interfaces (core/src/main/java/…):**
- `com.github.javaxcel.core.in.strategy.ExcelReadStrategy` — `isSupported(ExcelReadContext<?>)` + `execute(ExcelReadContext<?>)`
- `com.github.javaxcel.core.out.strategy.ExcelWriteStrategy` — `isSupported(ExcelWriteContext<?>)` + `execute(ExcelWriteContext<?>)`

**Placement:**
- Read strategy impls → `core/src/main/java/com/github/javaxcel/core/in/strategy/impl/`
- Write strategy impls → `core/src/main/java/com/github/javaxcel/core/out/strategy/impl/`

**Dedup contract (AbstractExcelReader.java:127-130):**
- `options(ExcelReadStrategy...)` collects strategies into a `TreeSet` sorted by class name, then into an unmodifiable map keyed by class. Passing the same class twice keeps only one.
- `isSupported(context)` is evaluated before insertion; unsupported strategies are silently dropped.

**Marker-only strategies (checked by presence, not by `execute()`):**
- `execute()` must throw `UnsupportedOperationException` and be annotated `@ExcludeFromGeneratedJacocoReport`.
- Example: `Parallel.java` lines 59-63 — `isSupported` gates on `ModelReader`, `execute` throws.

**Naming:** Name impls by behavior, not with a `*Strategy` suffix.
- Correct: `Parallel`, `BodyStyles`, `HeaderNames`, `Limit`, `Filter`, `SheetName`, `AutoResizedColumns`, `DefaultValue`
- Wrong: `LimitStrategy`, `FilterStrategy`

**Do:**
```java
public class MyOption implements ExcelReadStrategy {
    @Override public boolean isSupported(ExcelReadContext<?> ctx) { return ModelReader.class.isAssignableFrom(ctx.getReaderType()); }
    @Override public Object execute(ExcelReadContext<?> ctx) { /* return computed value */ }
}
```

**Don't:**
```java
// Don't override equals/hashCode to control dedup — dedup is by class name only
@Override public boolean equals(Object o) { ... }
```
