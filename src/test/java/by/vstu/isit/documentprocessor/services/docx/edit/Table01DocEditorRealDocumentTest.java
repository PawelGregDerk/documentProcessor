package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestData;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestSupport;
import by.vstu.isit.documentprocessor.services.docx.write.impl.Table01WordGenerator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Table01DocEditorRealDocumentTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/Table1.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Таблица 01: правка, добавление и удаление сборочной единицы сохраняют ручную пометку")
    void edit_addAndRemoveSborEd_preservesManualText() throws Exception {
        String pattern = pattern();
        DockPackageDto orig = DocxTestData.dto(
                DocxTestData.oper(101L, "005", "Токарная",
                        List.of(DocxTestData.func(201L, "Контроль", "Параметр", true, ""))),
                List.of(
                        DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME),
                        DocxTestData.sborEd(302L, "Втулка", "АБВГ.123456.002")
                ));
        new Table01WordGenerator(TEMPLATE, pattern).generate(orig);

        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "t01_name_301", 1, "MANUAL-T01-NOTE");

        Path archive = tempDir.resolve("archive").resolve("Table01.docx");
        Files.createDirectories(archive.getParent());
        Files.copy(out, archive);

        DockPackageDto saved = DocxTestData.dto(
                DocxTestData.oper(101L, "005", "Токарная",
                        List.of(DocxTestData.func(201L, "Контроль", "Параметр", true, ""))),
                List.of(
                        new SborEdDto(301L, 1L, "Вал NEW", DocxTestData.PACKAGE_NAME),
                        new SborEdDto(303L, 1L, "Крышка", "АБВГ.123456.003")
                ));

        Table01DocEditor editor = new Table01DocEditor(pattern, new Table01WordGenerator(TEMPLATE, pattern));
        editor.setSourceFolderOverride("archive");
        editor.edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int start = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 2);

            XWPFTableRow row301 = DocxTestSupport.rowByBookmark(table, "t01_name_301");
            assertThat(row301.getCell(0).getText()).contains(DocxTestData.PACKAGE_NAME);
            assertThat(DocxTestSupport.manualText(out, "t01_name_301", 1)).contains("MANUAL-T01-NOTE");

            XWPFTableRow row303 = DocxTestSupport.rowByBookmark(table, "t01_name_303");
            assertThat(row303.getCell(0).getText()).contains("АБВГ.123456.003");
            assertThat(DocxTestSupport.bookmarkNames(doc)).doesNotContain("t01_name_302");
        }
    }

    private String pattern() {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/Table01.docx";
    }

    private Path outputPath(DockPackageDto dto) {
        return tempDir.resolve(dto.path()).resolve("Table01.docx");
    }
}
