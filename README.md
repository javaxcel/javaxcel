<p align="center">
    <img  src="./src/main/resources/javaxcel-core-logo.png" alt="Javaxcel Core" width="20%">
</p>

<h1 align="center">Javaxcel Core</h1>

<p align="center">Supporter for export and import of excel file</p>

<p align="center">
    <!--
    <a href="https://travis-ci.com/github/javaxcel/javaxcel-core">
        <img alt="Travis CI" src="https://img.shields.io/travis/com/javaxcel/javaxcel-core/release?style=flat-square"/>
    </a>
    -->
    <a href="https://github.com/ImSejin/common-utils/actions/workflows/maven-build.yml">
    <img alt="Github Workflows" src="https://img.shields.io/github/workflow/status/javaxcel/javaxcel-core/Java%20CI%20with%20Maven?label=build&style=flat-square">
    </a>
    <a href="https://codecov.io/gh/javaxcel/javaxcel-core">
        <img alt="Codecov branch" src="https://img.shields.io/codecov/c/github/javaxcel/javaxcel-core/release?label=code%20coverage&style=flat-square&token=X7ZO535W9K"/>
    </a>
    <a href="https://search.maven.org/artifact/com.github.javaxcel/javaxcel-core">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.github.javaxcel/javaxcel-core?style=flat-square">
    </a>
    <br/>
    <a href="https://lgtm.com/projects/g/javaxcel/javaxcel-core/context:java">
        <img alt="Lgtm grade" src="https://img.shields.io/lgtm/grade/java/github/javaxcel/javaxcel-core.svg?logo=&logoWidth=&label=lgtm%3A%20code%20quality&&style=flat-square"/>
    </a>
    <a href="https://www.codacy.com/gh/javaxcel/javaxcel-core/dashboard">
        <img alt="Codacy grade" src="https://img.shields.io/codacy/grade/a430e1580c5f44d2b2f1b2c11195cc49?label=codacy%3A%20code%20quality&style=flat-square">
    </a>
    <img alt="jdk8" src="https://img.shields.io/badge/jdk-8-orange?style=flat-square">
</p>

## Table of Contents

