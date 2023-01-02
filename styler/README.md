<p align="center">
    <img  src="../asset/javaxcel-styler-logo.png" alt="Javaxcel Styler" width="20%">
</p>

<h1 align="center">Javaxcel Styler</h1>

<p align="center">Configurer for decoration of cell style with simple usage</p>

<p align="center">
    <a href="https://search.maven.org/artifact/com.github.javaxcel/javaxcel-styler">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.github.javaxcel/javaxcel-styler?style=flat">
    </a>
    <img alt="jdk8" src="https://img.shields.io/badge/jdk-8-orange?style=flat">
</p>

# Getting started

### Maven

```xml
<dependency>
  <groupId>com.github.javaxcel</groupId>
  <artifactId>javaxcel-styler</artifactId>
  <version>${javaxcel-styler.version}</version>
</dependency>

<!-- Required dependency -->
<dependency>
  <groupId>org.apache.poi</groupId>
  <artifactId>poi-ooxml</artifactId>
  <version>${apache-poi.version}</version>
</dependency>
```

### Gradle

```groovy
implementation group: "com.github.javaxcel", name: "javaxcel-styler", version: "$javaxcelStylerVersion"

// Required dependency
implementation group: "org.apache.poi", name: "poi-ooxml", version: "$apachePoiVersion"
```

<br><br>

# Examples

```java
public class DefaultHeaderStyleConfig implements ExcelStyleConfig {
    @Override
    public void configure(Configurer configurer) {
        configurer
            	.alignment()
                    .horizontal(HorizontalAlignment.CENTER)
                    .vertical(VerticalAlignment.CENTER)
            	    .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.GREY_25_PERCENT)
                .border()
                    .leftAndRight(BorderStyle.THIN, IndexedColors.BLACK)
                    .bottom(BorderStyle.MEDIUM, IndexedColors.BLACK)
                    .and()
                .font()
                    .name("Arial")
                    .size(12)
                    .color(IndexedColors.BLACK)
                    .bold();
    }
}
```

```java
File file = new File("/data", "result.xlsx");

try (OutputStream out = Files.newOutputStream(file.toPath());
        Workbook workbook = new XSSFWorkbook()) {
    Cell cell;
    
    /* ... */
    
    CellStyle headerStyle = workbook.createCellStyle();
    Font font = workbook.createFont();
    
    // Applies custom style configuration to cell style.
    new DefaultHeaderStyleConfig().configure(new Configurer(headerStyle, font));
    cell.setCellStyle(headerStyle);
    
    /* ... */
    
    workbook.write(out);
} catch (IOException e) {
    e.printStackTrace();
}
```

You can configure custom style with implementation or lambda expression.

The implementation of `ExcelStyleConfig` must have default constructor.

