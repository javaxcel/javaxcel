---
name: assertion-library
description: Main-code preconditions use Asserts (imsejin-common); never use Objects.requireNonNull or raw throws for user-input validation.
globs: ["core/src/main/java/**/*.java", "styler/src/main/java/**/*.java"]
alwaysApply: false
---

RULE: Use `io.github.imsejin.common.assertion.Asserts` with fluent `.describedAs(message, args)` chains for all precondition checks in main source. Do not use `Objects.requireNonNull`, raw `throw new IllegalArgumentException(...)`, or Guava Preconditions.

WHY: `Asserts` provides consistent, formatted error messages with positional args and chainable conditions. The codebase is uniform on this; mixing styles fragments the error-message contract.

**Verified examples:**
- `AbstractExcelReader.options()` lines 115-119 — null-check then doesNotContainNull on strategy array
- `AbstractExcelTypeHandler(Class<T>)` lines 41-44 — single `.isNotNull()` on constructor arg
- `BodyStyles(List<ExcelStyleConfig>)` lines 40-49 — chained `.isNotNull().isNotEmpty().doesNotContainNull().isInstanceOf(List.class)`

**Pattern:**
```java
Asserts.that(value)
        .describedAs("ClassName.fieldName is not allowed to be null")
        .isNotNull()
        .describedAs("ClassName.fieldName cannot have null element: {0}", value)
        .doesNotContainNull();
```

**Message conventions:**
- Null check: `"ClassName.fieldOrParam is not allowed to be null"`
- Collection null element: `"ClassName.fieldOrParam cannot have null element: {0}"` (include value as `{0}`)
- Type check: `"ClassName.fieldOrParam must be an implementation of SomeInterface: {0}"`

**Test code is different:** Test files (Spock/JUnit) use AssertJ's `assertThat(...)` — that is not the same library. Do not use AssertJ in main source.

**Don't:**
```java
Objects.requireNonNull(strategies, "strategies must not be null");   // wrong
if (strategies == null) throw new IllegalArgumentException("...");   // wrong
checkNotNull(strategies);                                             // wrong (Guava)
```
