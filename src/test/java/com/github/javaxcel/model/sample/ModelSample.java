package com.github.javaxcel.model.sample;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Set;

@ToString
@EqualsAndHashCode
@ExcelModel(defaultValue = "(empty)")
public class ModelSample {

    private Long id;

    @ExcelColumn(defaultValue = "none")
    private String name;

    private BigDecimal price;

    @ExcelColumn(defaultValue = "[]")
    private Set<String> tags;

}
