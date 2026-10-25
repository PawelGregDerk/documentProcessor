package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestData;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestSupport;
import by.vstu.isit.documentprocessor.services.docx.write.impl.RiWordGenerator;
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

class RiDocEditorRealDocumentTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/RI.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("РИ: правка, добавление и удаление функции сохраняют ручную пометку")
    void edit_addAndRemoveFunctions_preservesManualText() throws Exception {
        String pattern = pattern();
        FuncDto product = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto process = DocxTestData.func(202L, "Контроль температуры", "20±2 °C", false, "");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(product, process));
        DockPackageDto orig = DocxTestData.dto(oper,
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        RiWordGenerator generator = new RiWordGenerator(TEMPLATE, pattern);
        generator.generate(orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "func_201_name", 0, "MANUAL-RI-NOTE");

        FuncDto updatedProduct = DocxTestData.func(201L, "Контроль диаметра NEW", "Ø21±0,1", true, "S");
        FuncDto added = DocxTestData.func(203L, "Новый контроль", "NEW-PARAM", false, "K");
        OperDto savedOper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(updatedProduct, added)
        );
        DockPackageDto saved = DocxTestData.dto("NEW_PACKAGE", List.of(savedOper),
                List.of(DocxTestData.sborEd(301L, "Вал NEW", DocxTestData.PACKAGE_NAME)));

        new RiDocEditor(pattern, TEMPLATE, generator).edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().get(2);
            int start = DocxTestSupport.templateRows(TEMPLATE, 2);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 2);

            XWPFTableRow firstRow = DocxTestSupport.rowByBookmark(table, "func_201_name");
            assertThat(firstRow.getCell(1).getText())
                    .contains("Контроль диаметра NEW")
                    .contains("Ø21±0,1");
            assertThat(DocxTestSupport.manualText(out, "func_201_name", 0)).contains("MANUAL-RI-NOTE");

            XWPFTableRow addedRow = DocxTestSupport.rowByBookmark(table, "func_203_name");
            assertThat(addedRow.getCell(1).getText()).contains("Новый контроль").contains("NEW-PARAM");
            assertThat(DocxTestSupport.bookmarkNames(doc)).doesNotContain("func_202_name");

            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .contains("Вал NEW")
                    .contains("NEW_PACKAGE")
                    .doesNotContain("{{d}}")
                    .doesNotContain("{{d1}}")
                    .doesNotContain("{{p}}")
                    .doesNotContain("{{wi}}")
                    .doesNotContain("{{shop}}")
                    .doesNotContain("{{namOp}}")
                    .doesNotContain("{{numOp}}")
                    .doesNotContain("{{oObr}}")
                    .doesNotContain("{{oOst}}")
                    .doesNotContain("{{nZech}}");
        }

        Properties meta = DocxTestSupport.headerMeta(out);
        assertThat(meta.getProperty("d1")).isEqualTo("Вал NEW");
        assertThat(meta.getProperty("p")).isEqualTo("NEW_PACKAGE");
        assertThat(meta.getProperty("wi")).isEqualTo("15-132/005");
    }

    private String pattern() {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/{1}/{2}.docx";
    }

    private Path outputPath(DockPackageDto dto) {
        return tempDir.resolve(dto.path()).resolve("ri").resolve("005 Токарная.docx");
    }
}
