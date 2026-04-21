---
name: version-pinning
description: All dependency versions live in parent properties; child POMs never declare versions; frozen deps must not be upgraded without reading their constraint
globs: ["**/pom.xml"]
alwaysApply: false
---

RULE: Version numbers belong exclusively in parent `<properties>` (pom.xml lines 55–78). Child POMs never declare `<version>` on managed deps. Two versions are frozen with documented constraints — do not upgrade without understanding the constraint.

WHY: Centralizing versions prevents version skew between modules. Frozen versions have known compatibility constraints that upgrading would silently break.

Version properties block (pom.xml lines 55–78):
- `apache-poi.version` = `5.5.0` (line 62)
- `spring-expression.version` = `5.3.39` (line 65)
- `common-utils.version` = `0.14.0` (line 66)
- `jetbrains-annotations.version` = `26.0.2-1` (line 67)
- `lombok.version` = `1.18.42` (line 68)
- `junit5.version` = `5.14.1` (line 71)
- `assertj.version` = `3.27.6` (line 72)
- `spock.version` = `2.4-M6-groovy-4.0` (line 73)
- `jmh.version` = `1.37` (line 74)
- `byte-buddy.version` = `1.18.2` (line 75)
- `easy-random.version` = `5.0.0` (line 76)
- `excel-streaming-reader.version` = `5.1.2` (line 77)

Frozen versions (do NOT upgrade without reading the constraint):
- `easy-random-core` `5.0.0` — pom.xml lines 175–177 comment: "DO NOT UPGRADE THIS LIBRARY. EASY-RANDOM IS BASED ON JDK 11 SINCE 5.0.0." The project targets JDK 17 but easy-random 6+ changed its API and may break tests.
- `spring-expression` `5.3.39` — last 5.x release. Staying on 5.x minimizes shaded jar size; consumers who bring Spring 6 won't conflict because the package is relocated.

Adding a new dependency:
1. Add `<foo.version>x.y.z</foo.version>` to parent `<properties>`.
2. Add entry to parent `<dependencyManagement>` referencing `${foo.version}`.
3. Add `<dependency>` in the child pom without `<version>`.

**Don't:**
```xml
<!-- Never in a child pom -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.5.0</version> <!-- WRONG — version belongs in parent properties -->
</dependency>
```
