---
name: provided-scope-policy
description: Keep poi-ooxml, jetbrains-annotations, and lombok at scope=provided (and optional=true where applicable)
globs: ["**/pom.xml"]
alwaysApply: false
---

RULE: `poi-ooxml`, `annotations`, and `lombok` must stay `scope=provided`; `annotations` and `lombok` must also carry `optional=true`. Never promote any of them to `compile` scope.

WHY: Promoting breaks consumers who bring their own versions. POI's API is unstable across minor versions, so users must control which POI they depend on. Jetbrains annotations and Lombok are compile-time only — shipping them as compile deps silently leaks them into consumer classpaths.

Evidence (parent pom.xml):
- `poi-ooxml` → `scope=provided`, no `optional` (lines 135–140)
- `annotations` → `scope=provided`, `optional=true` (lines 155–161)
- `lombok` → `scope=provided`, `optional=true` (lines 163–170)

Child modules declare these without `<scope>` or `<optional>` — they inherit the declarations from parent `<dependencyManagement>`:
- core/pom.xml lines 40–44 (poi-ooxml), 47–50 (annotations), 86–88 (lombok)
- styler/pom.xml lines 28–31 (poi-ooxml), 34–37 (annotations), 65–67 (lombok)

**Do:**
```xml
<!-- parent dependencyManagement -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>${apache-poi.version}</version>
    <scope>provided</scope>
</dependency>
```

**Don't:**
```xml
<!-- Never in any child pom -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <scope>compile</scope> <!-- WRONG -->
</dependency>
```
