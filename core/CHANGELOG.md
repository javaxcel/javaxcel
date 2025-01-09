# Table of Contents

- [v0.20.0](#v0200):
- [v0.9.2](#v092): 2023-03-22
- [v0.9.1](#v091): 2023-01-02
- [v0.9.0](#v090): 2022-12-29
- [v0.8.2](#v082): 2022-07-10
- [v0.8.1](#v081): 2022-04-19
- [v0.8.0](#v080): 2022-04-15
- [v0.7.5](#v075): 2021-11-06
- [v0.7.4](#v074): 2021-05-27
- [v0.7.3](#v073): 2021-05-27
- [v0.7.2](#v072): 2021-05-25
- [v0.7.1](#v071): 2021-05-25
- [v0.7.0](#v070): 2021-05-12
- [v0.6.0](#v060): 2021-04-11
- [v0.5.3](#v053): 2020-12-06
- [v0.5.2](#v052): 2020-12-03
- [v0.5.1](#v051): 2020-11-09
- [v0.5.0](#v050): 2020-11-06
- [v0.4.2](#v042): 2020-10-17
- [v0.4.1](#v041): 2020-10-11
- [v0.4.0](#v040): 2020-09-27
- [v0.3.1](#v031): 2020-09-13
- [v0.3.0](#v030): 2020-09-12
- [v0.2.1](#v021): 2020-09-07
- [v0.1.0](#v010): 2020-08-30

# v0.20.0

### Modification

- 🔥 Remove: abstract strategy classes `AbstractExcelWriteStrategy`, `AbstractExcelReadStrategy`
- 🚚 Move: methods `resolveFirst(Class, Object[])`, `resolveLast(Class, Object[])` in `FieldUtils` to `ObjectUtils`

### New features

- ✨ Add: utility `ObjectUtils`

### Dependencies

- ⬆️ Upgrade: intransitive dependency `spring-expression` from `5.3.25` to `5.3.26`
- ⬆️ Upgrade: test dependency `junit5` from `5.9.2` to `5.10.0`

# v0.9.2

### Modification

- ⚡️ Improve: performance of reading rows in `ExcelUtils`
- 🔧 Add: intellij formatter

### Troubleshooting

- 🐞 Fix: manual calculation of column width for option `AutoResizedColumns`

### Dependencies

- ⬆️ Upgrade: intransitive dependency `spring-expression` from `5.3.24` to `5.3.25`
- ⬆️ Upgrade: intransitive dependency `annotations` from `23.1.0` to `24.0.1`
- ⬆️ Upgrade: test dependency `junit5` from `5.9.1` to `5.9.2`
- ⬆️ Upgrade: test dependency `assertj-core` from `3.23.1` to `3.24.2`
- ⬆️ Upgrade: test dependency `byte-buddy` from `1.12.20` to `1.14.2`
- ⬆️ Upgrade: test dependency `lombok` from `1.18.24` to `1.18.26`
- ⬆️ Upgrade: build dependency `maven` from `3.6.3` to `3.9.1`
- ⬆️ Upgrade: build dependency `maven-wrapper` from `3.1.1` to `3.2.0`
- ⬆️ Upgrade: build dependency `maven-compiler-plugin` from `3.10.1` to `3.11.0`
- ⬆️ Upgrade: build dependency `maven-javadoc-plugin` from `3.4.1` to `3.5.0`

# v0.9.1

### Modification

- ♻️ Replace: internal abstract method createBody(ExcelWriteContext) with `getColumnCount()`, `createCellValue(T, int)`
- 🔨 Modify: type of `ExcelWriteContext.headerStyles` from `List<CellStyle>` to `CellStyle[]`
- 🔨 Modify: type of `ExcelWriteContext.bodyStyles` from `List<CellStyle>` to `CellStyle[]`
- 🔨 Modify: internal clean up on `ExcelWriter`

### Troubleshooting

- 🐞 Fix: wrong overwriting a cell style on `ModelWriter` due to ArrayIndexOutOfBoundsException

# v0.9.0

### Modification

- 🔥 Remove: useless packages `constant`, `model` — `ConversionType`, `ConverterType`, `Column`
- 🔥 Remove: unused exceptions `GettingFieldValueException`, `NoTargetedConstructorException`
  , `NotExistConverterException`
  , `SettingFieldValueException`
- ♻️ Refactor: package `converter`
- ♻️ Refactor: handlers for java time
- ♻️ Replace: default constructor of strategy `Filter` with `Filter(boolean)`
- 🚚 Move: package `util.resolver` to `in.resolver`
- 🚚 Move: packages of implementation `ExcelTypeHandler`
- 🚚 Rename: implementations of converter
- ⚡️ Improve: support for handling field `java.lang.Iterable` by converter
- ⚡️ Improve: performance of `BigIntegerTypeHandler` on reading
- 🔨 Modify: default policy of sheet name that increases suffix at 0 is changed to increase at 1.
- 🔨 Modify: type of parameter `convert(Map<String, Object>, Field)` in `ExcelReadConverter` is changed
  to `convert(Map<String, String>, Field)`
- 🔨 Modify:  generic return type of `Javaxcel.reader(Workbook)` is changed from `ExcelReader<Map<String, Object>>`
  to `ExcelReader<Map<String, String>>`
- 🔨 Modify: method `newInstance(ExcelTypeHandlerRegistry)` in `Javaxcel` is changed not to
  use `DefaultExcelTypeHandlerRegistry`.
- 🔨 Modify: policy of converter `ExcelWriteHandlerConverter`

### New features

- 📦️ Add: package `analysis`, `util.processor`
- ✨ Add: method `add(ExcelTypeHandler)` in `ExcelTypeHandlerRegistry`
- ✨ Add: strategies `UseGetters`, `UseSetters`
- ✨ Add: methods `resolveGetter(Field)`, `resolveSetter(Field)` in `FieldUtils`
- ✨ Add: type handlers for `Year`, `YearMonth`, `Month`, `MonthDay`, `Instant`

### Troubleshooting

- 🐞 Fix: resolution of method as a model creator due to already annotated one on super class

### Dependencies

- ➖ Remove: dependency `jsr305`
- ➕ Add: intransitive dependency `annotations`
- ➕ Add: test dependency `jmh-generator-annprocess`
- ⬆️ Upgrade: intransitive dependency `spring-expression` from `5.3.18` to `5.3.24`
- ⬆️ Upgrade: provided dependency `poi-ooxml` from `5.2.2` to `5.2.3`
- ⬆️ Upgrade: dependency `common-utils` from `0.9.0` to `0.14.0`
- ⬆️ Upgrade: test dependency `junit5` from `5.8.2` to `5.9.1`
- ⬆️ Upgrade: test dependency `assertj-core` from `3.22.0` to `3.23.1`
- ⬆️ Upgrade: test dependency `spock-core` from `2.1-groovy-3.0` to `2.3-groovy-4.0`
- ⬆️ Upgrade: test dependency `byte-buddy` from `1.12.9` to `1.12.20`
- ⬆️ Upgrade: test dependency `excel-streaming-reader` from `3.6.1` to `4.0.5`
- ⬆️ Upgrade: build dependency `gmavenplus-plugin` from `1.13.1` to `2.1.0`
- ⬆️ Upgrade: build dependency `maven-jar-plugin` from `3.2.0` to `3.3.0`
- ⬆️ Upgrade: build dependency `maven-javadoc-plugin` from `3.2.0` to `3.4.1`
- ⬆️ Upgrade: build dependency `maven-shade-plugin` from `3.3.0` to `3.4.1`

# v0.8.2

### Modification

- 🔥 Remove: useless type variable in `ExcelWriteConverter`
- 🔥 Remove: method `toMap(Object)` in `FieldUtils`
- 🚚 Move: package of classes from `com.github.javaxcel.core.out.strategy.ExcelWriteStrategy.*`
  to `com.github.javaxcel.core.out.strategy.impl.*`
- 🚚 Move: package of classes from `com.github.javaxcel.core.in.strategy.ExcelWriteStrategy.*`
  to `com.github.javaxcel.core.in.strategy.impl.*`
- ♻️ Increase: access privileges to `AbstractExcelWriteStrategy`, `AbstractExcelReadStrategy`
- ⚡️ Modify: `com.github.javaxcel.core.out.strategy.impl.Filter` to freeze header

### Troubleshooting

- 🐞 Fix: wrong import in `ModelWriter`, `MapWriter` — from `java.util.logging.Filter`
  to `com.github.javaxcel.core.out.strategy.impl.Filter`
- 🐞 Fix: compile error on jdk 9+ by using internal package `sun.util.locale.*` in `LocaleTypeHandler`

### Dependencies

- ⬆️ Upgrade: dependency `common-utils` from `0.8.0` to `0.9.0`

# v0.8.1

### Dependencies

- ⬆️ Upgrade: dependency `javaxcel-styler` from `0.2.1` to `0.2.2` --- nested dependency `poi-ooxml` provided
- ⬆️ Upgrade: test dependency `lombok` from `1.18.22` to `1.18.24`

# v0.8.0

### Modification

- 🚚 Move: package of classes from `com.github.javaxcel.out` to `com.github.javaxcel.out.core`
- 🚚 Move: subclasses `ExcelWriter` from `com.github.javaxcel.out` to `com.github.javaxcel.out.core.impl`
- 🚚 Rename: method `setDefaultValue(String)` to `setAllDefaultValues(String)` in `ExcelWriteConverterSupport`
- 🚚 Rename: annotation `@ExcelWriterExpression` to `@ExcelWriteExpression`
- 🚚 Rename: annotation `@ExcelReaderExpression` to `@ExcelReadExpression`
- ♻️ Refactor: implementations of `ExcelWriter`
- ♻️ Refactor: input/output conversion
- ♻️ Refactor: input/output core module
- ♻️ Replace: `ExcelWriterFactory`, `ExcelReaderFactory` with `Javaxcel`
- ⚡️ Resolve: generic type of `MapWriter`
- ⚡️ Support: non-default constructor, static factory method to instantiate model
- 🔥 Remove: exception `NoTargetedConstructorException`
- 🔥 Remove: methods `getSheetRange(Workbook)`, `getNumOfRows(File)` in `ExcelUtils`

### New features

- ✨ Add: entrypoint class `Javaxcel`
- ✨ Add: domain model `Column`
- ✨ Add: annotations `@ExcelModelCreator`, `@ExcelModelCreator.FieldName`
- ✨ Add: modules `AbstractExcelModelExecutableResolver`, `ExcelModelConstructorResolver`, `ExcelModelMethodResolver`
- ✨ Add: modules `ExcelTypeHandler`, `ExcelTypeHandlerRegistry`
- ✨ Add: methods `toMap(T)`, `toHeaderName(Field)`, `resolveFirst(Class, Object...)`, `resolveLast(Class, Object...)`
  in `FieldUtils`
- 🔊 Add: fluent exception messages

### Troubleshooting

- 🐞 Fix: wrong computation of targeted fields
- 🐞 Fix: security problem for changing value of final field

### Dependencies

- ♻️ Make: dependency `poi-ooxml` provided
- ➕ Add: dependency `spring-expression` (shaded in `com.github.javaxcel.core.internal.springframework`)
- ➕ Add: test dependency `spock-core`
- ➕ Add: test dependency `excel-streaming-reader`
- ➕ Add: build dependency `maven-shade-plugin`
- ➖ Remove: dependency `spel`
- ➖ Remove: dependency `poi-scratchpad`
- ⬆️ Upgrade: dependency `common-utils` from `0.7.0` to `0.8.0`
- ⬆️ Upgrade: test dependency `junit5` from `5.8.1` to `5.8.2`
- ⬆️ Upgrade: test dependency `assertj-core` from `3.21.0` to `3.22.0`
- ⬆️ Upgrade: test dependency `lombok` from `1.18.20` to `1.18.22`
- ⬆️ Upgrade: test dependency `byte-buddy` from `1.11.1` to `1.12.9`
- ⬆️ Upgrade: build dependency `maven-gpg-plugin` from `1.6` to `3.0.1`
- ⬆️ Upgrade: build dependency `gmavenplus-plugin` from `1.13.0` to `1.13.1`
- ⬆️ Upgrade: build dependency `maven-compiler-plugin` from `3.8.1` to `3.10.1`
- ⬆️ Upgrade: build dependency `jacoco-maven-plugin` from `0.8.7` to `0.8.8`

# v0.7.5

### Modification

- ♻️ Replace: internal utilities
- 👷 Replace: travis CI with github actions due to a negative credit balance (10000 credits of free plan)

### Dependencies

- ⬆️ Upgrade: dependency `common-utils` from `0.4.7` to `0.7.0`
- ⬆️ Upgrade: test dependency `junit5` `5.7.2` to `5.8.1`
- ⬆️ Upgrade: test dependency `assertj-core` from `3.19.0` to `3.21.0`
- ⬆️ Upgrade: test dependency `byte-buddy` from `1.10.22` to `1.11.22`
- ➖ Remove: useless build dependency `maven-dependency-plugin`

# v0.7.4

### Dependencies

- ⬆️ Upgrade: dependency `common-utils` --- `0.4.7`

# v0.7.3

### Dependencies

- ⬆️ Upgrade: dependency `common-utils` --- `0.4.6`

# v0.7.2

### Dependencies

- ⬆️ Upgrade: dependency `common-utils` --- `0.4.5`

# v0.7.1

### Modification

- 🚚 Move: `initialValueOf(Class)` from `DefaultInputConverter` to `FieldUtils`
- ⚡️ Change: fields to be non-null in `Expression*Converter`
- ⚡️ Decrease: branch complexity
- ⚡️ Support: `java.util.UUID` for `DefaultInputConverter`
- ♻️ Refactor: exceptions
- ♻️ Replace: converters in `ModelReader` with `InputConverterSupport`

### Dependencies

- ⬆️ Upgrade: dependency `common-utils` --- `0.4.4`
- ⬆️ Upgrade: test dependency `junit5` --- `5.7.2`
- ⬆️ Upgrade: code coverage plugin `Jacoco` --- `0.8.7`

# v0.7.0

### Modification

- ♻️ **Change: static field to be excluded on target because of static field `$jacocoData`**
- ♻️ Change: cache key type(`java.lang.String` => `java.lang.reflect.Field`)
- ♻️ Replace: converters in `ModelWriter` with `OutputConverterSupport`
- ⚡️ Improve: performance by not creating useless `CellStyle`
- ⚡️ Improve: preemptive validation of sheet name
- ⚡️ Improve: performance of `ExcelUtils#getWorkbook(File)`
- ⚡️ Increase: visibility of constructors of `ExcelWriter`, `ExcelReader` implementation
- ⚡️ Separate: converters and processing of default value
- 🔥 Remove: `FieldUtils#convertIfFaulty(String, String, Field)`
- 🔥 Remove: useless `DefaultValueStore`
- 🔥 Discard: `java.lang.reflect.Field` from serializable fields
- 🚚 **Rename: class `*WritingConverter` => `*OutputConverter`, `*ReadingConverter` => `*InputConverter`**
- 🚚 **Rename: method `disableRolling()` => `unrotate()` in `AbstractExcelWriter`**
- 🚚 **Rename: method `autoResizeCols()` => `autoResizeColumns()` in `AbstractExcelWriter`**
- 🚚 **Rename: method `hideExtraCols()` => `hideExtraColumns()` in `AbstractExcelWriter`**
- 🚚 Move: defined methods in class `AbstractExcel*` to its interface

### New features

- ✨ Add: feature `ExcelWriter#filter()`
- ✨ Add: enum `ConversionType`, `ConverterType`
- ✨ Add: `OutputConverterSupport`
- ✨ Add: utilities `getNumOfDeclaredCellStyles(Workbook)`,
  `getNumOfInitialCellStyles(Workbook)`,
  `getDeclaredCellStyles(Workbook)`,
  `getFontFromCellStyle(Workbook, CellStyle)`,
  `equalsCellStyleAndFont(Workbook, CellStyle, Workbook, CellStyle)`,
  `equalsCellStyle(CellStyle, CellStyle)`,
  `equalsFont(Font, Font)` in `ExcelUtils`
- 🔧 Add: maven wrapper
- 🔧 Add: configuration for Travis CI

### Troubleshooting

- 🐞 Fix: mis-computation that `ExcelColumn#defaultValue()` doesn't override `ExcelModel#defaultValue()`
- 🐞 Fix: omission of conversion from empty value to default value on `MapWriter`

### Dependencies

- ➕ Add: build plugin `Jacoco`
- ➕ Add: test dependency `EasyRandom`
- ➕ Add: test dependency `Byte Buddy`
- ⬆️ Upgrade: test dependency `lombok` --- `1.18.20`

# v0.6.0

### Modification

- 🔥 Remove: `ExpressiveReadingConverter#convert(Map, Field, Expression)`
- 🔥 Remove: redundant constructor of `ExpressiveReadingConverter(List, boolean)`
- 🔥 Remove: duplicated code in each implementation of `AbstractExcelReader`
- ♻️ Reduce: visibility of `ExpressiveReadingConverter#createCache(List)`
- ♻️ Change: default header name in `MapReader` --- `null` => `column number`
- 🚧 Prevent: access to origin model by expression
- 🚧 Prevent: `AbstractExcelReader#readRow(Row)` from being overridden because it is default implementation
- ⚡️ Make: cache of expression unmodifiable
- 🚚 Rename: field `AbstractExcelReader#numOfRowsRead` => `AbstractExcelReader#numOfModelsRead`

### New features

- ✨ Add: constructors of `ExpressiveWritingConverter`
- ✨ Add: hook `beforeReadModels(Sheet)` in `AbstractExcelReader`
- ✨ Add: default implementation `AbstractExcelReader#readSheetAsMaps(Sheet)`
  , `AbstractExcelReader#getNumOfModels(Sheet)`
- ✨ Add: `headerStyle(ExcelStyleConfig)`, `bodyStyle(ExcelStyleConfig)` in `AbstractExcelWriter`, `ModelWriter`
  , `MapWriter`
- ✨ Add: `setRangeAlias(Workbook, String, String)`, `toRangeReference(Sheet, Cell, Cell)`
  , `toRangeReference(Sheet, int, int, int, int)`, `toColumnRangeReference(Sheet, int)`
  , `setValidation(Sheet, DataValidationHelper, String, String...)` in `ExcelUtils`
- ✨ Add: `ExcelModel#enumDropdown()`, `ExcelColumn#enumDropdown()`, `ExcelColumn#dropdownItems()`
  , `ModelWriter#enumDropdown()`

### Troubleshooting

- 🐞 Fix: `NullPointerException` caused by approaching a non-existent row; When an empty row is in body, computation of
  the number of models by `ExcelUtils#getNumOfModels(Sheet)` is failed.

### Dependencies

- ⬆️ Upgrade: dependencies for test
- ⬆️ Upgrade: dependency `common-utils` --- `0.3.4`

# v0.5.3

### Troubleshooting

- 🐞 Prevent: `NullPointerException` with `SXSSFWorkbook` in `ExcelUtils#autoResizeColumns(Sheet, int)`

# v0.5.2

### Modification

- 🚚 Rename: method in `TypeClassifier` --- `isPrimitiveAndNumeric` => `isNumericPrimitive`
- 🔥 Remove: methods in `ExcelWriter` --- `defaultValue(String)`, `sheetName(String)`, `headerNames(List)`
- 🔥 Remove: `ExcelReader#limit(int)`
- 🔥 Remove: validation in `AbstractExcelWriter#headerNames(List)`
- ♻️ Change: inner logic in `TypeClassifier` to be intuitive with `Types`

### New features

- ⚡️ Support: types of `ZonedDateTime`, `OffsetDateTime`, `OffsetTime` in `Basic*Converter`
- ⚡️ Support: `SXSSFWorkbook` to resize column width
- ✨ Add: methods in `ExcelUtils` --- `getWorkbook(File)`, `getNumOfRows(File)`, `getNumOfModels(File)`
- ✨ Add: `MapWriter#headerNames(List, List)`
- ✨ Add: enum `Types` for `TypeClassifier`
- 🔊 Provide: error message when `Excel*Factory#create`

### Troubleshooting

- 🐞 Fix: custom header names in wrong position --- `MapWriter#headerNames(List)`

### Dependencies

- ⬆️ Upgrade: dependency `common-utils` --- `0.3.3`

# v0.5.1

### Modification

- ⚡️ Prevent: `ExcelReader` from reading other sheets when number of rows read reach limit
- 🚚 Move: `*WritingConverter` to `com.github.javaxcel.core.converter.out`
- 🚚 Move: `*ReadingConverter` to `com.github.javaxcel.core.converter.in`
- 🔥 Remove: type parameter from `*ReadingConverter`
- ♻️ Refactor: `*WritingConverter`
- ♻️ Refactor: `*ReadingConverter`

### New features

- ✨ Add: detaching suffix from sheet name with `AbstractExcelWriter#disableRolling()`

### Troubleshooting

- 🐞 Fix: slicing list with over index --- `AbstractExcelWriter#disableRolling()`

# v0.5.0

### Modification

- ♻️ Make: `ExcelWriter`, `ExcelReader` to be interface
- ♻️ Change: `ExcelModel#policy()` to `ExcelModel#includeSuper()`
- 🔥 Discard: `WritingConverter#convertIfDefault(String, String, Field)`
- 🔥 Remove: `ModelReader#init(Workbook, Class)`, `ModelReader#sheetIndexes(int...)`, `ModelReader#startRow(int)`
  , `ModelReader#endRow(int)`
- 🔥 Remove: useless `TargetedFieldPolicy`
- 🚚 Rename: `ExcelWriter` => `ModelWriter`, `ExcelReader` => `ModelReader`
- 🚚 Move: `ExcelUtils#instantiate(Class)` to `FieldUtils#instantiate(Class)`
- 🚚 Move: methods in `ExcelStyler` to `ExcelUtils`
- ⚡️ Use: `FieldUtils#convertIfFaulty`
- 🗑 Deprecate: `ExcelDateTimeFormat#timezone()`

### New features

- ✨ Add: `MapWriter`, `MapReader`
- ✨ Add: `ExcelModel#headerStyle()`, `ExcelModel#bodyStyle()`, `ExcelColumn#headerStyle()`, `ExcelColumn#bodyStyle()`
- ✨ Add: `ExcelWriterFactory`, `ExcelReaderFactory`
- ✨ Add: `AbstractExcelWriter#disableRolling()`
- ✨ Add: `ExcelReader#limit(int)`
- ✨ Add: `ExcelModel#defaultValue()`
- ✨ Add: `ExcelUtils#getMaxRows`, `ExcelUtils#getMaxColumns`, `ExcelUtils#getNumOfRows`

### Troubleshooting

- 🐞 Fix: possibility that header name can be empty string
- 🐞 Fix: possibility for allowing `ExcelReader` to access row that doesn't exist

### Dependencies

- ➕ Use: dependency `common-utils` --- `0.3.2`
- ⬆️ Upgrade: dependency `javaxcel-styler` --- `0.2.1`

# v0.4.2

### Modification

- ⚡️ Improve: performance by pre-parsed expression as cache

### New features

- ✨ Add: utilities `StringUtils`
- ✨ Add: utilities to `ExcelUtils`
- ✨ Add: exception case --- when read Excel file with `SXSSFWorkbook`

### Dependencies

- ➖ Reduce: dependency scope `common-utils` to test
- ➖ Remove: dependency 'junit-platform-launcher'

# v0.4.1

### Modification

- 🚚 Rename: `Excel*Conversion` => `Excel*Expression`
- 🚚 Rename: `ExcelColumn#value()` => `ExcelColumn#name()`

### New features

- ⚡️ Add: validation to `ExcelReader`

### Dependencies

- ♻️ Replace: dependency `spring-expression` with `spel`
- ⬆️ Upgrade: dependency `common-utils` --- `0.3.1`
- ➕ Add: dependency `maven-surefire-plugin` for maven test

# v0.4.0

*This release is deprecated.*

### Modification

- ♻️ Refactor: massive classes
- ♻️ Separate: utilities
- 🔥 Discard: messy utilities
- ⚡️ Prevent: `ExcelReader` from getting empty string

### New features

- ✨ Add: new features --- SpEL expression
- ✨ Add: converters
- ✨ Add: `ExcelReader#parallel()`

### Troubleshooting

- 🐞 Fix: bugs --- creating empty cell(not null) even through not define default value, so that excel recognize that the
  cell exists.

### Dependencies

- ➕ Add: dependency `common-utils` --- `0.1.2`
- ➕ Add: dependency `spring-expression`

# v0.3.1

### Modification

- ⚡️ Improve: `ExcelReader` to read not only cell values but formula
- ♻️ Change: `ExcelWriter#headerStyle(BiFunction)` and `ExcelWriter#columnStyles(BiFunction)` to provide the font, not
  the workbook

### New features

- ✨ Add: validating sheet name when it is empty string

# v0.3.0

### Modification

♻️ Change: checked exception into unchecked exception

### New features

✨ Add: unchecked exceptions

# v0.2.1

### Modification

- 🚚 Rename: artifact id from `javaxcel` to `javaxcel-core`
- ♻️ Change: receiving dependencies of Workbook, OutputStream => separation of duties
- ⚡️ Update: `ExcelReader`

### New features

- 🛠️ Add: excel utilities
- ✨ Add: custom style in `ExcelWriter`

### Troubleshooting

- 🐞 Fix: parsing string as long to convert it into big decimal

### Dependencies

➖ Remove: dependency 'org.jetbrains:annotations'

# v0.1.0

#### New features

- 🎉 Begin: first release
