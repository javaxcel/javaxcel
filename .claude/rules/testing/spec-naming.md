---
name: spec-naming
description: Spock spec class and method naming conventions.
globs: ["**/*Spec.groovy"]
alwaysApply: false
---

RULE: Spec class name = subject class name + `Spec`; annotate with `@Subject`; use plain-English `def "…"()` method names.

WHY: Consistent discovery and navigation. Evidence: `JavaxcelSpec.groovy:26-27` — `@Subject(Javaxcel)` / `class JavaxcelSpec extends Specification`.

- Class name: `<SubjectClass>Spec` (e.g., `AbstractExcelReaderSpec` tests `AbstractExcelReader`).
- Class-level annotation: `@Subject(TargetClass)` from `spock.lang.Subject` (pom.xml:237).
- Method names: `def "sentence describing behavior"()` — plain English, not camelCase.
  - Evidence: `JavaxcelSpec.groovy:29` — `def "Creates an instance"()`.
  - Evidence: `JavaxcelSpec.groovy:40` — `def "Creates an instance with registry of handlers"()`.
- The spec's package MUST match the production package of the subject.
- Spec extends `spock.lang.Specification` (imported at `JavaxcelSpec.groovy:19`).

Do: `@Subject(Javaxcel)` / `class JavaxcelSpec extends Specification` / `def "Creates an instance"()`
Don't: `class JavaxcelTests`, camelCase method names like `def createInstance()`, missing `@Subject`.
