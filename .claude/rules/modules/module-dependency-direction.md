---
name: module-dependency-direction
description: Enforce one-way dependency: core → styler only; styler must never depend on core.
globs: ["core/pom.xml", "styler/pom.xml", "styler/src/main/java/**"]
alwaysApply: false
---

RULE: `javaxcel-core` may depend on `javaxcel-styler`; the reverse is FORBIDDEN.

WHY: Styler is a standalone POI CellStyle builder usable without core. A styler→core dep would create a cycle that breaks the Maven reactor and destroys standalone usability.

- `core/pom.xml:22-26` declares `javaxcel-styler` as a compile dependency.
- `styler/pom.xml` has NO dependency on `javaxcel-core` — confirm before adding anything.
- Both modules inherit `${project.parent.version}` from the root POM; a circular dep still fails the Maven build.
- If core code needs a type that feels generic enough for styling, move it to styler — not the other way.
- If styler code needs something from core, refactor to remove the need; never add the dep.

DO: Add utilities that both modules need to `styler` (or extract a shared module).
DON'T: Add `javaxcel-core` as a dependency in `styler/pom.xml` under any scope.
