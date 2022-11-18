package com.github.javaxcel.styler;

import com.github.javaxcel.Javaxcel;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy.*;
import com.github.javaxcel.styler.conf.BodyStyleConfig;
import com.github.javaxcel.styler.conf.CustomExcelStyleConfig;
import com.github.javaxcel.styler.model.Product;
import com.github.javaxcel.styler.model.factory.MockFactory;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import io.github.imsejin.common.util.DateTimeUtils;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExcelStylerTest {

    private Stopwatch stopwatch;

    @BeforeEach
    void beforeEach() {
        this.stopwatch = new Stopwatch(TimeUnit.SECONDS);
        stopwatch.start();
    }

    @AfterEach
    void afterEach() {
        stopwatch.stop();
        System.out.println(stopwatch.getStatistics());
    }

    @Test
    @Order(0)
    @DisplayName("autoResizeColumns")
    @SneakyThrows
    void autoResizeColumns(@TempDir Path path) {
        // given
        File file = new File(path.toFile(), DateTimeUtils.now() + ".xls");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();

        // when
        List<Product> products = MockFactory.generateRandomProducts(1000);
        Javaxcel.newInstance().writer(workbook, Product.class)
                .options(new AutoResizedColumns())
                .write(out, products);

        // then
        assertThat(file)
                .exists();
        assertThat(products.size())
                .isEqualTo(ExcelUtils.getNumOfModels(file));
    }

    @Test
    @Order(1)
    @DisplayName("applyHeaderStyle")
    @SneakyThrows
    void applyHeaderStyle(@TempDir Path path) {
        // given
        File file = new File(path.toFile(), DateTimeUtils.now() + ".xlsx");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();

        // when
        List<Product> products = MockFactory.generateRandomProducts(1000);
        Javaxcel.newInstance().writer(workbook, Product.class)
                .options(new HeaderStyles(new CustomExcelStyleConfig()))
                .write(out, products);

        // then
        assertThat(file)
                .exists();
        assertThat(products.size())
                .isEqualTo(ExcelUtils.getNumOfModels(file));
    }

    @Test
    @Order(2)
    @DisplayName("applyBodyStyle")
    @SneakyThrows
    void applyBodyStyle(@TempDir Path path) {
        // given
        File file = new File(path.toFile(), DateTimeUtils.now() + ".xlsx");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();

        // when
        List<Product> products = MockFactory.generateRandomProducts(1000);
        Javaxcel.newInstance().writer(workbook, Product.class)
                .options(new BodyStyles(new BodyStyleConfig()))
                .write(out, products);

        // then
        assertThat(file)
                .exists();
        assertThat(products.size())
                .isEqualTo(ExcelUtils.getNumOfModels(file));
    }

    @Test
    @Order(3)
    @DisplayName("applyMultipleStyle")
    @SneakyThrows
    void applyMultipleStyle() {
        // given
        File file = new File("/data", "styled.xls");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();

        // when
        List<Product> products = MockFactory.generateRandomProducts(1000);
        Javaxcel.newInstance().writer(workbook, Product.class)
                .options(new AutoResizedColumns(), new HiddenExtraRows(), new HiddenExtraColumns(),
                        new HeaderStyles(CustomExcelStyleConfig.getRainbowHeader()),
                        new BodyStyles(new BodyStyleConfig()), new EnumDropdown())
                .write(out, products);

        // then
        assertThat(file)
                .exists()
                .canRead()
                .canWrite();
    }

}
