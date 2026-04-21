---
name: jacoco-exclusion
description: Use @ExcludeFromGeneratedJacocoReport for unreachable-by-design code; do not suppress coverage any other way.
globs: ["core/src/main/java/**/*.java", "styler/src/main/java/**/*.java"]
alwaysApply: false
---

RULE: Annotate unreachable-by-design methods with `@io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport`; never delete, suppress-warn, or XML-exclude to hide coverage gaps.

WHY: The annotation is honored by the project's jacoco exclusion filter. Evidence: `Parallel.java:57-60` — `@ExcludeFromGeneratedJacocoReport` on `execute()` which throws `UnsupportedOperationException` because the strategy is a marker and its `execute()` is never called at runtime.

- Valid use cases: marker strategy `execute()` methods, impossible `default` branches, deliberate `UnsupportedOperationException` stubs.
- Import: `io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport` (from common-utils).
- Do NOT suppress by: deleting code, `@SuppressWarnings`, jacoco XML `<exclude>` entries, or commenting out lines.
- Do NOT use the annotation to hide legitimately testable code — it is for structurally unreachable paths only.

Do: `@ExcludeFromGeneratedJacocoReport` on a `throw new UnsupportedOperationException(…)` stub.
Don't: Add `<exclude>` in pom.xml jacoco config, or annotate a method just to avoid writing a test.
