package com.github.javaxcel.styler.conf;

import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.config.Configurer;
import org.apache.poi.ss.usermodel.*;

public class HeaderStyleConfig implements ExcelStyleConfig {

    @Override
    public void configure(Configurer configurer) {
        configurer.alignment()
                    .horizontal(HorizontalAlignment.CENTER)
                    .vertical(VerticalAlignment.CENTER)
                    .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.GREY_25_PERCENT)
                .border()
                    .leftAndRight(BorderStyle.THIN, IndexedColors.BLACK)
                    .bottom(BorderStyle.MEDIUM, IndexedColors.BLACK)
                    .and()
                .font()
                    .name("NanumGothic")
                    .size(12)
                    .color(IndexedColors.BLACK)
                    .bold();
    }

}
