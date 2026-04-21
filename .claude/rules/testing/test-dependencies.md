---
name: test-dependencies
description: Test-scoped dependency rules — versions, freeze constraints, and placement.
globs: ["**/pom.xml", "**/src/test/**"]
alwaysApply: false
---

RULE: Test dependencies stay in `<scope>test</scope>`; never import them from `src/main/java/**`. easy-random MUST stay at 5.0.0.

WHY: All test deps are declared `scope=test` in parent `<dependencyManagement>` (`pom.xml:178-248`). easy-random is frozen by explicit comment at `pom.xml:175-177`: "DO NOT UPGRADE THIS LIBRARY. EASY-RANDOM IS BASED ON JDK 11 SINCE 5.0.0."

Current test dependency versions (pom.xml:71-77):
- `junit-jupiter-{engine,api,params}` 5.14.1 (`junit5.version`, pom.xml:71)
- `assertj-core` 3.27.6 (`assertj.version`, pom.xml:72)
- `spock-core` 2.4-M6-groovy-4.0 (`spock.version`, pom.xml:73)
- `jmh-generator-annprocess` 1.37 (`jmh.version`, pom.xml:74)
- `byte-buddy` 1.18.2 (`byte-buddy.version`, pom.xml:75)
- `easy-random-core` 5.0.0 (`easy-random.version`, pom.xml:76) — **FROZEN**
- `excel-streaming-reader` 5.1.2 (`excel-streaming-reader.version`, pom.xml:77)

- Add new test deps to parent `<dependencyManagement>` with `<scope>test</scope>`, then reference without `<version>` in child poms.
- Do NOT upgrade easy-random past 5.0.0.
- Do NOT use test-scoped classes (`EasyRandom`, Spock, AssertJ) in production code.

Do: declare version in parent `<dependencyManagement>`, reference by `artifactId` only in child pom.
Don't: set `easy-random.version` to anything other than `5.0.0`, import `org.assertj.*` from `src/main/java/`.
