package com.github.javaxcel.styler.conf;

import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.config.Configurer;
import com.github.javaxcel.styler.role.Fonts;
import org.apache.poi.ss.usermodel.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CustomExcelStyleConfig implements ExcelStyleConfig {

    public static List<ExcelStyleConfig> getRainbowHeader() {
        ExcelStyleConfig r = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.RED)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold();
        ExcelStyleConfig a = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.ORANGE)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold();
        ExcelStyleConfig i = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.GOLD)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold();
        ExcelStyleConfig n = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.GREEN)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold().color(IndexedColors.WHITE);
        ExcelStyleConfig b = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.BLUE)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold().color(IndexedColors.WHITE);
        ExcelStyleConfig o = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.INDIGO)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold().color(IndexedColors.WHITE);
        ExcelStyleConfig w = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.VIOLET)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold().color(IndexedColors.WHITE);
        ExcelStyleConfig s = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.BLACK)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold().color(IndexedColors.WHITE);

        return Collections.unmodifiableList(Arrays.asList(r, a, i, n, b, o, w, s));
    }

    @Override
    public void configure(Configurer configurer) {
        configurer.alignment()
                    .horizontal(HorizontalAlignment.CENTER)
                    .vertical(VerticalAlignment.CENTER)
                    .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.GREY_25_PERCENT)
                .border()
                    .top(BorderStyle.THIN, IndexedColors.BLACK)
                    .right(BorderStyle.THIN, IndexedColors.BLACK)
                    .bottom(BorderStyle.THIN, IndexedColors.BLACK)
                    .left(BorderStyle.THIN, IndexedColors.BLACK)
                    .topAndBottom(BorderStyle.THIN, IndexedColors.BLACK)
                    .leftAndRight(BorderStyle.THIN, IndexedColors.BLACK)
                    .all(BorderStyle.THIN, IndexedColors.BLACK)
                    .and()
                .font()
                    .name("NanumGothic")
                    .size(12)
                    .color(IndexedColors.BLACK)
                    .bold()
                    .italic()
                    .strikeout()
                    .underline(Fonts.Underline.DOUBLE_ACCOUNTING)
                    .offset(Fonts.Offset.SUPER);
    }

}
