# Table of Contents

- [v0.9.0](#v090):
- [v0.2.1](#v021): 2020-09-07
- [v0.1.0](#v010): 2020-08-30

# v0.9.0

### Modification

- 🔥 Remove: method `drawAll(CellStyle, BorderStyle)` in `Borders`
- 🔥 Remove: method `dyeAll(CellStyle, IndexedColors)` in `Borders`
- 🚚 Rename: method `horizontal(CellStyle, HorizontalAlignment)` to `setHorizontal(CellStyle, HorizontalAlignment)`
  in `Alignments`
- 🚚 Rename: method `vertical(CellStyle, VerticalAlignment)` to `setVertical(CellStyle, VerticalAlignment)`
  in `Alignments`
- 🚚 Rename: method `drawPattern(CellStyle, FillPatternType)` to `pattern(CellStyle, FillPatternType)` in `Backgrounds`
- 🚚 Rename: method `dye(CellStyle, IndexedColors)` to `color(CellStyle, IndexedColors)` in `Backgrounds`
- 🚚 Rename: method `drawTop(CellStyle, BorderStyle)` to `setTopStyle(CellStyle, BorderStyle)` in `Borders`
- 🚚 Rename: method `drawRight(CellStyle, BorderStyle)` to `setRightStyle(CellStyle, BorderStyle)` in `Borders`
- 🚚 Rename: method `drawBottom(CellStyle, BorderStyle)` to `setBottomStyle(CellStyle, BorderStyle)` in `Borders`
- 🚚 Rename: method `drawLeft(CellStyle, BorderStyle)` to `setLeftStyle(CellStyle, BorderStyle)` in `Borders`
- 🚚 Rename: method `dyeTop(CellStyle, IndexedColors)` to `setTopColor(CellStyle, IndexedColors)` in `Borders`
- 🚚 Rename: method `dyeRight(CellStyle, IndexedColors)` to `setRightColor(CellStyle, IndexedColors)` in `Borders`
- 🚚 Rename: method `dyeBottom(CellStyle, IndexedColors)` to `setBottomColor(CellStyle, IndexedColors)` in `Borders`
- 🚚 Rename: method `dyeLeft(CellStyle, IndexedColors)` to `setLeftColor(CellStyle, IndexedColors)` in `Borders`
- 🚚 Rename: method `name(CellStyle, Font, String)` to `setName(CellStyle, Font, String)` in `Fonts`
- 🚚 Rename: method `size(CellStyle, Font, int)` to `setSize(CellStyle, Font, int)` in `Fonts`
- 🚚 Rename: method `color(CellStyle, Font, IndexedColors)` to `setColor(CellStyle, Font, IndexedColors)` in `Fonts`
- 🚚 Rename: method `underline(CellStyle, Font, Underline)` to `setUnderline(CellStyle, Font, Underline)` in `Fonts`
- 🚚 Rename: method `offset(CellStyle, Font, Offset)` to `setOffset(CellStyle, Font, Offset)` in `Fonts`
- 🔨 Modify: return type of `getValue()` in `Underline` from `short` to `byte`

### New features

### Troubleshooting

- 🐞 Fix:

### Dependencies

- ➖ Remove: dependency `jsr305`
- ➕ Add: intransitive dependency `annotations`
- ⬆️ Upgrade: provided dependency `poi-ooxml` from `5.2.2` to `5.2.3`
- ⬆️ Upgrade: dependency `common-utils` from `0.9.0` to `0.13.0`
- ⬆️ Upgrade: test dependency `junit5` from `5.8.2` to `5.9.1`
- ⬆️ Upgrade: test dependency `assertj-core` from `3.22.0` to `3.23.1`
- ⬆️ Upgrade: test dependency `spock-core` from `2.1-groovy-3.0` to `2.3-groovy-4.0`
- ⬆️ Upgrade: test dependency `byte-buddy` from `1.12.9` to `1.12.18`
- ⬆️ Upgrade: test dependency `excel-streaming-reader` from `3.6.1` to `4.0.4`
- ⬆️ Upgrade: build dependency `gmavenplus-plugin` from `1.13.1` to `2.1.0`
- ⬆️ Upgrade: build dependency `maven-jar-plugin` from `3.2.0` to `3.3.0`
- ⬆️ Upgrade: build dependency `maven-javadoc-plugin` from `3.2.0` to `3.4.1`
- ⬆️ Upgrade: build dependency `maven-shade-plugin` from `3.3.0` to `3.4.1`

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
