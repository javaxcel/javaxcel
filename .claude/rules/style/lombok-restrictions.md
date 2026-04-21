---
name: lombok-restrictions
description: Experimental Lombok annotations are banned; stable annotations are allowed; Lombok must not leak to consumers.
globs: ["**/*.java"]
alwaysApply: false
---

RULE: Experimental Lombok annotations are compile errors; use only stable Lombok APIs. Lombok is declared `provided`+`optional` so it does not leak to library consumers.

WHY: `lombok.config` (project root, line 2) sets `lombok.experimental.flagUsage = ERROR`. The root `pom.xml` declares Lombok with `<scope>provided</scope>` and `<optional>true</optional>` (lines 168-169).

**Banned (experimental):**
- `@Accessors`, `@Wither`, `@FieldDefaults`, `@UtilityClass`, `@SuperBuilder` (experimental variant), `@Delegate` (experimental), and any annotation from `lombok.experimental.*`

**Allowed (stable):**
- `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@AllArgsConstructor`, `@NoArgsConstructor`
- `@Data`, `@Value`, `@EqualsAndHashCode`, `@ToString`
- `@SneakyThrows`, `@Slf4j`, `@NonNull`, `@Cleanup`

**Dependency rule:**
- Never change Lombok's scope from `provided` or remove `<optional>true</optional>` — doing so would expose it as a transitive runtime dependency to consumers of the library.
