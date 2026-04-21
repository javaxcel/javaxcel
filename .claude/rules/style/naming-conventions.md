---
name: naming-conventions
description: Naming conventions for abstract classes, default implementations, strategy impls, *Impl suffix, and test files.
globs: ["**/*.java", "**/*.groovy"]
alwaysApply: false
---

RULE: Follow the project's established naming patterns for class prefixes, suffixes, and package placement.

WHY: Consistent naming is demonstrated across the codebase; deviating creates discoverability issues.

**Abstract base classes — `Abstract*` prefix:**
- `AbstractExcelReader`, `AbstractExcelWriter`, `AbstractExcelTypeHandler`, `AbstractExcelAnalyzer`, `AbstractExcelModelExecutableResolver`
- Any new abstract base class must use the `Abstract*` prefix.

**Default implementations — `Default*` prefix:**
- Used when there is a primary, general-purpose implementation of an interface: `DefaultExcelTypeHandlerRegistry`, `DefaultValueInfoImpl`.

**Strategy implementations — plain descriptive names, no `*Strategy` suffix:**
- Strategy classes live in a sibling `impl/` package and are named by what they do, not by their interface: `ModelReader`, `MapReader`, `Parallel`, `BodyStyles`, `Limit`, `KeyNames`, `UseSetters`, `AutoResizedColumns`, `CloseResource`.
- Do **not** append `Strategy` to these class names even though they implement `ExcelReadStrategy` or `ExcelWriteStrategy`.

**`*Impl` suffix — only when distinguishing primary impl from interface:**
- Use `*Impl` when the class is the canonical, single implementation and its name would otherwise collide with the interface: `ExcelAnalysisImpl` (implements `ExcelAnalysis`), `DefaultValueInfoImpl` (implements `DefaultValueInfo`).
- Do not use `*Impl` as a generic suffix on all implementation classes.

**Test file naming:**
- Spock specifications: `*Spec.groovy`
- JUnit tests: `*Test.java`
