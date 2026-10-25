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

class SxPuWordGeneratorRealTemplateTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/SXPU.docx");
    private static final String OUTPUT_PREFIX = "СХПУ";

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("СХПУ: реальный шаблон — только функции со спец. характеристиками")
    void generate_realTemplate_writesOnlySpecFunctions() throws Exception {
        FuncDto spec = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto noSpec = DocxTestData.func(202L, "Контроль температуры", "20±2 °C", false, "");
        FuncDto secondSpec = DocxTestData.func(203L, "Контроль длины", "100±0,5", true, "K");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(spec, noSpec, secondSpec));
        DockPackageDto dto = DocxTestData.dto(oper, List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        Path out = generate(dto);
        assertThat(out).exists();

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().get(1);
            int start = DocxTestSupport.templateRows(TEMPLATE, 1);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 2);

            XWPFTableRow row1 = table.getRow(start);
            XWPFTableRow row2 = table.getRow(start + 1);

            assertThat(row1.getCell(1).getText()).contains("Контроль диаметра");
            assertThat(row1.getCell(2).getText()).contains("Ø20±0,1");
            assertThat(row1.getCell(3).getText()).contains("S");
            assertThat(row1.getCell(4).getText()).isEmpty();
            assertThat(row1.getCell(5).getText()).isEmpty();

            assertThat(row2.getCell(1).getText()).contains("Контроль длины");
            assertThat(row2.getCell(3).getText()).contains("K");

            assertThat(DocxTestSupport.bookmarkNames(doc))
                    .contains(
                            "func_201_col_1", "func_201_col_2", "func_201_col_3",
                            "func_201_col_4", "func_201_col_5",
                            "func_203_col_1", "func_203_col_3")
                    .doesNotContain("func_202_col_1");

            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .doesNotContain("{{puName}}")
                    .doesNotContain("{{spuName}}")
                    .doesNotContain("{{nazv}}")
                    .doesNotContain("{{oboznach}}")
                    .doesNotContain("{{packageName}}")
                    .contains(DocxTestData.PU_NAME)
                    .contains(DocxTestData.SPU_NAME)
                    .contains("Вал")
                    .contains(DocxTestData.PACKAGE_NAME);
        }

        Properties meta = DocxTestSupport.headerMeta(out);
        assertThat(meta.getProperty("puName")).isEqualTo(DocxTestData.PU_NAME);
        assertThat(meta.getProperty("spuName")).isEqualTo(DocxTestData.SPU_NAME);
        assertThat(meta.getProperty("nazv")).isEqualTo("Вал");
        assertThat(meta.getProperty("oboznach")).isEqualTo(DocxTestData.PACKAGE_NAME);
        assertThat(meta.getProperty("packageName")).isEqualTo(DocxTestData.PACKAGE_NAME);
    }

    @Test
    @DisplayName("СХПУ: без спец. характеристик — документ не создаётся")
    void generate_withoutSpecChars_doesNotCreateFile() throws Exception {
        FuncDto noSpec = DocxTestData.func(201L, "Контроль", "Параметр", true, "");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(noSpec));
        DockPackageDto dto = DocxTestData.dto(oper, List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        Path out = expectedOutput(dto);
        new SxPuWordGenerator(TEMPLATE, pattern()).generate(dto);

        assertThat(out).doesNotExist();
    }

    private Path generate(DockPackageDto dto) throws Exception {
        new SxPuWordGenerator(TEMPLATE, pattern()).generate(dto);
        return expectedOutput(dto);
    }

    private Path expectedOutput(DockPackageDto dto) {
        return tempDir.resolve(dto.path()).resolve(OUTPUT_PREFIX + dto.spuName() + ".docx");
    }

    private String pattern() {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/" + OUTPUT_PREFIX + "{1}.docx";
    }
}
