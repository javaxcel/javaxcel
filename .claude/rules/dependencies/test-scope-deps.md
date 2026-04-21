---
name: test-scope-deps
description: All test framework and test-utility dependencies must stay scope=test and must never be referenced from src/main
globs: ["**/pom.xml"]
alwaysApply: false
---

RULE: The following dependencies are `scope=test` in parent `<dependencyManagement>` and must never be changed to `provided` or `compile`, and must never be imported from `src/main/java/**`.

Deps (all declared `scope=test` in pom.xml lines 178–248):
- `org.junit.jupiter:junit-jupiter-engine` (lines 201–207)
- `org.junit.jupiter:junit-jupiter-api` (lines 208–213)
- `org.junit.jupiter:junit-jupiter-params` (lines 214–219)
- `org.assertj:assertj-core` (lines 221–227)
- `org.spockframework:spock-core` (lines 235–240)
- `org.openjdk.jmh:jmh-generator-annprocess` (lines 243–248)
- `org.jeasy:easy-random-core` (lines 178–183)
- `net.bytebuddy:byte-buddy` (lines 185–191)
- `com.github.pjfanning:excel-streaming-reader` (lines 193–199)

Additional constraints:
- `excel-streaming-reader` is used in tests for streaming integration tests only; it is NOT a runtime dependency of javaxcel-core.
- `byte-buddy` is used solely for test-side dynamic class generation.
- `easy-random-core` is frozen at `5.0.0` — see version-pinning.md.
- Child modules declare all these without `<scope>` (inherited from parent management).

**Don't:**
```xml
<!-- Never in src/main/java imports, never change scope -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>compile</scope> <!-- WRONG -->
</dependency>
```
