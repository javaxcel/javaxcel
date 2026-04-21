---
name: common-utils-exclusions
description: Always exclude gson, ini4j, and commons-compress from common-utils in parent dependencyManagement
globs: ["**/pom.xml"]
alwaysApply: false
---

RULE: The parent `<dependencyManagement>` declaration for `io.github.imsejin:common-utils` must always exclude `gson`, `ini4j`, and `commons-compress`. Never remove these exclusions.

WHY: These are optional heavy transitive deps of `common-utils` that consumers do not expect and that frequently conflict with versions they already depend on.

Evidence (pom.xml lines 110–128):
```xml
<dependency>
    <groupId>io.github.imsejin</groupId>
    <artifactId>common-utils</artifactId>
    <version>${common-utils.version}</version>
    <exclusions>
        <exclusion>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.ini4j</groupId>
            <artifactId>ini4j</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-compress</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

- Child modules declare `common-utils` without `<version>` or `<exclusions>` — they inherit from parent management (core/pom.xml lines 29–32, styler/pom.xml lines 22–25).
- If you add a new exclusion, add a pom comment explaining which feature of `common-utils` pulls it in and why it is unwanted.

**Don't:**
```xml
<!-- Never remove an existing exclusion without a documented reason -->
<dependency>
    <groupId>io.github.imsejin</groupId>
    <artifactId>common-utils</artifactId>
    <!-- missing exclusions = leaking gson/ini4j/commons-compress to consumers -->
</dependency>
```
