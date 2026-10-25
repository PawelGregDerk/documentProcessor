package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestData;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestSupport;
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

class KpDocEditorRealDocumentTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/KP.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("КП: правка продукта/процесса сохраняет ручную пометку и обновляет шапку")
    void edit_functions_preservesManualTextAndUpdatesHeader() throws Exception {
        String pattern = pattern();
        FuncDto product = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto process = DocxTestData.func(202L, "Контроль температуры", "20±2 °C", false, "");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(product, process));
        DockPackageDto orig = DocxTestData.dto(oper,
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        DocxTestSupport.generateKp(TEMPLATE, pattern, orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "oper_101_col_0", 1, "MANUAL-KP-NOTE");

        FuncDto updatedProduct = DocxTestData.func(201L, "Контроль диаметра NEW", "Ø21±0,1", true, "S");
        FuncDto addedProduct = DocxTestData.func(203L, "Новый продукт", "NEW-PARAM", true, "K");
        OperDto savedOper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(updatedProduct, process, addedProduct)
        );
        DockPackageDto saved = DocxTestData.dto("NEW_PACKAGE", List.of(savedOper),
                List.of(DocxTestData.sborEd(301L, "Вал NEW", DocxTestData.PACKAGE_NAME)));

        new KpDocEditor(pattern).edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int start = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 1);

            XWPFTableRow row = DocxTestSupport.rowByBookmark(table, "oper_101_col_0");
            assertThat(row.getCell(9).getText())
                    .contains("Контроль диаметра NEW")
                    .contains("Ø21±0,1")
                    .contains("Новый продукт")
                    .contains("NEW-PARAM");
            assertThat(row.getCell(10).getText())
                    .contains("Контроль температуры")
                    .contains("20±2 °C")
                    .doesNotContain("Новый продукт");
            assertThat(row.getCell(8).getText()).contains("S").contains("K");
            assertThat(DocxTestSupport.manualText(out, "oper_101_col_0", 1)).contains("MANUAL-KP-NOTE");

            assertThat(DocxTestSupport.bookmarkNames(doc)).contains("func_203_name", "func_203_param");

            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .contains("Вал NEW")
                    .contains("NEW_PACKAGE")
                    .doesNotContain("{{d}}")
                    .doesNotContain("{{n}}")
                    .doesNotContain("{{p}}");
        }

        Properties meta = DocxTestSupport.headerMeta(out);
        assertThat(meta.getProperty("d")).isEqualTo("Вал NEW " + DocxTestData.PACKAGE_NAME);
        assertThat(meta.getProperty("p")).isEqualTo("NEW_PACKAGE");
    }

    private String pattern() {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/КП{1}.docx";
    }

    private Path outputPath(DockPackageDto dto) {
        return tempDir.resolve(dto.path()).resolve("КП" + dto.kpName() + ".docx");
    }
}
