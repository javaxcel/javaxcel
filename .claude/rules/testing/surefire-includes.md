---
name: surefire-includes
description: Surefire only picks up *Test.java and *Spec.java — name test classes accordingly.
globs: ["core/src/test/**", "styler/src/test/**", "pom.xml"]
alwaysApply: false
---

RULE: Test class names MUST end in `Test` or `Spec`; no other suffixes. Groovy specs match `*Spec.java` (compiled class), not `*Spec.groovy`.

WHY: `pom.xml:384-387` configures Surefire `<includes>` as `**/*Test.java` and `**/*Spec.java`. gmavenplus (`pom.xml:355` `addTestSources`) compiles `.groovy` to `.class`; Surefire matches by compiled class name, not source file name.

- Renaming a test class to any other suffix (`Tests`, `IT`, `Check`, `Suite`) = silent skip in CI.
- Do not add a `*Spec.groovy` include pattern to Surefire — it does not work for compiled classes.
- Both Java and Groovy test sources are registered via `gmavenplus-plugin` `addTestSources` goal (`pom.xml:355`).
- The `<testFailureIgnore>false</testFailureIgnore>` (`pom.xml:377`) means any failing test breaks the build.

Do: `class JavaxcelSpec`, `class SomeFeatureTest`
Don't: `class JavaxcelIT`, `class SomeFeatureTests`, `class SomeFeatureCheck`
