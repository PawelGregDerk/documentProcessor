package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestData;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestSupport;
import by.vstu.isit.documentprocessor.services.docx.write.impl.FmeaWordGenerator;
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

class FmeaDocEditorRealDocumentTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/FMEA.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("FMEA: добавление функции сохраняет ручную пометку и обновляет шапку")
    void edit_addFunction_preservesManualTextAndUpdatesHeader() throws Exception {
        String pattern = pattern();
        FuncDto product = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto process = DocxTestData.func(202L, "Контроль температуры", "20±2 °C", false, "");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(product, process));
        DockPackageDto orig = DocxTestData.dto(oper,
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new FmeaWordGenerator(TEMPLATE, pattern).generate(orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "func_201_col_7", 8, "MANUAL-FMEA-NOTE");

        FuncDto updatedProduct = DocxTestData.func(201L, "Контроль диаметра NEW", "Ø21±0,1", true, "S2");
        FuncDto addedProcess = DocxTestData.func(203L, "Новый контроль", "NEW-PARAM", false, "K");
        OperDto savedOper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(updatedProduct, process, addedProcess)
        );
        DockPackageDto saved = DocxTestData.dto("NEW_PACKAGE", List.of(savedOper),
                List.of(DocxTestData.sborEd(301L, "Вал NEW", DocxTestData.PACKAGE_NAME)));

        new FmeaDocEditor(pattern).edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int start = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 3);

            XWPFTableRow firstRow = DocxTestSupport.rowByBookmark(table, "func_201_col_7");
            assertThat(firstRow.getCell(7).getText()).contains("Контроль диаметра NEW");
            assertThat(firstRow.getCell(17).getText()).contains("S2");
            assertThat(DocxTestSupport.manualText(out, "func_201_col_7", 8)).contains("MANUAL-FMEA-NOTE");

            XWPFTableRow addedRow = DocxTestSupport.rowByBookmark(table, "func_203_col_7");
            assertThat(addedRow.getCell(7).getText()).contains("Новый контроль");
            assertThat(addedRow.getCell(17).getText()).contains("K");

            assertThat(DocxTestSupport.bookmarkNames(doc)).contains(
                    "func_201_col_7", "func_201_col_17",
                    "func_202_col_7", "func_203_col_7", "func_203_col_17");

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

    @Test
    @DisplayName("FMEA: удаление функции удаляет строку, но не ручную пометку")
    void edit_removeFunction_removesRowAndPreservesManualText() throws Exception {
        String pattern = pattern();
        FuncDto product = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto process = DocxTestData.func(202L, "Контроль температуры", "20±2 °C", false, "");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(product, process));
        DockPackageDto orig = DocxTestData.dto(oper,
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new FmeaWordGenerator(TEMPLATE, pattern).generate(orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "func_201_col_7", 8, "MANUAL-FMEA-KEEP");

        OperDto savedOper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(product)
        );
        DockPackageDto saved = DocxTestData.dto(List.of(savedOper),
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new FmeaDocEditor(pattern).edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int start = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 1);
            assertThat(DocxTestSupport.bookmarkNames(doc)).doesNotContain("func_202_col_7");
            assertThat(DocxTestSupport.manualText(out, "func_201_col_7", 8)).contains("MANUAL-FMEA-KEEP");
        }
    }

    private String pattern() {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/FMEA{1}.docx";
    }

    private Path outputPath(DockPackageDto dto) {
        return tempDir.resolve(dto.path()).resolve("FMEA" + dto.fmeaName() + ".docx");
    }
}
