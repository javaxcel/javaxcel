package com.github.javaxcel.core.internal.style;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.config.Configurer;

public class DefaultBodyStyleConfig implements ExcelStyleConfig {

    @Override
    public void configure(Configurer configurer) {
        configurer.border()
                .all(BorderStyle.THIN, IndexedColors.BLACK);
        configurer.font()
                .name("Arial")
                .size(10);
    }

}
