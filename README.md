<p align="center">
    <img src="./asset/javaxcel-core-logo.png" alt="Javaxcel Core" width="20%">
</p>

<h1 align="center">Javaxcel</h1>

<p align="center">Supporter for export and import of excel file</p>

<p align="center">
    <a href="https://github.com/javaxcel/javaxcel/actions/workflows/maven-build.yml">
        <img alt="GitHub Workflow Status (branch)" src="https://img.shields.io/github/actions/workflow/status/javaxcel/javaxcel/maven-build.yml?branch=release&logo=github&style=flat-square&label=Build">
    </a>
    <a href="https://codecov.io/gh/javaxcel/javaxcel">
        <img alt="Codecov branch" src="https://img.shields.io/codecov/c/github/javaxcel/javaxcel/release?logo=codecov&style=flat-square&token=X7ZO535W9K&label=CodeCoverage"/>
    </a>
    <a href="https://central.sonatype.com/artifact/com.github.javaxcel/javaxcel">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.github.javaxcel/javaxcel?logo=apachemaven&style=flat-square&label=MavenCentral">
    </a>
    <br/>
    <a href="https://sonarcloud.io/summary/overall?id=javaxcel_javaxcel">
        <img alt="Sonarcloud Quality Gate Status" src="https://img.shields.io/sonar/quality_gate/javaxcel_javaxcel?server=https%3A%2F%2Fsonarcloud.io&style=flat-square&logo=sonarcloud&label=QualityGate"/>
    </a>
    <a href="https://sonarcloud.io/summary/overall?id=javaxcel_javaxcel">
        <img alt="Sonarcloud Maintainability Rating" src="https://img.shields.io/sonar/sqale_rating/javaxcel_javaxcel?server=https%3A%2F%2Fsonarcloud.io&style=flat-square&logo=sonarcloud&label=Maintainability"/>
    </a>
    <img alt="java17" src="https://img.shields.io/badge/Java-17-orange?style=flat-square">
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

Path src = Path.of("/data", "old-products.xls");
Path dest = Path.of("/data", "new-products.xlsx");

try (InputStream in = Files.newInputStream(src);
        OutputStream out = Files.newOutputStream(dest);
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
