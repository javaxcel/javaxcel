RULE: When `@Nullable` annotates a method's return type, place it on its own line above the method declaration (and above any other annotations except `@Override`, which conventionally sits below `@Nullable`). Do not inline `@Nullable` between modifiers and the return type.

WHY: The project uniformly applies `@Nullable` on its own line above method signatures. Examples: `ObjectUtils.java:63-64` `@Nullable` then `public static <T> T resolveFirst(...)`; `ExcelReadHandlerConverter.java:86-88` `@Nullable` then `@Override` then `public Object convert(...)`. Inlining `@Nullable` between modifiers and the return type fragments the visual signature scan and diverges from the established convention.

**Rules:**
- Methods returning a nullable value: `@Nullable` goes on its own line above the method declaration.
- When `@Override` is also present, order them as `@Nullable` then `@Override` then the modifiers.
- Field declarations are *not* covered by this rule — fields use the inline `private @Nullable Type field;` style consistently across the codebase (see `ExcelAnalysisImpl.java:45`, `ExcelReadContext.java:72`).
- Method parameters keep `@Nullable` inline before the parameter type (`void validate(@Nullable String cellValue)`), because the alternative would require splitting the parameter across lines.
- This rule applies to both `org.jspecify.annotations.Nullable` (preferred for new code) and `org.jetbrains.annotations.Nullable` (legacy).

**Do:**
```java
@Nullable
public static <T> T resolveFirst(Class<T> type, Object... arguments) { ... }

@Nullable
@Override
public Object convert(Map<String, String> variables, Field field) { ... }

@Nullable
private static FormulaEvaluator resolveFormulaEvaluator(Workbook workbook) { ... }
```

**Don't:**
```java
// Wrong — inline between modifiers and return type
public @Nullable Object readValue(Map<String, String> row) { ... }
private static @Nullable String resolveDefaultValue(...) { ... }
```

**Verification:**
- `grep -rn 'public @Nullable\|private @Nullable\|protected @Nullable\|static @Nullable' core/src/main/java/` should return zero hits for method declarations (field declarations such as `private @Nullable Sheet sheet;` are still permitted).
