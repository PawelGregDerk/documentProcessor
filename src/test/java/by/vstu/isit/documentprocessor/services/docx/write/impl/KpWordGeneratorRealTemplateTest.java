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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class KpWordGeneratorRealTemplateTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/KP.docx");
    private static final String OUTPUT_PREFIX = "КП";

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("КП: реальный шаблон — операция, продукт/процесс и спец. характеристика")
    void generate_realTemplate_writesOperationProductProcessAndSpec() throws Exception {
        FuncDto product = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto process = DocxTestData.func(202L, "Контроль температуры", "20±2 °C", false, "");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(product, process));
        DockPackageDto dto = DocxTestData.dto(oper, List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        Path out = generate(dto);
        assertThat(out).exists();

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int first = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(first + 1);

            XWPFTableRow row = table.getRow(first);
            assertThat(row.getCell(0).getText()).contains("005");
            assertThat(row.getCell(7).getText())
                    .contains("Токарная")
                    .contains("15")
                    .contains("Станок 005");
            assertThat(row.getCell(8).getText()).contains("S");
            assertThat(row.getCell(9).getText())
                    .contains("Контроль диаметра")
                    .contains("Ø20±0,1");
            assertThat(row.getCell(10).getText())
                    .contains("Контроль температуры")
                    .contains("20±2 °C");
            assertThat(row.getCell(9).getText()).doesNotContain("Контроль температуры");
            assertThat(row.getCell(10).getText()).doesNotContain("Контроль диаметра");

            assertThat(DocxTestSupport.bookmarkNames(doc)).contains(
                    "oper_101_col_0", "oper_101_name", "oper_101_numZech", "oper_101_oborud",
                    "func_201_col_8", "func_201_name", "func_201_param",
                    "func_202_name", "func_202_param");

            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .doesNotContain("{{d}}")
                    .doesNotContain("{{n}}")
                    .doesNotContain("{{p}}")
                    .contains("Вал " + DocxTestData.PACKAGE_NAME)
                    .contains(dto.kpName());
        }

        Properties meta = DocxTestSupport.headerMeta(out);
        assertThat(meta.getProperty("d")).isEqualTo("Вал " + DocxTestData.PACKAGE_NAME);
        assertThat(meta.getProperty("n")).isEqualTo(dto.kpName());
        assertThat(meta.getProperty("p")).isEqualTo(DocxTestData.PACKAGE_NAME);
    }

    @Test
    @DisplayName("КП: без спец. характеристик — ячейка 8 пустая, закладка операции есть")
    void generate_withoutSpecChar_keepsSpecCellEmpty() throws Exception {
        FuncDto product = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(product));
        DockPackageDto dto = DocxTestData.dto(oper, List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        Path out = generate(dto);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int first = DocxTestSupport.templateRows(TEMPLATE, 0);
            XWPFTableRow row = table.getRow(first);
            assertThat(row.getCell(8).getText()).isEmpty();
            assertThat(DocxTestSupport.hasBookmark(row.getCell(8), "oper_101_col_8")).isTrue();
        }
    }

    private Path generate(DockPackageDto dto) throws Exception {
        String pattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/" + OUTPUT_PREFIX + "{1}.docx";
        new KpWordGenerator(TEMPLATE, pattern).generate(dto);
        return tempDir.resolve(dto.path()).resolve(OUTPUT_PREFIX + dto.kpName() + ".docx");
    }
}
