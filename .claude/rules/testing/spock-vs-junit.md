---
name: spock-vs-junit
description: Default to Spock for new tests; use JUnit only when Spock is awkward.
globs: ["core/src/test/**", "styler/src/test/**"]
alwaysApply: false
---

RULE: New tests go in Spock (`*Spec.groovy` under `src/test/groovy/`) unless Spock syntax is a poor fit.

WHY: The project is overwhelmingly Spock. As of current HEAD, core has 65 `*Spec.groovy` files vs 23 `*Test.java` files; styler has 6 Spock specs and 0 JUnit tests.

- Spock specs: `src/test/groovy/…/*Spec.groovy`, extend `spock.lang.Specification`.
- JUnit tests: `src/test/java/…/*Test.java`, annotated with JUnit 5.
- Mirror the production package path in both cases.
- Acceptable reasons to reach for JUnit: heavy `@ParameterizedTest` with CSV sources, JUnit platform extensions unavailable in Spock, specific JUnit lifecycle needs.
- Do NOT mix Spock and JUnit assertions in the same file.

Do: `core/src/test/groovy/com/github/javaxcel/core/JavaxcelSpec.groovy`
Don't: create `JavaxcelTest.java` for a subject that already has a Spock spec.
