package com.github.javaxcel.core.model.sample;

import com.github.javaxcel.core.annotation.ExcelColumn;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@ToString
@EqualsAndHashCode
public class PlainSample {

    private Long id;

    @ExcelColumn(defaultValue = "0.00")
    private BigDecimal price;

    private Set<String> tags;

}
