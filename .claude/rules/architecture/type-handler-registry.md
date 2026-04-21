---
name: type-handler-registry
description: Type conversion uses ExcelTypeHandler implementations registered in one of two registry types; follow the registry selection and handler placement rules.
globs: ["core/src/main/java/**/converter/handler/**"]
alwaysApply: false
---

RULE: Custom type conversion requires an `ExcelTypeHandler<T>` registered with a registry passed to `Javaxcel.newInstance(registry)`. Do not modify built-in handlers under `converter/handler/impl/**`.

WHY: `Javaxcel.newInstance(ExcelTypeHandlerRegistry)` (Javaxcel.java:55-60) makes a defensive copy into a `StrictExcelTypeHandlerRegistry`; the caller's registry is not mutated. The two registry types have distinct purposes.

**Two registries:**
- `DefaultExcelTypeHandlerRegistry` — pre-populated with JDK type handlers; used by `Javaxcel.newInstance()` (no-arg).
- `StrictExcelTypeHandlerRegistry` — validation-heavy; used as the defensive-copy target inside `Javaxcel.newInstance(registry)`.

**Built-in handler packages (mirror JDK structure — do not edit):**
- `converter/handler/impl/io/`, `impl/lang/`, `impl/math/`, `impl/net/`, `impl/nio/`, `impl/time/`, `impl/util/`

**Adding a custom type handler:**
1. Implement `ExcelTypeHandler<T>`, or extend `AbstractExcelTypeHandler<T>` for the boilerplate (`type` field + null-check via `Asserts` at lines 41-44).
2. Register via `registry.add(MyTypeHandler.class)` or `registry.add(new MyTypeHandler())`.
3. Pass the registry to `Javaxcel.newInstance(registry)`.

**Rules:**
- Do not add custom handlers to `converter/handler/impl/**` — those mirror JDK packages and changes will conflict with library upgrades.
- Do not call `new DefaultExcelTypeHandlerRegistry()` or `new StrictExcelTypeHandlerRegistry()` from application code; obtain a registry through `Javaxcel.newInstance()` or construct one separately for customization.
- `AbstractExcelTypeHandler` uses `Asserts` (not `Objects.requireNonNull`) for the null-check in its constructor — follow the same pattern in custom handlers.

**Do:**
```java
ExcelTypeHandlerRegistry registry = new DefaultExcelTypeHandlerRegistry();
registry.add(new MyCustomHandler());
Javaxcel javaxcel = Javaxcel.newInstance(registry);
```

**Don't:**
```java
// Don't edit built-in impls
// core/src/main/java/.../converter/handler/impl/time/LocalDateTypeHandler.java — leave alone
```
