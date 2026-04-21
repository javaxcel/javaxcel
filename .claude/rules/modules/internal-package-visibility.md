---
name: internal-package-visibility
description: Treat com.github.javaxcel.core.internal.* as non-public API; user code must not import from it.
globs: ["core/src/main/java/**/*.java"]
alwaysApply: false
---

RULE: Classes under `com.github.javaxcel.core.internal.*` are internal implementation — never part of the public API surface.

WHY: Internal classes may be `public` only so sibling sub-packages can cross package boundaries (e.g., `internal/analysis/` uses `internal/util/`). Exposing them to callers outside core creates fragile coupling.

- `internal/analysis/` — model reflection: `ExcelAnalysis`, `ExcelAnalyzer`, `AbstractExcelAnalyzer`, `ExcelAnalysisImpl`, `DefaultValueInfo`, `DefaultValueInfoImpl`, and sub-packages `in/` + `out/`.
- `internal/converter/` — read/write converters + SpEL expression converters; sub-packages `in/`, `in/support/`, `out/`, `out/support/`.
- `internal/util/` — `ObjectUtils`, `FieldUtils`, `ExcelUtils`.
- Never import `com.github.javaxcel.core.internal.*` from `annotation/`, `in/core/` (except `AbstractExcelReader`), `out/core/` (except `AbstractExcelWriter`), `Javaxcel.java`, or the `styler` module.
- New internal utilities go under `internal/util/` — do NOT put them in the `common-utils` library (`io.github.imsejin`).
- The shaded `com.github.javaxcel.internal.springframework.*` package is managed by the shade plugin; never create hand-written files there.

DO: Reference internal types freely within `internal/**` sub-packages.
DON'T: Import `com.github.javaxcel.core.internal.*` from public API classes or from the styler module.
