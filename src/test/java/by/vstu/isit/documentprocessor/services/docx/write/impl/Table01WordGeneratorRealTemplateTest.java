package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
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

import static org.assertj.core.api.Assertions.assertThat;

class Table01WordGeneratorRealTemplateTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/Table1.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Таблица 01: реальный шаблон — по строке на каждую сборочную единицу")
    void generate_twoSborEds_writesRowsWithBookmarks() throws Exception {
        DockPackageDto dto = DocxTestData.dto(
                DocxTestData.oper(101L, "005", "Токарная",
                        List.of(DocxTestData.func(201L, "Контроль", "Параметр", true, ""))),
                List.of(
                        DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME),
                        DocxTestData.sborEd(302L, "Втулка", "АБВГ.123456.002")
                ));

        Path out = expectedOutput(dto);
        new Table01WordGenerator(TEMPLATE, pattern()).generate(dto);

        assertThat(out).exists();
        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int start = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 2);

            XWPFTableRow row1 = table.getRow(start);
            XWPFTableRow row2 = table.getRow(start + 1);

            assertThat(row1.getCell(0).getText()).contains(DocxTestData.PACKAGE_NAME);
            assertThat(row2.getCell(0).getText()).contains("АБВГ.123456.002");
            for (int col = 1; col < 5; col++) {
                assertThat(row1.getCell(col).getText()).isEmpty();
                assertThat(row2.getCell(col).getText()).isEmpty();
            }

            assertThat(DocxTestSupport.hasFontFamily(row1.getCell(0), "Arial Narrow")).isTrue();
            assertThat(DocxTestSupport.bookmarkNames(doc)).contains("t01_name_301", "t01_name_302");
        }
    }

    @Test
    @DisplayName("Таблица 01: одна сборочная единица — файл не создаётся")
    void generate_oneSborEd_doesNotCreateFile() throws Exception {
        DockPackageDto dto = DocxTestData.dto(
                DocxTestData.oper(101L, "005", "Токарная",
                        List.of(DocxTestData.func(201L, "Контроль", "Параметр", true, ""))),
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        Path out = expectedOutput(dto);
        new Table01WordGenerator(TEMPLATE, pattern()).generate(dto);

        assertThat(out).doesNotExist();
    }

    private Path expectedOutput(DockPackageDto dto) {
        return tempDir.resolve(dto.path()).resolve("Table01.docx");
    }

    private String pattern() {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/Table01.docx";
    }
}
