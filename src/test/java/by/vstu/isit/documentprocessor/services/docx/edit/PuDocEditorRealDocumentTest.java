package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestData;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestSupport;
import by.vstu.isit.documentprocessor.services.docx.write.impl.PuWordGenerator;
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

class PuDocEditorRealDocumentTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/PU.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("PU: правка функции сохраняет ручную пометку и обновляет шапку")
    void edit_existingFunction_preservesManualTextAndUpdatesHeader() throws Exception {
        String pattern = pattern();
        FuncDto product = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto process = DocxTestData.func(202L, "Контроль температуры", "20±2 °C", false, "");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(product, process));
        SborEdDto sborEd = DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME);
        DockPackageDto orig = DocxTestData.dto(oper, List.of(sborEd));

        new PuWordGenerator(TEMPLATE, pattern).generate(orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "oper_101_col_0", 3, "MANUAL-PU-NOTE");

        FuncDto updatedProduct = DocxTestData.func(201L, "Контроль диаметра NEW", "Ø21±0,1", true, "S");
        FuncDto addedProcess = DocxTestData.func(203L, "Новый контроль", "NEW-PARAM", false, "");
        OperDto savedOper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(updatedProduct, process, addedProcess)
        );
        SborEdDto savedSborEd = DocxTestData.sborEd(301L, "Вал NEW", DocxTestData.PACKAGE_NAME);
        DockPackageDto saved = DocxTestData.dto("NEW_PACKAGE", List.of(savedOper), List.of(savedSborEd));

        new PuDocEditor(pattern).edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int start = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 3);

            XWPFTableRow firstRow = DocxTestSupport.rowByBookmark(table, "oper_101_col_0");
            assertThat(firstRow.getCell(4).getText()).contains("Контроль диаметра NEW");
            assertThat(firstRow.getCell(7).getText()).contains("Ø21±0,1");
            assertThat(firstRow.getCell(5).getText()).doesNotContain("Контроль температуры");
            assertThat(table.getRow(start + 1).getCell(5).getText()).contains("Контроль температуры");
            assertThat(table.getRow(start + 2).getCell(5).getText()).contains("Новый контроль");

            assertThat(DocxTestSupport.manualText(out, "oper_101_col_0", 3)).contains("MANUAL-PU-NOTE");
            assertThat(DocxTestSupport.bookmarkNames(doc)).contains("func_203_col_5", "func_203_col_7");

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
    @DisplayName("PU: правка полей операции сохраняет ручную пометку")
    void edit_operationFields_preservesManualText() throws Exception {
        String pattern = pattern();
        FuncDto func = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        OperDto oper = DocxTestData.oper(101L, "005", "Токарная", List.of(func));
        DockPackageDto orig = DocxTestData.dto(oper, List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new PuWordGenerator(TEMPLATE, pattern).generate(orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "oper_101_col_0", 3, "MANUAL-PU-OPER");

        OperDto savedOper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок NEW", "Оснастка NEW", "Токарная NEW", "Т005", "16",
                List.of(func)
        );
        DockPackageDto saved = DocxTestData.dto(List.of(savedOper),
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new PuDocEditor(pattern).edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            XWPFTableRow row = DocxTestSupport.rowByBookmark(table, "oper_101_col_0");
            assertThat(row.getCell(1).getText()).contains("Токарная NEW").contains("16");
            assertThat(row.getCell(2).getText()).contains("Станок NEW").contains("Оснастка NEW");
            assertThat(DocxTestSupport.manualText(out, "oper_101_col_0", 3)).contains("MANUAL-PU-OPER");
        }
    }

    @Test
    @DisplayName("PU: добавление операции создаёт новый блок и не стирает ручную пометку")
    void edit_addOperation_createsNewBlock() throws Exception {
        String pattern = pattern();
        FuncDto firstFunc = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        OperDto firstOper = DocxTestData.oper(101L, "005", "Токарная", List.of(firstFunc));
        DockPackageDto orig = DocxTestData.dto(firstOper,
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new PuWordGenerator(TEMPLATE, pattern).generate(orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "oper_101_col_0", 3, "MANUAL-PU-ADD-OPER");

        FuncDto secondFunc = DocxTestData.func(203L, "Контроль длины", "100±0,5", true, "");
        OperDto secondOper = DocxTestData.oper(102L, "010", "Фрезерная", List.of(secondFunc));
        DockPackageDto saved = DocxTestData.dto(List.of(firstOper, secondOper),
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new PuDocEditor(pattern).edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int start = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 2);

            XWPFTableRow added = DocxTestSupport.rowByBookmark(table, "oper_102_col_0");
            assertThat(added.getCell(0).getText()).contains("010");
            assertThat(added.getCell(1).getText()).contains("Фрезерная").contains("15");
            assertThat(added.getCell(4).getText()).contains("Контроль длины");
            assertThat(DocxTestSupport.bookmarkNames(doc)).contains("func_203_col_4");
            assertThat(DocxTestSupport.manualText(out, "oper_101_col_0", 3)).contains("MANUAL-PU-ADD-OPER");
        }
    }

    @Test
    @DisplayName("PU: удаление операции убирает её блок, ручная пометка другой операции остаётся")
    void edit_removeOperation_removesBlock() throws Exception {
        String pattern = pattern();
        FuncDto firstFunc = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        OperDto firstOper = DocxTestData.oper(101L, "005", "Токарная", List.of(firstFunc));
        FuncDto secondFunc = DocxTestData.func(202L, "Контроль длины", "100±0,5", true, "");
        OperDto secondOper = DocxTestData.oper(102L, "010", "Фрезерная", List.of(secondFunc));
        DockPackageDto orig = DocxTestData.dto(List.of(firstOper, secondOper),
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new PuWordGenerator(TEMPLATE, pattern).generate(orig);
        Path out = outputPath(orig);
        DocxTestSupport.putManualText(out, "oper_101_col_0", 3, "MANUAL-PU-KEEP");

        DockPackageDto saved = DocxTestData.dto(firstOper,
                List.of(DocxTestData.sborEd(301L, "Вал", DocxTestData.PACKAGE_NAME)));

        new PuDocEditor(pattern).edit(orig, saved);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int start = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(start + 1);
            assertThat(DocxTestSupport.bookmarkNames(doc)).doesNotContain("oper_102_col_0");
            assertThat(DocxTestSupport.manualText(out, "oper_101_col_0", 3)).contains("MANUAL-PU-KEEP");
        }
    }

    private String pattern() {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/PU{1}.docx";
    }

    private Path outputPath(DockPackageDto dto) {
        return tempDir.resolve(dto.path()).resolve("PU" + dto.puName() + ".docx");
    }
}
