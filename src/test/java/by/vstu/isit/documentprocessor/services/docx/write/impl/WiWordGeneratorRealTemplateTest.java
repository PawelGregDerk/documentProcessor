package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
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

class WiWordGeneratorRealTemplateTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/WI.docx");
    private static final String OUTPUT_PREFIX = "ВИ";

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("ВИ: реальный шаблон — две операции с шифрами и номерами инструкций")
    void generate_realTemplate_writesOperations() throws Exception {
        OperDto first = DocxTestData.oper(101L, "005", "Токарная",
                List.of(DocxTestData.func(201L, "Контроль", "Параметр", true, "")));
        OperDto second = DocxTestData.oper(102L, "010", "Фрезерная",
                List.of(DocxTestData.func(202L, "Контроль", "Параметр", true, "")));
        DockPackageDto dto = DocxTestData.dto(List.of(first, second),
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        Path out = generate(dto);
        assertThat(out).exists();

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().get(1);
            int start = DocxTestSupport.templateRows(TEMPLATE, 1);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 2);

            XWPFTableRow row1 = table.getRow(start);
            XWPFTableRow row2 = table.getRow(start + 1);

            assertThat(row1.getCell(0).getText()).contains("005");
            assertThat(row1.getCell(1).getText()).contains("Т005").contains("Токарная");
            assertThat(row1.getCell(2).getText()).contains("15-132/005");

            assertThat(row2.getCell(0).getText()).contains("010");
            assertThat(row2.getCell(1).getText()).contains("Т010").contains("Фрезерная");
            assertThat(row2.getCell(2).getText()).contains("15-132/010");

            assertThat(DocxTestSupport.bookmarkNames(doc)).contains(
                    "oper_101_col_0", "oper_101_shifr", "oper_101_name", "oper_101_nomInstr",
                    "oper_102_col_0", "oper_102_shifr", "oper_102_name", "oper_102_nomInstr");

            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .doesNotContain("{{d}}")
                    .doesNotContain("{{d1}}")
                    .doesNotContain("{{p}}")
                    .doesNotContain("{{wi}}")
                    .contains(DocxTestData.PACKAGE_NAME)
                    .contains("Вал")
                    .contains(dto.vedIName());
        }

        Properties meta = DocxTestSupport.headerMeta(out);
        assertThat(meta.getProperty("d")).isEqualTo(DocxTestData.PACKAGE_NAME);
        assertThat(meta.getProperty("d1")).isEqualTo("Вал");
        assertThat(meta.getProperty("p")).isEqualTo(DocxTestData.PACKAGE_NAME);
        assertThat(meta.getProperty("wi")).isEqualTo(dto.vedIName());
    }

    @Test
    @DisplayName("ВИ: две сборочные единицы — в шапке 'согласно таблице 1' и 'Таблица 01'")
    void generate_twoSborEds_usesTable01() throws Exception {
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная",
                List.of(DocxTestData.func(201L, "Контроль", "Параметр", true, "")));
        DockPackageDto dto = DocxTestData.dto(oper, List.of(
                DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME),
                DocxTestData.sborEd(302L, "Втулка", "АБВГ.123456.002")
        ));

        Path out = generate(dto);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .contains("согласно таблице 1")
                    .contains("Таблица 01");
        }

        Properties meta = DocxTestSupport.headerMeta(out);
        assertThat(meta.getProperty("d")).isEqualTo("согласно таблице 1");
        assertThat(meta.getProperty("d1")).isEqualTo("Таблица 01");
        assertThat(meta.getProperty("p")).isEqualTo("Таблица 01");
    }

    private Path generate(DockPackageDto dto) throws Exception {
        String pattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/" + OUTPUT_PREFIX + "{1}.docx";
        new WiWordGenerator(TEMPLATE, pattern).generate(dto);
        return tempDir.resolve(dto.path()).resolve(OUTPUT_PREFIX + dto.vedIName() + ".docx");
    }
}
