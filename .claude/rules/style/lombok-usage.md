RULE: Prefer Lombok-generated boilerplate over hand-written getters, setters, equals/hashCode, toString, and constructors. Reserve hand-written accessors for cases where the method body needs custom logic (validation, transformation, defensive copy) or where the public method name does not match Lombok's bean-getter naming.

WHY: The project already standardizes on Lombok stable annotations (see `lombok-restrictions.md`). Hand-written accessors duplicate intent, drift from field declarations, and add noise to small data-holder classes. Existing examples follow this pattern: `ExcelAnalysisImpl.java:35-37` (`@Getter @ToString`), `Limit.java` (`@Getter` for the value), `BodyStyles.java` (constructor-only data holders).

**Default to Lombok for:**
- Bean-style getters: `@Getter` at field or class level.
- Bean-style setters on mutable fields: `@Setter`.
- Constructors when the body is just field assignment: `@RequiredArgsConstructor`, `@AllArgsConstructor`, `@NoArgsConstructor`.
- Equality and string forms on data holders: `@EqualsAndHashCode`, `@ToString`, `@Data`, `@Value`.

**Use field-level `@Getter`/`@Setter` when only some fields need accessors.** Class-level `@Getter` exposes every field, which is rarely correct.

**Use `AccessLevel` to narrow generated visibility:**
```java
@Getter(AccessLevel.PACKAGE)
private final ExcelReadContext<T> context;
```

**Stay hand-written when:**
- Public method name diverges from `getFoo()`/`setFoo()` (e.g., the `ColumnDescriptor` interface uses `name()`, `fieldKey()`, `validators()` — Lombok would produce the wrong names).
- The accessor needs validation, lazy initialization, defensive copy, conversion, or any non-trivial logic.
- The method has a non-trivial javadoc that must travel with the method (Lombok's javadoc copy from field is best-effort).

**Banned:** experimental Lombok annotations (see `lombok-restrictions.md`). `@Accessors`, `@FieldDefaults`, `@UtilityClass`, etc. are compile errors.

**Do:**
```java
@Getter
@ToString
public final class ExcelAnalysisImpl implements ExcelAnalysis {
    private final Field field;
    private int flags;
    private @Nullable ExcelTypeHandler<?> handler;
    ...
}

public final class FieldColumnDescriptor<T> implements ColumnDescriptor<T> {
    @Getter private final Field field;
    @Getter private final @Nullable ExcelAnalysis analysis;
    private final String name;  // exposed via interface method name(), not getName()
    ...
    @Override public String name() { return this.name; }
}
```

**Don't:**
```java
// Pointless boilerplate — use @Getter
public Field getField() {
    return this.field;
}

// Class-level @Getter when only one field needs exposure
@Getter
public class Foo {
    private final A a;          // accidentally exposed
    private final B internalB;  // accidentally exposed
    private final C c;          // the only one we wanted public
}
```

**Verification:**
- New value-holder classes should reach for `@Getter` / `@Value` / `@RequiredArgsConstructor` first; hand-written boilerplate is justified only by a body that does more than field assignment or a name that bean-getter naming cannot produce.
