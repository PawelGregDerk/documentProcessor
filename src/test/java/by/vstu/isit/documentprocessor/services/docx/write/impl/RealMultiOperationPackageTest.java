package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RealMultiOperationPackageTest {

    private static final Resource PU = new ClassPathResource("templates/PU.docx");
    private static final Resource FMEA = new ClassPathResource("templates/FMEA.docx");
    private static final Resource KP = new ClassPathResource("templates/KP.docx");
    private static final Resource WI = new ClassPathResource("templates/WI.docx");
    private static final Resource RI = new ClassPathResource("templates/RI.docx");
    private static final Resource SXPY = new ClassPathResource("templates/SXPU.docx");
    private static final Resource TABLE01 = new ClassPathResource("templates/Table1.docx");

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Пакет с 3 операциями и 3 сборочными единицами создаёт все документы")
    void generate_multiOperationPackage_createsAllDocuments() throws Exception {
        FuncDto diameter = DocxTestData.func(201L, "Контроль диаметра", "Ø20±0,1", true, "S");
        FuncDto temperature = DocxTestData.func(202L, "Контроль температуры", "20±2 °C", false, "");
        FuncDto length = DocxTestData.func(203L, "Контроль длины", "100±0,5", true, "");
        FuncDto roughness = DocxTestData.func(204L, "Контроль шероховатости", "Ra 1,6", false, "K");
        FuncDto hole = DocxTestData.func(205L, "Контроль отверстия", "Ø8H7", true, "S");

        OperDto turning = DocxTestData.oper(101L, "005", "Токарная", List.of(diameter, temperature));
        OperDto milling = DocxTestData.oper(102L, "010", "Фрезерная", List.of(length, roughness));
        OperDto drilling = DocxTestData.oper(103L, "015", "Сверлильная", List.of(hole));

        SborEdDto shaft = DocxTestData.sborEd(301L, "Вал", "АБВГ.123456.001");
        SborEdDto bush = DocxTestData.sborEd(302L, "Втулка", "АБВГ.123456.002");
        SborEdDto cover = DocxTestData.sborEd(303L, "Крышка", "АБВГ.123456.003");

        DockPackageDto dto = new DockPackageDto(
                1L, DocxTestData.PACKAGE_NAME, DocxTestData.PACKAGE_FOLDER,
                DocxTestData.PU_NAME, DocxTestData.SPU_NAME, DocxTestData.KP_NAME,
                DocxTestData.FMEA_NAME, DocxTestData.VED_I_NAME,
                List.of(turning, milling, drilling), List.of(shaft, bush, cover));

        String base = tempDir.toAbsolutePath().toString().replace('\\', '/');
        new PuWordGenerator(PU, base + "/{0}/PU{1}.docx").generate(dto);
        new FmeaWordGenerator(FMEA, base + "/{0}/FMEA{1}.docx").generate(dto);
        DocxTestSupport.generateKp(KP, base + "/{0}/КП{1}.docx", dto);
        new WiWordGenerator(WI, base + "/{0}/ВИ{1}.docx").generate(dto);
        new RiWordGenerator(RI, base + "/{0}/{1}/{2}.docx").generate(dto);
        new SxPuWordGenerator(SXPY, base + "/{0}/СХПУ{1}.docx").generate(dto);
        new Table01WordGenerator(TABLE01, base + "/{0}/Table01.docx").generate(dto);

        Path packageDir = tempDir.resolve(DocxTestData.PACKAGE_FOLDER);
        assertThat(packageDir.resolve("PU" + DocxTestData.PU_NAME + ".docx")).exists();
        assertThat(packageDir.resolve("FMEA" + DocxTestData.FMEA_NAME + ".docx")).exists();
        assertThat(packageDir.resolve("КП" + DocxTestData.KP_NAME + ".docx")).exists();
        assertThat(packageDir.resolve("ВИ" + DocxTestData.VED_I_NAME + ".docx")).exists();
        assertThat(packageDir.resolve("СХПУ" + DocxTestData.SPU_NAME + ".docx")).exists();
        assertThat(packageDir.resolve("Table01.docx")).exists();
        assertThat(packageDir.resolve("ri/005 Токарная.docx")).exists();
        assertThat(packageDir.resolve("ri/010 Фрезерная.docx")).exists();
        assertThat(packageDir.resolve("ri/015 Сверлильная.docx")).exists();

        assertPu(packageDir.resolve("PU" + DocxTestData.PU_NAME + ".docx"));
        assertFmea(packageDir.resolve("FMEA" + DocxTestData.FMEA_NAME + ".docx"));
        assertKp(packageDir.resolve("КП" + DocxTestData.KP_NAME + ".docx"));
        assertWi(packageDir.resolve("ВИ" + DocxTestData.VED_I_NAME + ".docx"));
        assertSxPy(packageDir.resolve("СХПУ" + DocxTestData.SPU_NAME + ".docx"));
        assertRi(packageDir);
        assertTable01(packageDir.resolve("Table01.docx"));
    }

    private void assertPu(Path file) throws Exception {
        try (XWPFDocument doc = DocxTestSupport.open(file)) {
            XWPFTable table = doc.getTables().getFirst();
            assertThat(table.getNumberOfRows()).isEqualTo(DocxTestSupport.templateRows(PU, 0) + 5);
            assertThat(DocxTestSupport.cellByBookmark(table, "oper_103_col_0").getText()).contains("015");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_201_col_4").getText())
                    .contains("Контроль диаметра");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_202_col_5").getText())
                    .contains("Контроль температуры");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_204_col_6").getText()).contains("K");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_205_col_7").getText()).contains("Ø8H7");
            assertHeader(doc, true);
        }
    }

    private void assertFmea(Path file) throws Exception {
        try (XWPFDocument doc = DocxTestSupport.open(file)) {
            XWPFTable table = doc.getTables().getFirst();
            assertThat(table.getNumberOfRows()).isEqualTo(DocxTestSupport.templateRows(FMEA, 0) + 5);
            assertThat(DocxTestSupport.cellByBookmark(table, "oper_102_numOper").getText()).contains("010");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_204_col_17").getText()).contains("K");
            assertHeader(doc, true);
        }
    }

    private void assertKp(Path file) throws Exception {
        try (XWPFDocument doc = DocxTestSupport.open(file)) {
            XWPFTable table = doc.getTables().getFirst();
            assertThat(table.getNumberOfRows()).isEqualTo(DocxTestSupport.templateRows(KP, 0) + 3);
            assertThat(DocxTestSupport.cellByBookmark(table, "oper_103_col_0").getText()).contains("015");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_201_col_8").getText()).contains("S");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_204_col_8").getText()).contains("K");
            assertHeader(doc, true);
        }
    }

    private void assertWi(Path file) throws Exception {
        try (XWPFDocument doc = DocxTestSupport.open(file)) {
            XWPFTable table = doc.getTables().get(1);
            assertThat(table.getNumberOfRows()).isEqualTo(DocxTestSupport.templateRows(WI, 1) + 3);
            assertThat(DocxTestSupport.cellByBookmark(table, "oper_103_nomInstr").getText()).contains("15-132/015");
            assertHeader(doc, true);
        }
    }

    private void assertSxPy(Path file) throws Exception {
        try (XWPFDocument doc = DocxTestSupport.open(file)) {
            XWPFTable table = doc.getTables().get(1);
            assertThat(table.getNumberOfRows()).isEqualTo(DocxTestSupport.templateRows(SXPY, 1) + 3);
            assertThat(DocxTestSupport.cellByBookmark(table, "func_205_col_3").getText()).contains("S");
            assertHeader(doc, false);
        }
    }

    private void assertRi(Path packageDir) throws Exception {
        assertRiFile(packageDir.resolve("ri/005 Токарная.docx"), 2, "Контроль диаметра");
        assertRiFile(packageDir.resolve("ri/010 Фрезерная.docx"), 2, "Контроль длины");
        assertRiFile(packageDir.resolve("ri/015 Сверлильная.docx"), 1, "Контроль отверстия");
    }

    private void assertRiFile(Path file, int functions, String functionName) throws Exception {
        try (XWPFDocument doc = DocxTestSupport.open(file)) {
            XWPFTable table = doc.getTables().get(2);
            assertThat(table.getNumberOfRows()).isEqualTo(DocxTestSupport.templateRows(RI, 2) + functions);
            assertThat(DocxTestSupport.bodyContains(doc, functionName)).isTrue();
            assertHeader(doc, true);
        }
    }

    private void assertTable01(Path file) throws Exception {
        try (XWPFDocument doc = DocxTestSupport.open(file)) {
            XWPFTable table = doc.getTables().getFirst();
            assertThat(table.getNumberOfRows()).isEqualTo(DocxTestSupport.templateRows(TABLE01, 0) + 3);
            assertThat(DocxTestSupport.cellByBookmark(table, "t01_name_303").getText())
                    .contains("АБВГ.123456.003");
        }
    }

    private void assertHeader(XWPFDocument doc, boolean table01Text) {
        String header = DocxTestSupport.headerText(doc);
        assertThat(header)
                .doesNotContain("{{")
                .contains("согласно таблице 1");
        if (table01Text) {
            assertThat(header).contains("Таблица 01");
        }
    }
}