- [What is Javaxcel Core?](#what-is-javaxcel-core)
- [Getting started](#getting-started)
- [Examples](#examples)
  1. [No option](#no-option)
  2. [Exclude field](#exclude-field)
  3. [Name the header](#name-the-header)
  4. [Set the default value](#set-the-default-value)
  5. [Model without the targeted fields](#model-without-the-targeted-fields)
  6. [Model that extends class](#model-that-extends-class)
  7. [Format date/time](#format-datetime)
  8. [Name a Sheet](#name-a-sheet)
  9. [Decoration](#decoration)
  10. [Expression](#expression)
  11. [Value constraint](#value-constraint)

<br><br>

# What is Javaxcel Core?

Javaxcel Core is a supporter for exporting list object to spread sheets and importing list object from spread sheets using [Apache POI](https://github.com/apache/poi).

<br><br>

# Getting started

### Maven

```xml
<dependency>
  <groupId>com.github.javaxcel</groupId>
  <artifactId>javaxcel-core</artifactId>
  <version>${javaxcel-core.version}</version>
</dependency>
```

### Gradle

```groovy
implementation group: "com.github.javaxcel", name: "javaxcel-core", version: "$javaxcelCoreVersion"
```

<br>

```java
File src = new File("/data", "old-products.xls");
File dest = new File("/data", "new-products.xlsx");

try (InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);
        Workbook oldWorkbook = new HSSFWorkbook(in);
        Workbook newWorkbook = new SXSSFWorkbook()) {
    // Reads all the sheet and returns data as a list.
    List<Product> products = ExcelReaderFactory.create(oldWorkbook, Product.class).read();
    
    // Creates a excel file and writes data to it.
    ExcelWriterFactory.create(newWorkbook, Product.class).write(out, products);
} catch (IOException e) {
    e.printStackTrace();
}
```

Code with simple usage.

<br><br>

# Examples

```java
class Product {
    private long serialNumber;
    private String name;
    private String accessId;
    private Double width;
    private double depth;
    private double height;
    private Double weight;
}

/* ... */

Product product = Product.builder()
    .serialNumber(10000)
    .name("Choco cereal")
    .accessId("2a60-4973-aec0-685e")
    .height(20.5)
    .weight(580.5)
    .build();
List<Product> products = Collections.singletonList(product);
```

There is a list that contains a `Product`.

<br><br>

## No option

### writer:

```java
File dest = new File("/data", "products.xlsx")
OutputStream out = new FileOutputStream(dest);
Workbook workbook = new SXSSFWorkbook();

ExcelWriterFactory.create(workbook, Product.class).write(out, products);
```
The result is

| serialNumber | name         | accessId            | width | depth | height | weight |
| ------------ | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal | 2a60-4973-aec0-685e |       | 0.0   | 20.5   | 580.5  |

The column order is the same as the declared field order.

If nothing is specified for the column, header name is the field name.

<br>

### reader:

```java
File src = new File("/data", "products.xlsx");
Workbook workbook = new XSSFWorkbook(src);

List<Product> products = ExcelReaderFactory.create(workbook, Product.class).read();
```

The result is

```json
[
    {
        "serialNumber": 10000,
        "name": "Choco cereal",
        "apiId": "2a60-4973-aec0-685e",
        "width": null,
        "depth": 0.0,
        "height": 20.5,
        "weight": 580.5
    }
]
```

The column order, also when read, is the same as the declared field order.

Model must has a constructor without parameters, so that `ExcelReader` can instantiate.

<br><br>

## Exclude field

```java
@ExcelIgnore
private String accessId;
```

### writer:

| serialNumber | name         | width | depth | height | weight |
| ------------ | ------------ | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal |       | 0.0   | 20.5   | 580.5  |

If you want to exclude several fields, annotate `@ExcelIgnore` to them.

<br>

### reader:

```json
[
    {
        "serialNumber": 10000,
        "name": "Choco cereal",
        "apiId": null,
        "width": null,
        "depth": 0.0,
        "height": 20.5,
        "weight": 580.5
    }
]
```

`ExcelReader` will pass the fields that annotated `@ExcelIgnore` by.

If column `apiId` exists and `Product#apiId` is still annotated `@ExcelIgnore`,

the exception will occur becauseof setting `apiId` to `width` (NumberFormatException).

<br><br>

## Name the header

```java
@ExcelColumn(name = "PRODUCT_NO")
private long serialNumber;

@ExcelColumn
private String name;
```

### writer:

| PRODUCT_NO | name         | accessId            | width | depth | height | weight |
| ---------- | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000      | Choco cereal | 2a60-4973-aec0-685e |       | 0.0   | 20.5   | 580.5  |

If you want to name the header, annotate `@ExcelColumn` and assign `name()` you want.

<br>

If you want to use header names only once or override `ExcelColumn#name()`, invoke `AbstractExcelWriter#headerNames(List)`.

```java
ExcelWriterFactory.create(workbook, Product.class)
    .headerNames(Arrays.asList("PRD_NO","NM","ACS_ID","WID","DEP","HEI","WEI")) // 7
//    .headerNames(Arrays.asList("PRD_NO","NM","ACS_ID","WID","DEP","HEI")) // 6: Occurs exception.
    .write(out, products);
```

The result is

| PRD_NO | NM           | ASC_ID              | WID  | DEP  | HEI  | WEI   |
| ------ | ------------ | ------------------- | ---- | ---- | ---- | ----- |
| 10000  | Choco cereal | 2a60-4973-aec0-685e |      | 0.0  | 20.5 | 580.5 |

If the number of arguments is not equal to the number of targeted fields, `ExcelWriter` throws exception.

<br>

### reader:

Not affected.

<br><br>

## Set the default value

```java
@ExcelColumn(name = "WIDTH", defaultValue = "0.0mm") // Default value is effective except primitive type.
private Double width;

@ExcelColumn(name = "Depth", defaultValue = "(empty)") // Default value is ineffective to primitive type.
private double depth;

@ExcelColumn(defaultValue = "0")
private double height;
```

### writer:

| serialNumber | name         | accessId            | WIDTH | Depth | height | weight |
| ------------ | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal | 2a60-4973-aec0-685e | 0.0mm | 0.0   | 20.5   | 580.5  |

It's ineffective to assign default value to primitive type, because the field of primitive type is always initialized.

<br>

If you want to use default value only once or override `ExcelColumn#defaultValue()`, invoke `AbstractExcelWriter#defaultValue(String)`.

```java
Product product = Product.builder().build(); // Not assigns to all fields.
List<Product> products = Collections.singletonList(product);

ExcelWriterFactory.create(workbook, Product.class)
    .defaultValue("(empty)")
    .write(out, products);
```

The result is

| serialNumber | name    | accessId | WIDTH   | Depth | height | weight  |
| ------------ | ------- | -------- | ------- | ----- | ------ | ------- |
| 0            | (empty) | (empty)  | (empty) | 0.0   | 0.0    | (empty) |

`AbstractExcelWriter#defaultValue(String)` will be applied to all fields.

<br>

### reader:

Not affected,

but if you set `ExcelColumn#defaultValue()` that doesn't match type of its field, the exception for type casting may occurred.

<br><br>

## Model without the targeted fields

```java
class NoFieldModel {}

class AllIgnoredModel {
    @ExcelIgnore
    private int number;
    
    @ExcelIgnore
    private Character character;
}
```

### writer:

```java
ExcelWriterFactory.create(workbook, NoFieldModel.class); // Occurs exception.
ExcelWriterFactory.create(workbook, AllIgnoredModel.class); // Occurs exception.
```

If you try to write with the class that has no targeted fields, `ExcelWriter` will throw exception.

<br>

### reader:

```java
List<NoFieldModel> noFieldModels = ExcelReaderFactory.create(workbook, NoFieldModel.class); // Occurs exception.
List<AllIgnoredModel> allIgnoredModels = ExcelReaderFactory.create(workbook, AllIgnoredModel.class); // Occurs exception.
```

If you try to write with the class that has no targeted fields, `ExcelReader` will throw exception.

<br><br>

## Model that extends class

```java
class EducationalProduct extends Product {
    private int[] targetAges;
    private String goals;
    private LocalDate date;
    private LocalTime time;
    private LocalDateTime dateTime;
}

/* ... */

EducationalProduct eduProduct = EducationalProduct.builder()
    .serialNumber(10001)
    .name("Mathematics puzzle toys for kids")
    .accessId("1a57-4055-a75b-98e4")
    .width(18.0)
    .depth(6)
    .height(20)
    .weight(340.07)
    .targetAges(4,5,6,7,8,9)
    .goals("Develop intelligence")
    .date(LocalDate.now())
    .time(LocalTime.now())
    .dateTime(LocalDateTime.now())
    .build();
List<EducationalProduct> eduProducts = Collections.singletonList(eduProduct);
```

There is a list that contains a `EducationalProduct`.

<br>

### writer:

```java
ExcelWriterFactory.create(workbook, EducationalProduct.class).write(out, list);
```

| targetAges  | goals                | date       | time         | dateTime                |
| ----------- | -------------------- | ---------- | ------------ | ----------------------- |
| [I@6a84a97d | Develop intelligence | 2020-09-13 | 11:54:26.176 | 2020-09-13T11:54:26.176 |

It writes the declared own fields, not including the inherited fields.

It's default.

<br>

```java
@ExcelModel(includeSuper = true)
class EducationalProduct extends Product { /* ... */ }
```

But if you annotate `@ExcelModel` and assign true into `includeSuper()`, it writes including the inherited fields.

The result is

| serialNumber | name                             | accessId            | width | depth | height | weight | targetAges  | goals                | date       | time         | dateTime                |
| ------------ | -------------------------------- | ------------------- | ----- | ----- | ------ | ------ | ----------- | -------------------- | ---------- | ------------ | ----------------------- |
| 10001        | Mathematics puzzle toys for kids | 1a57-4055-a75b-98e4 | 18.0  | 6.0   | 20.0   | 340.07 | [I@6a84a97d | Develop intelligence | 2020-09-13 | 11:54:26.176 | 2020-09-13T11:54:26.176 |

<br>

### reader:

```java
List<EducationalProduct> eduProducts = ExcelReaderFactory.create(workbook, EducationalProduct.class).read();
```

```js
[
    {
        "serialNumber": 10001,
        "name": "Mathematics puzzle toys for kids",
        "accessId": "1a57-4055-a75b-98e4",
        "width": 18.0,
        "depth": 6.0,
        "height": 20.0,
        "weight": 340.07,
        "targetAges": null, // Not supported type
        "goals": "Develop intelligence",
        "date": "2020-09-13",
        "time": "11:54:26.176",
        "dateTime": "2020-09-13T11:54:26.176"
    }    
]
```

Basically supported type

- primitive type, Wrapper(primitive) type
- `String`
- `BigInteger`, `BigDecimal`
- `LocalDate`, `LocalTime`, `LocalDateTime`

<br>

Others are not supported, so that the field value will be null.

<br><br>

## Format date/time

```java
@ExcelDateTimeFormat(pattern = "yyyyMMdd")
private LocalDate date = LocalDate.now();

@ExcelDateTimeFormat(pattern = "HH/mm/ss")
private LocalTime time = LocalTime.now();

@ExcelDateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
private LocalDateTime dateTime = LocalDateTime.now();
```

### writer:

| date     | time     | dateTime                |
| -------- | -------- | ----------------------- |
| 20200913 | 11/54/26 | 2020-09-13 11:54:26.176 |

If you want to write formatted `LocalDate`, `LocalTime` or `LocalDateTime`, annotate `@ExcelDateTimeFormat` and assign `pattern()` you want.

<br>

### reader:

```json
{
    "date": "2020-09-13",
    "time": "11:54:26.0",
    "dateTime": "2020-09-13T11:54:26.176"
}
```

`ExcelReader` parses `LocalDate`, `LocalTime` and `LocalDateTime` with `ExcelDateTimeFormat#pattern()`.

<br><br>

## Name a Sheet

### writer:

```java
ExcelWriterFactory.create(workbook, Product.class)
    .sheetName("Products")
    .write(out, products);
```

If you want to name a sheet, invoke `AbstractExcelWriter#sheetName(String)`.

If you don't, the name is `Sheet`.

<br>

### reader:

Not affected.

<br><br>

## Decoration

### writer:

```java
ExcelWriterFactory.create(workbook, Product.class)
    .autoResizeColumns() // Makes all columns fit content.
    .hideExtraRows() // Hides extra rows.
    .hideExtraColumns() // Hides extra columns.
    .headerStyle(new DefaultHeaderStyleConfig())
    .bodyStyle(new DefaultBodyStyleConfig())
    .write(out, products);
```

You can adjust all sheets with `AbstractExcelWriter#autoResizeColumns()`, `AbstractExcelWriter#hideExtraRows()` and `AbstractExcelWriter#hideExtraColumns()`.

<br>

You can decorate the header with `AbstractExcelWriter#headerStyle(ExcelStyleConfig)`

and also decorate the body with `AbstractExcelWriter#bodyStyles(ExcelStyleConfig)`.

<br>

If the number of arguments is not equal to 1 or the number of targeted fields, `ExcelWriter` throws exception.

When you input single argument, `ExcelWriter` applies it to all columns.

<br>

```java
@ExcelModel(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
class Product {

    @ExcelColumn(headerStyle = RedColumnStyleConfig.class)
    private long serialNumber;

    @ExcelColumn(bodyStyle = GrayColumnStyleConfig.class)
    private String name;

    /* ... */
}
```

You can also decorate the header and body with annotations.

Look [here](https://github.com/javaxcel/javaxcel-styler) for how to configure styles.

<br>

`ExcelColumn#headerStyle()` takes precedence over `ExcelModel#headerStyle()`.

`ExcelColumn#bodyStyle()` takes precedence over `ExcelModel#bodyStyle()`.

<br>

`serialNumber` will be applied with `RedColumnStyleConfig`. and `DefaultBodyStyleConfig`.

`name` will be applied with `DefaultHeaderStyleConfig` and `GrayColumnStyleConfig`.

<br>

### reader:

Not affected.

<br><br>

## Expression

### writer:

```java
class Product {
    @ExcelWriterExpression("T(io.github.imsejin.common.util.StringUtils).formatComma(#serialNumber)")
    private long serialNumber;

    @ExcelWriterExpression("#name.toUpperCase()")
    private String name;

    @ExcelWriterExpression("#accessId.replaceAll('\\d+', '')")
    private String accessId;

    @ExcelWriterExpression("T(String).valueOf(#width).replaceAll('\\.0+$', '')")
    private Double width;

    @ExcelWriterExpression("#depth + 'cm'")
    private double depth;

    private double height;

    @ExcelWriterExpression("T(Math).ceil(#weight)")
    private Double weight;
}

@ExcelModel(includeSuper = true)
class EducationalProduct extends Product {
    @ExcelWriterExpression("T(java.util.Arrays).stream(#targetAges)" +
            ".boxed()" +
            ".collect(T(java.util.stream.Collectors).toList())" +
            ".toString()" +
            ".replaceAll('[\\[\\]]', '')")
    private int[] targetAges;

    @ExcelWriterExpression("'none'") // Static value
    private String goals;

    @ExcelWriterExpression("T(java.time.LocalDateTime).of(#date, #time)") // Refers other field
    private LocalDate date;

    private LocalTime time;

    private LocalDateTime dateTime;    
}
```

| serialNumber | name                             | accessId | width | depth | height | weight | targetAges       | goals | date                    | time         | dateTime                |
| ------------ | -------------------------------- | -------- | ----- | ----- | ------ | ------ | ---------------- | ----- | ----------------------- | ------------ | ----------------------- |
| 10,001       | MATHEMATICS PUZZLE TOYS FOR KIDS | a--ab-e  | 18    | 6.0cm | 20.0   | 341.0  | 4, 5, 6, 7, 8, 9 | none  | 2020-09-13T11:54:26.176 | 11:54:26.176 | 2020-09-13T11:54:26.176 |

You can pre-process field value with `@ExcelWriterExpression` before set value into a cell.

The expression language is `SpEL(Spring Expression Language)`.

Look [here](https://github.com/spring-projects/spring-framework/tree/master/spring-expression) for more details about this.

<br>

If you want to refer field in a expression, write `#` and the `field name`. (e.g. #targetAges)

Also you can refer other field. We call this as `variable`.

<br>

Field you can refer is only targeted field.

It means you cannot refer the field that is annotated with `@ExcelIgnore`.

If type of expression result is not `String`, the converter will invoke `Object#toString()`.

<br>

### reader:

```java
class Product {
    @ExcelReaderExpression("T(Long).parseLong(#serialNumber.replace(',', ''))")
    private long serialNumber;

    @ExcelReaderExpression("#name.charAt(0) + #name.substring(1).toLowerCase()")
    private String name;

    @ExcelReaderExpression("#accessId.replaceAll('-', '0')")
    private String accessId;

    private Double width;

    @ExcelReaderExpression("#depth.replace('cm', '')") // This string will be parsed as double.
    private double depth;

    private double height;

    @ExcelReaderExpression("T(Double).parseDouble(#weight) - 0.93")
    private Double weight;
}

@ExcelModel(includeSuper = true)
class EducationalProduct extends Product {
    @ExcelReaderExpression("T(com.github.javaxcel.Converter).toIntArray(#targetAges.split(', ')") // Custom converter method
    private int[] targetAges;

    @ExcelReaderExpression("'Develop intelligence'") // Static value
    private String goals;

    @ExcelReaderExpression("T(java.time.LocalDate).parse(#date)")
    private LocalDate date;

    private LocalTime time;

    private LocalDateTime dateTime;
}

// com.github.javaxcel.Converter
public class Converter {
    public static int[] toIntArray(String[] strs) {
        return Arrays.stream(strs).mapToInt(Integer::parseInt).toArray();
    }    
}
```

```json
[
    {
        "serialNumber": 10001,
        "name": "Mathematics puzzle toys for kids",
        "accessId": "a00ab0e",
        "width": 18.0,
        "depth": 6.0,
        "height": 20.0,
        "weight": 340.07,
        "targetAges": [
            4, 5, 6, 7, 8, 9
        ],
        "goals": "Develop intelligence",
        "date": "2020-09-13",
        "time": "11:54:26.176",
        "dateTime": "2020-09-13T11:54:26.176"
    }    
]
```

You can support not basic supported type with `@ExcelReaderExpression`.

The type of `variable` is `String`. It is value in cell.

