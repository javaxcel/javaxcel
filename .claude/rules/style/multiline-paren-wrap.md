---
name: multiline-paren-wrap
description: When method/constructor parameters or call arguments are wrapped one-per-line (chop-down), the closing `)` goes on its own line aligned to the declaration/statement start.
globs: ["**/*.java", "**/*.groovy"]
alwaysApply: false
---

RULE: When a method/constructor declaration or invocation is wrapped one-parameter-per-line (chop-down), the closing `)` goes on its own line, aligned to the start of the declaration or to the receiver of the call. Applies to both signatures (`) {`) and invocations (`);`).

WHY: `intellij-code-style.xml` sets `METHOD_PARAMETERS_WRAP="1"` (line 71) and `CALL_PARAMETERS_WRAP="1"` (line 70). The formatter chops on overflow but does not push the closing `)` to its own line — the project convention adds that step so adding or removing a trailing parameter is a one-line diff and the wrapped block is visually delimited like a brace body. In-repo evidence: `DefaultExcelWriter.java:99-104` (constructor) and `:113-117` (`new ExcelWriteContext<>(...)`).

**Does NOT apply when:**
- The signature/call fits on one line.
- All parameters/arguments fit on a single continuation line — keep `)` at the end of that line. See `DefaultExcelWriter.java:122-123`:
  ```java
  public static <T> DefaultExcelWriter<T> forModel(
          Workbook workbook, Class<T> type, ExcelTypeHandlerRegistry registry) {
  ```

**Do — multi-line declaration:**
```java
private DefaultExcelWriter(
        Workbook workbook,
        Class<T> modelType,
        @Nullable ExcelTypeHandlerRegistry registry,
        boolean modelMode
) {
```

**Don't — multi-line declaration with `)` glued to the last parameter:**
```java
private DefaultExcelWriter(
        Workbook workbook,
        Class<T> modelType,
        @Nullable ExcelTypeHandlerRegistry registry,
        boolean modelMode) {
```

**Do — multi-line invocation:**
```java
this.context = new ExcelWriteContext<>(
        workbook,
        modelType,
        (Class<? extends ExcelWriter<T>>) (Class<?>) DefaultExcelWriter.class
);
```

**Don't — multi-line invocation with `)` glued to the last argument:**
```java
this.context = new ExcelWriteContext<>(
        workbook, modelType, (Class<? extends ExcelWriter<T>>) (Class<?>) DefaultExcelWriter.class);
```

**Indentation:**
- Closing `)` aligns with the first character of the declaration (e.g. `private`) or the receiver of the call (e.g. `this.context`).
- Parameters/arguments use one continuation indent (8 spaces in this project).
- `{` or `;` follows `)` on the same line — `) {` for declarations, `);` for invocations.

**Verification:**
`grep -nE '^[[:space:]]+\) \{$' core/src/main/java/com/github/javaxcel/core/out/core/impl/DefaultExcelWriter.java` matches `104:    ) {` (the constructor's closing `)` after `boolean modelMode`).
