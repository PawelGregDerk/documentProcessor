package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestData;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestSupport;
import by.vstu.isit.documentprocessor.services.docx.write.impl.WiWordGenerator;
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

class WiDocEditorRealDocumentTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/WI.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("ВИ: правка операции и добавление новой сохраняют ручную пометку")
    void edit_operationAndNewRow_preservesManualText() throws Exception {
        String pattern = pattern();
        OperDto first = DocxTestData.oper(101L, "005", "Токарная",
                List.of(DocxTestData.func(201L, "Контроль", "Параметр", true, "")));
        DockPackageDto orig = DocxTestData.dto(first,
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new WiWordGenerator(TEMPLATE, pattern).generate(orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "oper_101_col_0", 3, "MANUAL-WI-NOTE");

        OperDto updatedFirst = new OperDto(
                101L, 1L, "005", "15-132/999",
                "Станок 005", "Оснастка 005", "Токарная NEW", "Т999", "15",
                first.funcs()
        );
        OperDto added = DocxTestData.oper(102L, "010", "Фрезерная",
                List.of(DocxTestData.func(202L, "Контроль", "Параметр", true, "")));
        DockPackageDto saved = DocxTestData.dto("NEW_PACKAGE", List.of(updatedFirst, added),
                List.of(DocxTestData.sborEd(301L, "Вал NEW", DocxTestData.PACKAGE_NAME)));

        new WiDocEditor(pattern).edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().get(1);
            int start = DocxTestSupport.templateRows(TEMPLATE, 1);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 2);

            XWPFTableRow firstRow = DocxTestSupport.rowByBookmark(table, "oper_101_col_0");
            assertThat(firstRow.getCell(1).getText()).contains("Т999").contains("Токарная NEW");
            assertThat(firstRow.getCell(2).getText()).contains("15-132/999");
            assertThat(DocxTestSupport.manualText(out, "oper_101_col_0", 3)).contains("MANUAL-WI-NOTE");

            XWPFTableRow addedRow = DocxTestSupport.rowByBookmark(table, "oper_102_col_0");
            assertThat(addedRow.getCell(0).getText()).contains("010");
            assertThat(addedRow.getCell(1).getText()).contains("Т010").contains("Фрезерная");
            assertThat(addedRow.getCell(2).getText()).contains("15-132/010");

            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .contains("Вал NEW")
                    .contains("NEW_PACKAGE")
                    .doesNotContain("{{d}}")
                    .doesNotContain("{{d1}}")
                    .doesNotContain("{{p}}")
                    .doesNotContain("{{wi}}");
        }

        Properties meta = DocxTestSupport.headerMeta(out);
        assertThat(meta.getProperty("d1")).isEqualTo("Вал NEW");
        assertThat(meta.getProperty("p")).isEqualTo("NEW_PACKAGE");
        assertThat(meta.getProperty("wi")).isEqualTo(DocxTestData.VED_I_NAME);
    }

    private String pattern() {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/ВИ{1}.docx";
    }

    private Path outputPath(DockPackageDto dto) {
        return tempDir.resolve(dto.path()).resolve("ВИ" + dto.vedIName() + ".docx");
    }
}
