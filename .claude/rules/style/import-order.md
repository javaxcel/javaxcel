---
name: import-order
description: Imports must follow the project-defined group order with blank-line separators and no wildcards.
globs: ["**/*.java", "**/*.groovy"]
alwaysApply: false
---

RULE: Imports must be ordered in strict groups separated by blank lines; wildcard imports are forbidden.

WHY: `intellij-code-style.xml` defines `IMPORT_LAYOUT_TABLE` for both Java (lines 35-54) and Groovy (lines 6-29), and sets `CLASS_COUNT_TO_USE_IMPORT_ON_DEMAND=99` / `NAMES_COUNT_TO_USE_IMPORT_ON_DEMAND=1` to suppress wildcards in both languages.

**Java import order:**
1. `java.*`
2. `javax.*`
3. `org.*`
4. `net.*`
5. `com.*` (third-party)
6. (other / unmatched)
7. `com.github.javaxcel.*`
8. `static` (all static imports)

**Groovy import order** (two extra groups at the top):
1. `groovy.*`
2. `spock.*`
3. `java.*`
4. `javax.*`
5. `org.*`
6. `net.*`
7. `com.*` (third-party)
8. (other / unmatched)
9. `com.github.javaxcel.*`
10. `static`

- Separate every group with exactly one blank line.
- No wildcard (`*`) imports — thresholds are set to 99/1 to prevent auto-collapse.
- Static imports go last as a single group, regardless of origin package.
