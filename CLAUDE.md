# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test

Maven multi-module project (Java 17). Use the Maven wrapper — do not assume a system `mvn`.

```bash
./mvnw clean package                # Build + test all modules
./mvnw -pl core test                # Test only core module (-am to also build deps)
./mvnw -pl styler test
./mvnw test -Dtest=ClassName        # Run a single JUnit test
./mvnw test -Dtest=ClassName#method # Run a single test method
./mvnw test -Dtest=SomeSpec         # Run a single Spock spec (see naming note)
./mvnw deploy -DperformRelease=true # Activates `ossrh` profile: sources + javadoc + GPG signing
```

CI (GitHub Actions `maven-build.yml`) builds on JDK 17 and 21 across ubuntu/macos/windows. When changing build config, remember the matrix.

### Test naming (non-obvious)

Surefire is configured with `**/*Test.java` and `**/*Spec.java`. Spock tests are written in Groovy under `src/test/groovy/` but compiled via `gmavenplus-plugin` so the resulting class name must still match `*Spec` — hence the `.java` include pattern covers them. Pure JUnit tests live in `src/test/java/`.

## Module Layout

Two Maven modules with a one-way dependency: `core` depends on `styler`.

- **`styler/`** (`javaxcel-styler`): Standalone fluent-API wrapper around Apache POI `CellStyle`. `ExcelStyleConfig` + `Configurer` — no reader/writer concerns.
- **`core/`** (`javaxcel-core`): The reader/writer engine. Depends on `styler` and exposes the public `Javaxcel` facade.

The top-level `pom.xml` only defines dependency versions and shared build plugins; it is not the API surface.

## Public API Entry Point

`com.github.javaxcel.core.Javaxcel` is the single factory. `Javaxcel.newInstance()` → `.writer(workbook, Class)` / `.reader(workbook, Class)` — or the no-type overloads which return `Map`-based readers/writers. Everything else in `core/` is internal-but-public (reachable for advanced users) or truly internal (`internal/` package).

## Core Architecture

Reading and writing follow symmetrical pipelines under `core/in/` and `core/out/`:

```
annotation → analysis → context → lifecycle → processor/resolver → strategy
```

- **`annotation/`**: User-facing annotations that drive everything — `@ExcelModel`, `@ExcelColumn`, `@ExcelDateTimeFormat`, `@ExcelReadExpression`, `@ExcelWriteExpression`, `@ExcelModelCreator`, `@ExcelValidation`.
- **`internal/analysis/`**: Reflects over a model class once at startup and produces the field metadata that both converters and readers/writers share. If you change annotation semantics, the analyzer is where it lands.
- **`converter/handler/`**: Type conversion system. `ExcelTypeHandler` + `ExcelTypeHandlerRegistry`. `DefaultExcelTypeHandlerRegistry` is used by default; `StrictExcelTypeHandlerRegistry` is used when a custom registry is passed to `Javaxcel.newInstance(registry)` (defensive copy). To add a new custom type, implement `ExcelTypeHandler` and add it to a registry — do not modify handlers under `converter/handler/impl/` (they mirror JDK type packages: `lang/`, `math/`, `net/`, `nio/`, `time/`, `util/`, `io/`).
- **`in/core/` and `out/core/`**: The `ExcelReader` / `ExcelWriter` interfaces and their `Model*` / `Map*` impls. `AbstractExcelReader` / `AbstractExcelWriter` contain the shared lifecycle.
- **`in/strategy/impl/` and `out/strategy/impl/`**: Configurable behavior exposed via `.options(...)` on readers/writers — `SheetName`, `HeaderNames`, `HeaderStyles`, `BodyStyles`, `Filter`, `AutoResizedColumns`, `DefaultValue`, `EnumDropdown`, `HiddenExtraColumns`, `HiddenExtraRows`, `KeyNames`, `UseGetters`/`UseSetters`, `CloseResource`, `Parallel`, `Limit`. These are the extension points users compose; prefer adding a new `ExcelStrategy` over adding flags to existing classes.
- **`in/resolver/` and lifecycle**: Constructor/setter resolution for instantiating models — governed by `@ExcelModelCreator` and the `UseSetters` strategy. Models must be instantiable; see README for the resolution rules.
- **`validator/`**: `@ExcelValidation` pipeline; runs after conversion on read.

## Shaded `spring-expression` (critical)

`core/pom.xml` uses `maven-shade-plugin` to relocate `org.springframework.*` → `com.github.javaxcel.internal.springframework.*` and `org.apache.commons.logging.*` → `com.github.javaxcel.internal.springframework.jcl.*`, with `minimizeJar=true`. SpEL is used to evaluate `@ExcelReadExpression` / `@ExcelWriteExpression` and must not leak into the public API — users must never need to add `spring-expression` themselves, and must not experience version conflicts if they do. Consequences:

- Do **not** import `org.springframework.*` in any public-facing class signature. Keep SpEL types confined to `core/internal/` implementation classes.
- When adding a transitive Spring type, add it to the `<artifactSet>` `<includes>` in `core/pom.xml` or it will be stripped by `minimizeJar` (and fail at runtime only when the code path is exercised by tests).
- `spring-expression` is declared with `<optional>true</optional>` in the parent — it is shaded, not provided at runtime.

## Annotations & Model Contracts

- A model for reading must be instantiable. Resolution order: `@ExcelModelCreator`-annotated constructor/method → single public constructor → default constructor. See `in/resolver/impl/`.
- `@ExcelColumn(ignored = true)` skips a field for both read and write — but if the sheet still contains that column, subsequent columns shift and reads will misalign types (documented in `core/README.md`). Preserve this behavior; do not silently remap.
- Lombok is `provided`/`optional` and `lombok.config` sets `lombok.experimental.flagUsage = ERROR` — experimental Lombok features are banned project-wide.

## Conventions

- Code style: `intellij-code-style.xml` at repo root; copyright header in `intellij-copyright.txt` (applied to every source file — preserve it when editing).
- JetBrains annotations (`@Nullable`, `@VisibleForTesting`, etc.) are used throughout and are `provided` + `optional` — they disappear at runtime. Use them freely in new code.
- Releases are cut from the `release` branch; day-to-day development lands on `dev` (the default branch here). CodeQL runs on `dev` PRs.
