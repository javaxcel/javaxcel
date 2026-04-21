---
name: impl-package-convention
description: Place every interface implementation in an impl/ sub-package next to its interface, never alongside it.
globs: ["core/src/main/java/**/*.java"]
alwaysApply: false
---

RULE: An implementation of an interface lives in an `impl/` sub-package relative to that interface's package.

WHY: Keeps interface contracts discoverable at the package root and prevents polluting it with multiple concrete classes.

- `in/core/impl/ModelReader.java` and `in/core/impl/MapReader.java` implement `in/core/ExcelReader`.
- `out/core/impl/ModelWriter.java` and `out/core/impl/MapWriter.java` implement `out/core/ExcelWriter`.
- `out/strategy/impl/{AutoResizedColumns,BodyStyles,CloseResource,DefaultValue,EnumDropdown,Filter,HeaderNames,HeaderStyles,HiddenExtraColumns,HiddenExtraRows,KeyNames,SheetName,UseGetters}.java` implement `out/strategy/ExcelWriteStrategy`.
- `in/strategy/impl/{KeyNames,Limit,Parallel,UseSetters}.java` implement `in/strategy/ExcelReadStrategy`.
- `converter/handler/registry/impl/{DefaultExcelTypeHandlerRegistry,StrictExcelTypeHandlerRegistry}.java` implement `converter/handler/registry/ExcelTypeHandlerRegistry`.
- `converter/handler/impl/{lang,math,time,util,io,net,nio/file}/*TypeHandler.java` implement `converter/handler/ExcelTypeHandler`.
- `in/resolver/impl/{ExcelModelConstructorResolver,ExcelModelMethodResolver}.java` implement `in/resolver/AbstractExcelModelExecutableResolver`.

DO: Create `converter/handler/impl/security/` (mirroring JDK package names) when adding a new category of type handlers.
DON'T: Place a new `FooReader.java` directly in `in/core/` next to `ExcelReader.java`.
