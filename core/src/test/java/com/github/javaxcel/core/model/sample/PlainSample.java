package com.github.javaxcel.core.model.sample;

import java.math.BigDecimal;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import com.github.javaxcel.core.annotation.ExcelColumn;

@Getter
@ToString
@EqualsAndHashCode
public class PlainSample {

    private Long id;

    @ExcelColumn(defaultValue = "0.00")
    private BigDecimal price;

    private Set<String> tags;

}
