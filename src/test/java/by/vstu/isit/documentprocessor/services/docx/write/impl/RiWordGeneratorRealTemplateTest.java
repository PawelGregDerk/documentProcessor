package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class RiWordGeneratorRealTemplateTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/RI.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("РИ: реальный шаблон — отдельный документ на операцию, функции и объединение")
    void generate_realTemplate_createsOneDocumentPerOperation() throws Exception {
        FuncDto product = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto process = DocxTestData.func(202L, "Контроль температуры", "20±2 °C", false, "");
        OperDto turning = DocxTestData.oper(101L, "005", "Токарная", List.of(product, process));

        FuncDto millingFunc = DocxTestData.func(203L, "Контроль длины", "100±0,5", true, "");
        OperDto milling = DocxTestData.oper(102L, "010", "Фрезерная", List.of(millingFunc));

        DockPackageDto dto = DocxTestData.dto(List.of(turning, milling),
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        String pattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/{1}/{2}.docx";
        new RiWordGenerator(TEMPLATE, pattern).generate(dto);

        Path riDir = tempDir.resolve(dto.path()).resolve("ri");
        Path turningFile = riDir.resolve("005 Токарная.docx");
        Path millingFile = riDir.resolve("010 Фрезерная.docx");
        assertThat(turningFile).exists();
        assertThat(millingFile).exists();

        try (XWPFDocument doc = DocxTestSupport.open(turningFile)) {
            XWPFTable table = doc.getTables().get(2);
            int start = DocxTestSupport.templateRows(TEMPLATE, 2);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 2);

            XWPFTableRow row1 = table.getRow(start);
            XWPFTableRow row2 = table.getRow(start + 1);

            assertThat(row1.getCell(1).getText())
                    .contains("Контроль диаметра")
                    .contains("Ø20±0,1");
            assertThat(row2.getCell(1).getText())
                    .contains("Контроль температуры")
                    .contains("20±2 °C");
            assertThat(row1.getCell(2).getText()).contains("S");
            assertThat(row2.getCell(2).getText()).isEmpty();

            assertThat(DocxTestSupport.verticalMerge(row1.getCell(3))).isEqualTo(STMerge.RESTART);
            assertThat(DocxTestSupport.verticalMerge(row2.getCell(3))).isEqualTo(STMerge.CONTINUE);

            assertThat(DocxTestSupport.hasFontFamily(row1.getCell(1), "Arial Narrow")).isTrue();
            assertThat(DocxTestSupport.bookmarkNames(doc)).contains(
                    "func_201_name", "func_201_param", "func_201_col_2",
                    "func_202_name", "func_202_param", "func_202_col_2");

            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .doesNotContain("{{d}}")
                    .doesNotContain("{{d1}}")
                    .doesNotContain("{{p}}")
                    .doesNotContain("{{wi}}")
                    .doesNotContain("{{shop}}")
                    .doesNotContain("{{namOp}}")
                    .doesNotContain("{{numOp}}")
                    .doesNotContain("{{oObr}}")
                    .doesNotContain("{{oOst}}")
                    .doesNotContain("{{nZech}}")
                    .contains("Токарная")
                    .contains("005")
                    .contains("15-132/005")
                    .contains("Т005")
                    .contains("Станок 005")
                    .contains("Оснастка 005")
                    .contains(DocxTestData.PACKAGE_NAME)
                    .contains("Вал");
        }

        Properties meta = DocxTestSupport.headerMeta(turningFile);
        assertThat(meta.getProperty("wi")).isEqualTo("15-132/005");
        assertThat(meta.getProperty("shop")).isEqualTo("Т005");
        assertThat(meta.getProperty("namOp")).isEqualTo("Токарная");
        assertThat(meta.getProperty("numOp")).isEqualTo("005");
        assertThat(meta.getProperty("oObr")).isEqualTo("Станок 005");
        assertThat(meta.getProperty("oOst")).isEqualTo("Оснастка 005");
        assertThat(meta.getProperty("nZech")).isEqualTo("15");

        try (XWPFDocument doc = DocxTestSupport.open(millingFile)) {
            XWPFTable table = doc.getTables().get(2);
            int start = DocxTestSupport.templateRows(TEMPLATE, 2);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 1);
            assertThat(table.getRow(start).getCell(1).getText())
                    .contains("Контроль длины")
                    .contains("100±0,5");
            assertThat(DocxTestSupport.headerText(doc))
                    .contains("Фрезерная")
                    .contains("010");
        }
    }
}
