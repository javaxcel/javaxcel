package com.github.javaxcel.styler.model;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Product {

    @ExcelColumn(name = "상품번호")
    private long serialNumber;

    private String name;

    @ExcelColumn(name = "API_ID")
    private String apiId;

    @ExcelColumn(dropdownItems = {"Y", "N"})
    @ExcelWriteExpression("#imported?.getValue()")
    private YesOrNo imported;

    @ExcelColumn(name = "가로")
    private Double width;

    @ExcelColumn(defaultValue = "(empty)") // Default value is ineffective to primitive type.
    private double depth;

    private double height;

    @ExcelColumn(name = "WEIGHT", defaultValue = "0") // Default value is effective except primitive type.
    private Double weight;

}
