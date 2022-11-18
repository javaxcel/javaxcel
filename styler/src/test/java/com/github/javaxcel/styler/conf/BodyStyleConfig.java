package com.github.javaxcel.styler.conf;

import com.github.javaxcel.styler.config.Configurer;
import com.github.javaxcel.styler.ExcelStyleConfig;
import org.apache.poi.ss.usermodel.*;

public class BodyStyleConfig implements ExcelStyleConfig {

    @Override
    public void configure(Configurer configurer) {
        configurer.border()
                    .all(BorderStyle.THIN, IndexedColors.BLACK)
                    .and()
                .font()
                    .name("NanumGothic")
                    .size(10)
                    .color(IndexedColors.BLACK);
    }

}
