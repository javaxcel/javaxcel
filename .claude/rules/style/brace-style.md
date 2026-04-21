---
name: brace-style
description: Braces are required on all control-flow bodies, including single-statement branches.
globs: ["**/*.java", "**/*.groovy"]
alwaysApply: false
---

RULE: All `if`, `else`, `while`, `for`, and `do` bodies must use braces — `IF_BRACE_FORCE=3` (Always).

WHY: `intellij-code-style.xml` sets `IF_BRACE_FORCE value="3"` for both Java (line 80) and Groovy (line 58).

```java
// Do
if (condition) {
    doSomething();
}

// Don't
if (condition)
    doSomething();
```

- Applies to `if`, `else if`, `else`, `for`, `while`, and `do-while` — no exceptions for one-liners.
- Opening brace stays on the same line as the control keyword (K&R style, IntelliJ default).
