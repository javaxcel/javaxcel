---
name: shaded-spring-expression
description: Spring Expression Language is shaded into an internal package; enforce import and pom.xml discipline.
globs: ["core/src/main/java/**/*.java", "core/pom.xml"]
alwaysApply: false
---

RULE: `org.springframework.*` is shaded and relocated to `com.github.javaxcel.internal.springframework.*` at package time (core/pom.xml lines 105-144, maven-shade-plugin with `minimizeJar=true`). Three artifacts are included: `spring-expression`, `spring-core`, `spring-jcl`.

WHY: Shading prevents version conflicts with applications that depend on Spring themselves and avoids exposing a transitive Spring dependency (see pom.xml comment at line 97-103).

**Allowlisted files that may import `org.springframework.*` (verified by grep):**
- `core/src/main/java/com/github/javaxcel/core/internal/converter/out/ExcelWriteExpressionConverter.java`
- `core/src/main/java/com/github/javaxcel/core/internal/converter/in/ExcelReadExpressionConverter.java`
- `core/src/main/java/com/github/javaxcel/core/in/resolver/ExcelModelExecutableParameterNameResolver.java`

**Rules:**
- No file outside the allowlist above may import `org.springframework.*`.
- Spring types must never appear in public method signatures, field types, or thrown exceptions visible from `Javaxcel`, `ExcelReader`, or `ExcelWriter`.
- When adding a new class that uses a transitive Spring artifact (e.g. `spring-jcl` commons-logging), add the artifact to `<artifactSet><includes>` in core/pom.xml lines 120-126; `minimizeJar=true` will otherwise strip it and cause `ClassNotFoundException` at runtime.
- Never hand-write classes under `com.github.javaxcel.internal.springframework.*` — that package is owned by the shade plugin.

**Do:**
```java
// Inside an allowlisted internal converter
import org.springframework.expression.ExpressionParser;
```

**Don't:**
```java
// In any public API class or outside the allowlist
import org.springframework.expression.Expression; // shade will rename this, caller sees wrong type
```
