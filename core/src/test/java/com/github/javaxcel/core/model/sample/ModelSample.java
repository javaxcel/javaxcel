package com.github.javaxcel.core.model.sample;

import java.math.BigInteger;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelModel;

@ToString
@EqualsAndHashCode
@ExcelModel(defaultValue = "(empty)")
public class ModelSample {

    private Integer identifier;

    @ExcelColumn(defaultValue = "none")
    private String title;

    private BigInteger serialNumber;

    @ExcelColumn(defaultValue = "[]")
    private Set<String> tags;

}
