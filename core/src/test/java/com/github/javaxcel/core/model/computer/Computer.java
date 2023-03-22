package com.github.javaxcel.core.model.computer;

import java.math.BigInteger;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.javaxcel.core.TestUtils.ExcludeOnPercentage;
import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelModel;
import com.github.javaxcel.core.annotation.ExcelModelCreator;
import com.github.javaxcel.core.internal.style.DefaultBodyStyleConfig;
import com.github.javaxcel.core.internal.style.DefaultHeaderStyleConfig;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"cpu", "disk", "manufacturer", "price"})
@ExcelModel(explicit = true, headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
public class Computer {

    @ExcludeOnPercentage(0.1)
    @ExcelColumn(name = "CPU_CLOCK")
    private final BigInteger cpu;

    @ExcludeOnPercentage(0.15)
    private Double ram;

    @ExcludeOnPercentage(0.2)
    @ExcelColumn(name = "DISK_SIZE")
    private final Long disk;

    @ExcludeOnPercentage(0.7)
    private String inputDevice;

    @ExcludeOnPercentage(0.67)
    private String outputDevice;

    @ExcelColumn
    @ExcludeOnPercentage(0.25)
    private final String manufacturer;

    @ExcelColumn
    @ExcludeOnPercentage(0.5)
    private final int price;

    @ExcelModelCreator
    Computer(BigInteger cpu, String manufacturerName, Long diskSize, int cost) {
        this.cpu = cpu;
        this.disk = diskSize;
        this.manufacturer = manufacturerName;
        this.price = cost;
    }

}
