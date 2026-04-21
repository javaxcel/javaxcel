---
name: shaded-dependencies
description: spring-expression and its transitive deps are shaded; never expose them as compile deps or rename the relocation prefix
globs: ["**/pom.xml"]
alwaysApply: false
---

RULE: `spring-expression`, `spring-core`, and `spring-jcl` are shaded into the javaxcel-core jar at package time. Do not add them as compile-scope deps for consumers, do not rename the relocation prefix, and do not add new Spring classes to runtime paths without including them in `<artifactSet><includes>`.

WHY: Shading avoids forcing consumers into a specific Spring version. The relocation prefix `com.github.javaxcel.internal.springframework` is part of the ABI — renaming it breaks any code that has already loaded the shaded classes via reflection or serialization.

Evidence (core/pom.xml):
- Shade plugin execution `shade-dependencies-of-spring-expression`, lines 105–144
- `minimizeJar=true` (line 116) — only classes reachable from the artifact are kept; unreachable Spring classes are stripped
- `<artifactSet><includes>` (lines 121–125): `spring-expression`, `spring-core`, `spring-jcl`
- Relocation 1 — `org.springframework` → `com.github.javaxcel.internal.springframework` (lines 131–134)
- Relocation 2 — `org.apache.commons.logging` → `com.github.javaxcel.internal.springframework.jcl` (lines 136–139)
- Parent declares `spring-expression` as `optional=true` (no explicit scope = compile) at pom.xml lines 147–152

Rules:
- Adding a new Spring class used at runtime: add its artifact to `<artifactSet><includes>`, otherwise `minimizeJar=true` strips it → `ClassNotFoundException` at runtime.
- Do not change `shadedPattern` values; they are stable internal identifiers.
- `spring-jcl` relocation uses a different source pattern (`org.apache.commons.logging`) than the main Spring relocation.

**Do:**
```xml
<artifactSet>
    <includes>
        <include>org.springframework:spring-expression</include>
        <include>org.springframework:spring-core</include>
        <include>org.springframework:spring-jcl</include>
        <!-- add new spring artifact here if needed at runtime -->
    </includes>
</artifactSet>
```

**Don't:**
```xml
<!-- Never expose spring as compile dep to consumers -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-expression</artifactId>
    <scope>compile</scope> <!-- WRONG — shaded, must stay optional -->
</dependency>
```
