<p align="center">
    <img src="./asset/javaxcel-core-logo.png" alt="Javaxcel Core" width="20%">
</p>

<h1 align="center">Javaxcel</h1>

<p align="center">Supporter for export and import of excel file</p>

<p align="center">
    <a href="https://github.com/javaxcel/javaxcel/actions/workflows/maven-build.yml">
        <img alt="GitHub Workflow Status (branch)" src="https://img.shields.io/github/actions/workflow/status/javaxcel/javaxcel/maven-build.yml?branch=release&style=flat-square">
    </a>
    <a href="https://codecov.io/gh/javaxcel/javaxcel">
        <img alt="Codecov branch" src="https://img.shields.io/codecov/c/github/javaxcel/javaxcel/release?label=code%20coverage&style=flat-square&token=X7ZO535W9K"/>
    </a>
    <a href="https://search.maven.org/artifact/com.github.javaxcel/javaxcel-core">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.github.javaxcel/javaxcel-core?style=flat-square">
    </a>
    <br/>
    <a href="https://lgtm.com/projects/g/javaxcel/javaxcel/context:java">
        <img alt="Lgtm grade" src="https://img.shields.io/lgtm/grade/java/github/javaxcel/javaxcel.svg?logo=&logoWidth=&label=lgtm%3A%20code%20quality&&style=flat-square"/>
    </a>
    <a href="https://www.codacy.com/gh/javaxcel/javaxcel/dashboard">
        <img alt="Codacy grade" src="https://img.shields.io/codacy/grade/6895ee87f26f491182e361d59e6f40b8?label=codacy%3A%20code%20quality&style=flat-square">
    </a>
    <img alt="jdk8" src="https://img.shields.io/badge/jdk-8-orange?style=flat-square">
</p>

## Table of Contents

- [What is Javaxcel?](#what-is-javaxcel)
- [Modules](#modules)
- [Getting started](#getting-started)
    - [Maven](#maven)
    - [Gradle](#gradle)
    - [Usage](#usage)

<br><br>

# What is Javaxcel?

Javaxcel is a supporter for exporting `java.util.List` to spreadsheets and importing `java.util.List` from spreadsheets
using [Apache POI](https://github.com/apache/poi).

<br><br>

# Modules

- [javaxcel-core](./core/): Supporter for export and import of Excel file.
- [javaxcel-styler](./styler/): Configurer for decoration of `CellStyle` with simple usage.

<br><br>

# Getting started

### Maven

```xml
<dependency>
    <groupId>com.github.javaxcel</groupId>
    <artifactId>javaxcel-core</artifactId>
    <version>x.y.z</version>
</dependency>

<!-- Required dependency -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>a.b.c</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.github.javaxcel:javaxcel-core:x.y.z'

// Required dependency
implementation 'org.apache.poi:poi-ooxml:a.b.c'
```

### Usage

```java
// Creates an instance of Javaxcel.
Javaxcel javaxcel = Javaxcel.newInstance();

File src = new File("/data", "old-products.xls");
File dest = new File("/data", "new-products.xlsx");

try (InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);
        Workbook oldWorkbook = new HSSFWorkbook(in);
        Workbook newWorkbook = new SXSSFWorkbook()) {
    // Reads all the sheets and returns data as a list.
    List<Product> products = javaxcel.reader(oldWorkbook, Product.class).read();
    
    // Creates an Excel file and writes data to it.
    javaxcel.writer(newWorkbook, Product.class).write(out, products);
} catch (IOException e) {
    e.printStackTrace();
}
```

Use Apache POI with simple code.
