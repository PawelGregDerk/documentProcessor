package by.vstu.isit.documentprocessor.services;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.docx.edit.AbstractDocEditor;
import by.vstu.isit.documentprocessor.services.docx.edit.FmeaDocEditor;
import by.vstu.isit.documentprocessor.services.docx.edit.KpDocEditor;
import by.vstu.isit.documentprocessor.services.docx.edit.PuDocEditor;
import by.vstu.isit.documentprocessor.services.docx.edit.RiDocEditor;
import by.vstu.isit.documentprocessor.services.docx.edit.SxPuDocEditor;
import by.vstu.isit.documentprocessor.services.docx.edit.Table01DocEditor;
import by.vstu.isit.documentprocessor.services.docx.edit.WiDocEditor;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestData;
import by.vstu.isit.documentprocessor.services.docx.write.impl.DocxTestSupport;
import by.vstu.isit.documentprocessor.services.docx.write.impl.FmeaWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.PuWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.RiWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.SxPuWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.Table01WordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.WiWordGenerator;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.main.web-application-type=none")
@Transactional
@EnabledIf("databaseAvailable")
class PackageLifecycleDatabaseTest {

    private static final String LOCAL_URL =
            "jdbc:mysql://localhost:3306/vsep?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000";
    private static final Resource PU = new ClassPathResource("templates/PU.docx");
    private static final Resource FMEA = new ClassPathResource("templates/FMEA.docx");
    private static final Resource KP = new ClassPathResource("templates/KP.docx");
    private static final Resource WI = new ClassPathResource("templates/WI.docx");
    private static final Resource RI = new ClassPathResource("templates/RI.docx");
    private static final Resource SXPY = new ClassPathResource("templates/SXPU.docx");
    private static final Resource TABLE01 = new ClassPathResource("templates/Table1.docx");

    @Autowired
    private DocpackageService service;

