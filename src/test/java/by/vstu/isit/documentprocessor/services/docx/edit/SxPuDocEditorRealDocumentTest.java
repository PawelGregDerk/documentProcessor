package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestData;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestSupport;
import by.vstu.isit.documentprocessor.services.docx.write.impl.SxPuWordGenerator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class SxPuDocEditorRealDocumentTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/SXPU.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("СХПУ: правка, добавление и удаление спец. функции сохраняют ручную пометку")
    void edit_addAndRemoveSpecFunctions_preservesManualText() throws Exception {
        String pattern = pattern();
        FuncDto firstSpec = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto secondSpec = DocxTestData.func(202L, "Контроль длины", "100±0,5", true, "K");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(firstSpec, secondSpec));
        DockPackageDto orig = DocxTestData.dto(oper,
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new SxPuWordGenerator(TEMPLATE, pattern).generate(orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "func_201_col_1", 0, "MANUAL-SXPY-NOTE");

        FuncDto updatedFirst = DocxTestData.func(201L, "Контроль диаметра NEW", "Ø21±0,1", true, "S2");
        FuncDto added = DocxTestData.func(203L, "Новый спец", "NEW-PARAM", true, "K2");
        OperDto savedOper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(updatedFirst, added)
        );
        DockPackageDto saved = DocxTestData.dto("NEW_PACKAGE", List.of(savedOper),
                List.of(DocxTestData.sborEd(301L, "Вал NEW", DocxTestData.PACKAGE_NAME)));

        SxPuDocEditor editor = new SxPuDocEditor(pattern);
        ReflectionTestUtils.setField(editor, "sxPuWordGenerator", new SxPuWordGenerator(TEMPLATE, pattern));
        editor.edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().get(1);
            int start = DocxTestSupport.templateRows(TEMPLATE, 1);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 2);

            XWPFTableRow firstRow = DocxTestSupport.rowByBookmark(table, "func_201_col_1");
            assertThat(firstRow.getCell(1).getText()).contains("Контроль диаметра NEW");
            assertThat(firstRow.getCell(2).getText()).contains("Ø21±0,1");
            assertThat(firstRow.getCell(3).getText()).contains("S2");
            assertThat(DocxTestSupport.manualText(out, "func_201_col_1", 0)).contains("MANUAL-SXPY-NOTE");

            XWPFTableRow addedRow = DocxTestSupport.rowByBookmark(table, "func_203_col_1");
            assertThat(addedRow.getCell(1).getText()).contains("Новый спец");
            assertThat(addedRow.getCell(3).getText()).contains("K2");

            assertThat(DocxTestSupport.bookmarkNames(doc)).doesNotContain("func_202_col_1");

            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .contains("Вал NEW")
                    .contains("NEW_PACKAGE")
                    .doesNotContain("{{puName}}")
                    .doesNotContain("{{spuName}}")
                    .doesNotContain("{{nazv}}")
                    .doesNotContain("{{oboznach}}")
                    .doesNotContain("{{packageName}}");
        }

        Properties meta = DocxTestSupport.headerMeta(out);
        assertThat(meta.getProperty("nazv")).isEqualTo("Вал NEW");
        assertThat(meta.getProperty("packageName")).isEqualTo("NEW_PACKAGE");
    }

    private String pattern() {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/СХПУ{1}.docx";
    }

    private Path outputPath(DockPackageDto dto) {
        return tempDir.resolve(dto.path()).resolve("СХПУ" + dto.spuName() + ".docx");
    }
}
