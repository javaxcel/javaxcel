package com.github.javaxcel.core.model.creature;

import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelReadExpression;
import com.github.javaxcel.core.annotation.ExcelWriteExpression;
import com.github.javaxcel.core.internal.style.DefaultBodyStyleConfig;
import com.github.javaxcel.core.internal.style.DefaultHeaderStyleConfig;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public abstract class Creature {

    @ExcelColumn(name = "Kingdom", enumDropdown = true, dropdownItems = {"archaea", "bacteria", "protista", "animalia", "fungi", "plantae"})
    @ExcelWriteExpression("#kingdom.toString().toLowerCase()")
    @ExcelReadExpression("T(com.github.javaxcel.core.model.creature.Kingdom).valueOf(#kingdom.toUpperCase())")
    private Kingdom kingdom;

    @ExcelColumn(name = "Sex", headerStyle = DefaultBodyStyleConfig.class, bodyStyle = DefaultHeaderStyleConfig.class)
    @ExcelWriteExpression("#kingdom.toString() + #sex.toString().replaceAll('(.+)', '/$1/')")
    @ExcelReadExpression("T(com.github.javaxcel.core.model.creature.Sex).valueOf(#sex.replaceAll(#kingdom.toUpperCase() + '|/', ''))")
    private Sex sex;

    @ExcelColumn(name = "Lifespan")
    @ExcelWriteExpression("#lifespan + (#lifespan > 1 ? ' years' : ' year')")
    @ExcelReadExpression("#lifespan.replaceAll('(\\d+).+', '$1')")
    private int lifespan;

}