    @TempDir
    Path tempDir;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        Properties local = localConfig();
        registry.add("spring.datasource.url", () -> LOCAL_URL);
        registry.add("spring.datasource.username",
                () -> local.getProperty("spring.datasource.username", ""));
        registry.add("spring.datasource.password",
                () -> local.getProperty("spring.datasource.password", ""));
        registry.add("spring.datasource.driverClassName", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    static boolean databaseAvailable() {
        Properties local = localConfig();
        String user = local.getProperty("spring.datasource.username");
        String password = local.getProperty("spring.datasource.password");
        if (user == null || password == null) {
            return false;
        }
        try (Connection ignored = DriverManager.getConnection(LOCAL_URL, user, password)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Properties localConfig() {
        Path path = Path.of(System.getProperty("user.dir"), "config", "application.properties");
        Properties props = new Properties();
        if (!Files.exists(path)) {
            return props;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (Exception ignored) {
            // tests will be skipped if credentials could not be read
        }
        return props;
    }

    @Test
    @DisplayName("Пакет: создание, две правки, удаление операции/функции и архивы")
    void createEditTwice_checksDatabaseDocumentsAndArchives() throws Exception {
        String marker = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String path = "e2e-" + marker;
        String packageName = "E2E-" + marker;
        String base = tempDir.toAbsolutePath().toString().replace('\\', '/');
        String puPattern = base + "/{0}/PU{1}.docx";
        String fmeaPattern = base + "/{0}/FMEA{1}.docx";
        String kpPattern = base + "/{0}/КП{1}.docx";
        String wiPattern = base + "/{0}/ВИ{1}.docx";
        String riPattern = base + "/{0}/{1}/{2}.docx";
        String sxpyPattern = base + "/{0}/СХПУ{1}.docx";
        String table01Pattern = base + "/{0}/Table01.docx";

        List<AbstractWordGenerator> generators = List.of(
                new PuWordGenerator(PU, puPattern),
                new FmeaWordGenerator(FMEA, fmeaPattern),
                DocxTestSupport.kpGenerator(KP, kpPattern),
                new WiWordGenerator(WI, wiPattern),
                new RiWordGenerator(RI, riPattern),
                new SxPuWordGenerator(SXPY, sxpyPattern),
                new Table01WordGenerator(TABLE01, table01Pattern)
        );
        SxPuDocEditor sxPuEditor = new SxPuDocEditor(sxpyPattern);
        ReflectionTestUtils.setField(sxPuEditor, "sxPuWordGenerator",
                new SxPuWordGenerator(SXPY, sxpyPattern));
        List<AbstractDocEditor> editors = List.of(
                new PuDocEditor(puPattern),
                new FmeaDocEditor(fmeaPattern),
                new KpDocEditor(kpPattern),
                new WiDocEditor(wiPattern),
                new RiDocEditor(riPattern, RI, new RiWordGenerator(RI, riPattern)),
                sxPuEditor,
                new Table01DocEditor(table01Pattern, new Table01WordGenerator(TABLE01, table01Pattern))
        );
        DocPackageSaveOrchestrator orchestrator = new DocPackageSaveOrchestrator(
                service, generators, editors, new PackageFileManager(), puPattern);

        FuncDto diameter = new FuncDto(null, "Контроль диаметра", null, "Ø20±0,1", true, "S");
        FuncDto temperature = new FuncDto(null, "Контроль температуры", null, "20±2 °C", false, "");
        FuncDto length = new FuncDto(null, "Контроль длины", null, "100±0,5", true, "");
        FuncDto roughness = new FuncDto(null, "Контроль шероховатости", null, "Ra 1,6", false, "K");
        FuncDto hole = new FuncDto(null, "Контроль отверстия", null, "Ø8H7", true, "S");
        OperDto turning = new OperDto(null, null, "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(diameter, temperature));
        OperDto milling = new OperDto(null, null, "010", "15-132/010",
                "Станок 010", "Оснастка 010", "Фрезерная", "Т010", "15",
                List.of(length, roughness));
        OperDto drilling = new OperDto(null, null, "015", "15-132/015",
                "Станок 015", "Оснастка 015", "Сверлильная", "Т015", "15",
                List.of(hole));
        DockPackageDto create = new DockPackageDto(
                null, packageName, path,
                DocxTestData.PU_NAME, DocxTestData.SPU_NAME, DocxTestData.KP_NAME,
                DocxTestData.FMEA_NAME, DocxTestData.VED_I_NAME,
                List.of(turning, milling, drilling),
                List.of(
                        new SborEdDto(null, null, "Вал", "АБВГ.123456.001"),
                        new SborEdDto(null, null, "Втулка", "АБВГ.123456.002"),
                        new SborEdDto(null, null, "Крышка", "АБВГ.123456.003")));

        DockPackageDto saved = orchestrator.createPackage(create);
        assertThat(saved.id()).isNotNull();
        Path packageDir = tempDir.resolve(path);
        assertThat(packageDir.resolve("PU" + DocxTestData.PU_NAME + ".docx")).exists();
        assertThat(packageDir.resolve("ri/005 Токарная.docx")).exists();
        assertThat(packageDir.resolve("ri/010 Фрезерная.docx")).exists();
        assertThat(packageDir.resolve("ri/015 Сверлильная.docx")).exists();

        Path puFile = packageDir.resolve("PU" + DocxTestData.PU_NAME + ".docx");
        Path fmeaFile = packageDir.resolve("FMEA" + DocxTestData.FMEA_NAME + ".docx");
        long oper1Id = saved.opers().get(0).id();
        long func1Id = saved.opers().get(0).funcs().get(0).id();
        long sbor1Id = saved.sborEds().get(0).id();
        long sbor2Id = saved.sborEds().get(1).id();

        DocxTestSupport.putManualText(puFile, "oper_" + oper1Id + "_col_0", 3, "РУЧНАЯ-ПОМЕТКА-ПУ");
        DocxTestSupport.putManualText(fmeaFile, "func_" + func1Id + "_col_7", 8, "РУЧНАЯ-ПОМЕТКА-FMEA");

        assertThat(service.findDtoById(saved.id()).opers()).hasSize(3);

        FuncDto updatedDiameter = new FuncDto(
                saved.opers().get(0).funcs().get(0).id(), "Контроль диаметра NEW",
                saved.opers().get(0).id(), "Ø21±0,1", true, "S");
        FuncDto added = new FuncDto(null, "Новый контроль", saved.opers().get(0).id(), "NEW", false, "");
        OperDto firstEdited = new OperDto(
                saved.opers().get(0).id(), saved.id(), "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(updatedDiameter, saved.opers().get(0).funcs().get(1), added));
        DockPackageDto edit1 = new DockPackageDto(
                saved.id(), packageName, path,
                saved.puName(), saved.spuName(), saved.kpName(), saved.fmeaName(), saved.vedIName(),
                List.of(firstEdited, saved.opers().get(1), saved.opers().get(2)),
                saved.sborEds());

        DocPackageSaveOrchestrator.UpdatePreview preview1 =
                orchestrator.previewUpdate(edit1, saved.id(), path);
        assertThat(preview1.folderExists()).isTrue();
        assertThat(preview1.orderChanged()).isFalse();
        DocPackageSaveOrchestrator.UpdateOutcome outcome1 = orchestrator.updatePackage(edit1, preview1);
        assertThat(outcome1.orderChanged()).isFalse();

        DockPackageDto afterFirstEdit = service.findDtoById(saved.id());
        assertThat(afterFirstEdit.opers()).hasSize(3);
        assertThat(afterFirstEdit.opers().get(0).funcs()).hasSize(3);
        assertThat(afterFirstEdit.opers().get(0).funcs().get(2).id()).isNotNull();
        long removedFuncId = afterFirstEdit.opers().get(0).funcs().get(1).id();
        long removedOperId = afterFirstEdit.opers().get(2).id();
        long removedSborId = afterFirstEdit.sborEds().get(2).id();
        assertThat(DocxTestSupport.manualText(puFile, "oper_" + oper1Id + "_col_0", 3))
                .contains("РУЧНАЯ-ПОМЕТКА-ПУ");
        assertThat(DocxTestSupport.manualText(fmeaFile, "func_" + func1Id + "_col_7", 8))
                .contains("РУЧНАЯ-ПОМЕТКА-FMEA");

        Path archive = packageDir.resolve("Предыдущая версия " + packageName);
        assertThat(archive).exists();
        try (XWPFDocument archived = DocxTestSupport.open(
                archive.resolve("PU" + DocxTestData.PU_NAME + ".docx"))) {
            assertThat(DocxTestSupport.bookmarkNames(archived)).contains("oper_" + oper1Id + "_col_0");
        }

        OperDto firstFinal = new OperDto(
                afterFirstEdit.opers().get(0).id(), afterFirstEdit.id(), "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(afterFirstEdit.opers().get(0).funcs().get(0),
                        afterFirstEdit.opers().get(0).funcs().get(2)));
        DockPackageDto edit2 = new DockPackageDto(
                afterFirstEdit.id(), packageName, path,
                afterFirstEdit.puName(), afterFirstEdit.spuName(), afterFirstEdit.kpName(),
                afterFirstEdit.fmeaName(), afterFirstEdit.vedIName(),
                List.of(firstFinal, afterFirstEdit.opers().get(1)),
                List.of(afterFirstEdit.sborEds().get(0), afterFirstEdit.sborEds().get(1)));

        DocPackageSaveOrchestrator.UpdatePreview preview2 =
                orchestrator.previewUpdate(edit2, saved.id(), path);
        assertThat(preview2.orderChanged()).isFalse();
        DocPackageSaveOrchestrator.UpdateOutcome outcome2 = orchestrator.updatePackage(edit2, preview2);
        assertThat(outcome2.orderChanged()).isFalse();

        DockPackageDto finalDto = service.findDtoById(saved.id());
        assertThat(finalDto.opers()).hasSize(2);
        assertThat(finalDto.opers().get(0).funcs()).hasSize(2);
        assertThat(finalDto.sborEds()).hasSize(2);
        assertThat(DocxTestSupport.manualText(puFile, "oper_" + oper1Id + "_col_0", 3))
                .contains("РУЧНАЯ-ПОМЕТКА-ПУ");
        assertThat(DocxTestSupport.manualText(fmeaFile, "func_" + func1Id + "_col_7", 8))
                .contains("РУЧНАЯ-ПОМЕТКА-FMEA");

        try (XWPFDocument pu = DocxTestSupport.open(puFile)) {
            XWPFTable table = pu.getTables().getFirst();
            assertThat(DocxTestSupport.bookmarkNames(pu))
                    .doesNotContain("oper_" + removedOperId + "_col_0",
                            "func_" + removedFuncId + "_col_4")
                    .contains("func_" + func1Id + "_col_4");
            assertThat(DocxTestSupport.cellByBookmark(table, "func_" + func1Id + "_col_4").getText())
                    .contains("Контроль диаметра NEW");
        }
        try (XWPFDocument fmea = DocxTestSupport.open(fmeaFile)) {
            assertThat(DocxTestSupport.bookmarkNames(fmea))
                    .doesNotContain("oper_" + removedOperId + "_numOper",
                            "func_" + removedFuncId + "_col_7");
        }
        assertThat(packageDir.resolve("ri/015 Сверлильная.docx")).doesNotExist();
        assertThat(packageDir.resolve("ri/005 Токарная.docx")).exists();

        try (XWPFDocument table01 = DocxTestSupport.open(packageDir.resolve("Table01.docx"))) {
            assertThat(DocxTestSupport.bookmarkNames(table01))
                    .contains("t01_name_" + sbor1Id, "t01_name_" + sbor2Id)
                    .doesNotContain("t01_name_" + removedSborId);
        }

        try (XWPFDocument archived = DocxTestSupport.open(
                archive.resolve("PU" + DocxTestData.PU_NAME + ".docx"))) {
            assertThat(DocxTestSupport.bookmarkNames(archived))
                    .contains("oper_" + removedOperId + "_col_0",
                            "func_" + removedFuncId + "_col_4");
        }
    }
}
