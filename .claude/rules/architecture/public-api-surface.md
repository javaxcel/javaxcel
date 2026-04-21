---
name: public-api-surface
description: Define and enforce the public API surface; everything under internal.* is off-limits to callers.
globs: ["core/src/main/java/**/*.java", "styler/src/main/java/**/*.java"]
alwaysApply: false
---

RULE: Only classes/interfaces explicitly listed below are public API. Everything under `com.github.javaxcel.core.internal.*` and the shaded `com.github.javaxcel.internal.springframework.*` is internal; users must never depend on it.

WHY: The facade + interface model lets the library evolve internal implementation without breaking callers. Internal packages carry no compatibility guarantee.

**Public API surface:**
- Facade: `com.github.javaxcel.core.Javaxcel` — sole entry point (`newInstance()`, `reader()`, `writer()`)
- Reader/writer interfaces: `ExcelReader`, `ExcelWriter` (in/core, out/core packages)
- Handler interface: `ExcelTypeHandler`, `ExcelTypeHandlerRegistry`
- Strategy interfaces: `ExcelReadStrategy`, `ExcelWriteStrategy`
- Style interface: `ExcelStyleConfig` (styler module) + `Configurer`
- Annotations: all types under `core.annotation.*`
- Strategy impls: `*/strategy/impl/**`
- Reader/writer impls: `*/core/impl/**` (ModelReader, MapReader, ModelWriter, MapWriter)

**NOT public API:**
- Anything under `com.github.javaxcel.core.internal.*` (converters, util, context internals)
- The shaded `com.github.javaxcel.internal.springframework.*` package (owned by the shade plugin — see `shaded-spring-expression.md`)
- Abstract base classes (`AbstractExcelReader`, `AbstractExcelWriter`, `AbstractExcelTypeHandler`) — extend only from within the library

**Do:**
```java
// Correct — program to interfaces
ExcelWriter<MyModel> writer = Javaxcel.newInstance().writer(workbook, MyModel.class);
```

**Don't:**
```java
// Wrong — bypasses public API surface
import com.github.javaxcel.core.internal.converter.out.ExcelWriteExpressionConverter;
```
