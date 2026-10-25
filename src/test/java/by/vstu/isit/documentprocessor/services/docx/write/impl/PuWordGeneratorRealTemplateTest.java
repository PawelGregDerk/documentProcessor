package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class PuWordGeneratorRealTemplateTest {

    private static final Resource TEMPLATE = new ClassPathResource("templates/PU.docx");
    private static final String PACKAGE_NAME = "АБВГ.123456.001";
    private static final String PU_NAME = "0360";
    private static final String PACKAGE_FOLDER = "pkg-1";

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("PU: реальный шаблон — операция и функция попадают в ячейки, закладки и шапку")
    void generate_realTemplate_writesOperationFunctionAndHeader() throws Exception {
        FuncDto func = new FuncDto(201L, "Контроль диаметра", 101L, "Ø20±0,1", true, "S");
        DockPackageDto dto = dto(List.of(func), List.of(sborEd(301L, "Вал", PACKAGE_NAME)));

        Path out = generate(dto);

        assertThat(out).exists();
        assertThat(Files.size(out)).isPositive();

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int templateRows = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(templateRows + 1);

            assertThat(DocxTestSupport.cellByBookmark(table, "oper_101_col_0").getText()).contains("005");
            assertThat(DocxTestSupport.cellByBookmark(table, "oper_101_col_1").getText())
                    .contains("Токарная")
                    .contains("15");
            assertThat(DocxTestSupport.cellByBookmark(table, "oper_101_oborud").getText())
                    .contains("Станок 1")
                    .contains("Оснастка 1");

            assertThat(DocxTestSupport.cellByBookmark(table, "func_201_col_4").getText())
                    .contains("Контроль диаметра");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_201_col_5").getText())
                    .doesNotContain("Контроль диаметра");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_201_col_6").getText()).contains("S");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_201_col_7").getText()).contains("Ø20±0,1");

            assertThat(DocxTestSupport.hasFontFamily(
                    DocxTestSupport.cellByBookmark(table, "func_201_col_4"), "Arial Narrow")).isTrue();

            assertThat(DocxTestSupport.bookmarkNames(doc)).contains(
                    "oper_101_col_0", "oper_101_col_1", "oper_101_numZech",
                    "oper_101_oborud", "oper_101_ostnasInstr",
                    "func_201_col_4", "func_201_col_5", "func_201_col_6", "func_201_col_7");

            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .doesNotContain("{{d}}")
                    .doesNotContain("{{n}}")
                    .doesNotContain("{{p}}")
                    .contains("Вал " + PACKAGE_NAME)
                    .contains(PU_NAME);
        }

        assertThatHeaderMeta(out, "Вал " + PACKAGE_NAME, PU_NAME, PACKAGE_NAME);
    }

    @Test
    @DisplayName("PU: две функции — продукт и процесс разнесены, объединения выставлены")
    void generate_twoFuncs_splitsProductAndProcessAndSetsMerges() throws Exception {
        FuncDto product = new FuncDto(201L, "Контроль диаметра", 101L, "Ø20±0,1", true, "S");
        FuncDto process = new FuncDto(202L, "Контроль температуры", 101L, "20±2 °C", false, "");
        DockPackageDto dto = dto(List.of(product, process), List.of(sborEd(301L, "Вал", PACKAGE_NAME)));

        Path out = generate(dto);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            XWPFTable table = doc.getTables().getFirst();
            int first = DocxTestSupport.templateRows(TEMPLATE, 0);
            assertThat(table.getNumberOfRows()).isEqualTo(first + 2);

            XWPFTableRow productRow = table.getRow(first);
            XWPFTableRow processRow = table.getRow(first + 1);

            assertThat(productRow.getCell(4).getText()).contains("Контроль диаметра");
            assertThat(productRow.getCell(5).getText()).doesNotContain("Контроль температуры");
            assertThat(processRow.getCell(4).getText()).doesNotContain("Контроль диаметра");
            assertThat(processRow.getCell(5).getText()).contains("Контроль температуры");

            assertThat(DocxTestSupport.gridSpan(productRow.getCell(2))).isEqualTo(BigInteger.valueOf(2));
            assertThat(DocxTestSupport.gridSpan(productRow.getCell(9))).isEqualTo(BigInteger.valueOf(2));

            for (int col : new int[]{0, 1, 2, 12, 13}) {
                assertThat(DocxTestSupport.verticalMerge(productRow.getCell(col))).isEqualTo(STMerge.RESTART);
                assertThat(DocxTestSupport.verticalMerge(processRow.getCell(col))).isEqualTo(STMerge.CONTINUE);
            }
        }
    }

    @Test
    @DisplayName("PU: две сборочные единицы — в шапке 'согласно таблице 1' и 'Таблица 01'")
    void generate_twoSborEds_usesTable01InHeader() throws Exception {
        FuncDto func = new FuncDto(201L, "Контроль диаметра", 101L, "Ø20±0,1", true, "S");
        DockPackageDto dto = dto(List.of(func), List.of(
                sborEd(301L, "Вал", PACKAGE_NAME),
                sborEd(302L, "Втулка", "АБВГ.123456.002")
        ));

        Path out = generate(dto);

        try (XWPFDocument doc = DocxTestSupport.open(out)) {
            String header = DocxTestSupport.headerText(doc);
            assertThat(header)
                    .contains("Вал согласно таблице 1")
                    .contains("Таблица 01");
        }

        assertThatHeaderMeta(out, "Вал согласно таблице 1", PU_NAME, "Таблица 01");
    }

    private DockPackageDto dto(List<FuncDto> funcs, List<SborEdDto> sborEds) {
        OperDto oper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок 1", "Оснастка 1", "Токарная", "Т01", "15",
                funcs
        );
        return new DockPackageDto(
                1L, PACKAGE_NAME, PACKAGE_FOLDER,
                PU_NAME, "0002", "0309", "T0363", "15-132",
                List.of(oper), sborEds
        );
    }

    private SborEdDto sborEd(Long id, String nazv, String oboznach) {
        return new SborEdDto(id, 1L, nazv, oboznach);
    }

    private Path generate(DockPackageDto dto) throws Exception {
        String pattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/PU{1}.docx";
        new PuWordGenerator(TEMPLATE, pattern).generate(dto);
        return tempDir.resolve(dto.path()).resolve("PU" + dto.puName() + ".docx");
    }

    private void assertThatHeaderMeta(Path out, String d, String n, String p) throws Exception {
        Path meta = out.getParent().resolve(".docproc-meta").resolve(out.getFileName() + ".hdr.properties");
        assertThat(meta).exists();

        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(meta, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        assertThat(props.getProperty("d")).isEqualTo(d);
        assertThat(props.getProperty("n")).isEqualTo(n);
        assertThat(props.getProperty("p")).isEqualTo(p);
    }
}
