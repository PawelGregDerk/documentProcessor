package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
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
import by.vstu.isit.documentprocessor.services.docx.write.impl.KpWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.PuWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.RiWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.SxPuWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.Table01WordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.WiWordGenerator;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;
import by.vstu.isit.documentprocessor.ui.AlertService;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxWeaver;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentPackageLifecycleTest {

    private static final Resource PU = new ClassPathResource("templates/PU.docx");
    private static final Resource FMEA = new ClassPathResource("templates/FMEA.docx");
    private static final Resource KP = new ClassPathResource("templates/KP.docx");
    private static final Resource WI = new ClassPathResource("templates/WI.docx");
    private static final Resource RI = new ClassPathResource("templates/RI.docx");
    private static final Resource SXPY = new ClassPathResource("templates/SXPU.docx");
    private static final Resource TABLE01 = new ClassPathResource("templates/Table1.docx");
    private static final String FOLDER = "existing-folder";

    @Mock
    private DockPackageFormMapper formMapper;
    @Mock
    private DockPackageGuiMapper guiMapper;
    @Mock
    private DocpackageService service;
    @Mock
    private TypeOperService typeOperService;
    @Mock
    private FxWeaver fxWeaver;
    @Mock
    private AlertService alertService;

    @TempDir
    Path tempDir;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already initialized
        }
    }

    @Test
    @DisplayName("Полный цикл: генерация, ручные пометки, архив и редактирование всех документов")
    void fullLifecycle_generatesArchivesAndEditsAllDocuments() throws Exception {
        String puPattern = pattern("PU{1}.docx");
        String fmeaPattern = pattern("FMEA{1}.docx");
        String kpPattern = pattern("КП{1}.docx");
        String wiPattern = pattern("ВИ{1}.docx");
        String riPattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/{1}/{2}.docx";
        String sxpyPattern = pattern("СХПУ{1}.docx");
        String table01Pattern = pattern("Table01.docx");

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
        ReflectionTestUtils.setField(sxPuEditor, "sxPuWordGenerator", new SxPuWordGenerator(SXPY, sxpyPattern));
        List<AbstractDocEditor> editors = List.of(
                new PuDocEditor(puPattern),
                new FmeaDocEditor(fmeaPattern),
                new KpDocEditor(kpPattern),
                new WiDocEditor(wiPattern),
                new RiDocEditor(riPattern, RI, new RiWordGenerator(RI, riPattern)),
                sxPuEditor,
                new Table01DocEditor(table01Pattern, new Table01WordGenerator(TABLE01, table01Pattern))
        );

        FuncDto product = new FuncDto(201L, "Контроль диаметра", 101L, "Ø20±0,1", true, "S");
        FuncDto process = new FuncDto(202L, "Контроль температуры", 101L, "20±2 °C", false, "");
        OperDto oper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(product, process));
        SborEdDto firstSborEd = new SborEdDto(301L, 1L, "Вал", DocxTestData.PACKAGE_NAME);
        SborEdDto secondSborEd = new SborEdDto(302L, 1L, "Втулка", "АБВГ.123456.002");
        DockPackageDto original = new DockPackageDto(
                1L, DocxTestData.PACKAGE_NAME, FOLDER,
                DocxTestData.PU_NAME, DocxTestData.SPU_NAME, DocxTestData.KP_NAME,
                DocxTestData.FMEA_NAME, DocxTestData.VED_I_NAME,
                List.of(oper), List.of(firstSborEd, secondSborEd));

        FuncDto updatedProduct = new FuncDto(
                201L, "Контроль диаметра NEW", 101L, "Ø21±0,1", true, "S");
        OperDto updatedOper = new OperDto(
                101L, 1L, "005", "15-132/005",
                "Станок 005", "Оснастка 005", "Токарная", "Т005", "15",
                List.of(updatedProduct, process));
        DockPackageDto saved = new DockPackageDto(
                1L, DocxTestData.PACKAGE_NAME, FOLDER,
                DocxTestData.PU_NAME, DocxTestData.SPU_NAME, DocxTestData.KP_NAME,
                DocxTestData.FMEA_NAME, DocxTestData.VED_I_NAME,
                List.of(updatedOper), List.of(firstSborEd, secondSborEd));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(saved);
        when(service.findDtoById(1L)).thenReturn(original, saved);
        when(service.updateFullPackage(any()))
                .thenReturn(new DocpackageService.UpdateResult(saved, false));

        Path packageDir = Files.createDirectories(tempDir.resolve(FOLDER));
        for (AbstractWordGenerator generator : generators) {
            generator.generate(original);
        }

        Path currentPu = packageDir.resolve("PU" + DocxTestData.PU_NAME + ".docx");
        Path currentFmea = packageDir.resolve("FMEA" + DocxTestData.FMEA_NAME + ".docx");
        DocxTestSupport.putManualText(currentPu, "oper_101_col_0", 3, "MANUAL-LIFECYCLE-PU");
        DocxTestSupport.putManualText(currentFmea, "func_201_col_7", 8, "MANUAL-LIFECYCLE-FMEA");

        DocPackageEditController controller = new DocPackageEditController(
                formMapper, guiMapper, editors, generators,
                service, typeOperService, fxWeaver, alertService, new PackageFileManager());
        ReflectionTestUtils.setField(controller, "outPuPath", puPattern);
        ReflectionTestUtils.setField(controller, "packageId", 1L);
        ReflectionTestUtils.setField(controller, "path", FOLDER);
        ReflectionTestUtils.setField(controller, "packageNameField", new TextField(original.packageName()));
        ReflectionTestUtils.setField(controller, "sborEdNazv", new TextField("Вал"));
        ReflectionTestUtils.setField(controller, "puField", new TextField(original.puName()));
        ReflectionTestUtils.setField(controller, "spuField", new TextField(original.spuName()));
        ReflectionTestUtils.setField(controller, "kpField", new TextField(original.kpName()));
        ReflectionTestUtils.setField(controller, "fmeaField", new TextField(original.fmeaName()));
        ReflectionTestUtils.setField(controller, "vedInstrField", new TextField(original.vedIName()));
        ReflectionTestUtils.setField(controller, "operationsContainer", new VBox());
        ReflectionTestUtils.setField(controller, "assemblyUnitsContainer", new VBox());
        ReflectionTestUtils.setField(controller, "saveBtn", new Button());
        ReflectionTestUtils.setField(controller, "updateDbOnlyBtn", new Button());

        ReflectionTestUtils.invokeMethod(controller, "onSaveOrCreate");

        verify(alertService).showInfo(contains("Документы сохранены в папку " + FOLDER));
        verify(alertService, never()).showError(any());

        Path archiveDir = packageDir.resolve("Предыдущая версия " + original.packageName());
        assertThat(archiveDir).exists();
        assertThat(archiveDir.resolve("PU" + DocxTestData.PU_NAME + ".docx")).exists();
        assertThat(archiveDir.resolve("FMEA" + DocxTestData.FMEA_NAME + ".docx")).exists();
        assertThat(archiveDir.resolve("КП" + DocxTestData.KP_NAME + ".docx")).exists();
        assertThat(archiveDir.resolve("ВИ" + DocxTestData.VED_I_NAME + ".docx")).exists();
        assertThat(archiveDir.resolve("СХПУ" + DocxTestData.SPU_NAME + ".docx")).exists();
        assertThat(archiveDir.resolve("Table01.docx")).exists();
        assertThat(archiveDir.resolve("ri").resolve("005 Токарная.docx")).exists();

        try (XWPFDocument doc = DocxTestSupport.open(currentPu)) {
            XWPFTable table = doc.getTables().getFirst();
            assertThat(DocxTestSupport.cellByBookmark(table, "func_201_col_4").getText())
                    .contains("Контроль диаметра NEW");
            assertThat(DocxTestSupport.manualText(currentPu, "oper_101_col_0", 3))
                    .contains("MANUAL-LIFECYCLE-PU");
        }

        try (XWPFDocument doc = DocxTestSupport.open(currentFmea)) {
            XWPFTable table = doc.getTables().getFirst();
            assertThat(DocxTestSupport.cellByBookmark(table, "func_201_col_7").getText())
                    .contains("Контроль диаметра NEW");
            assertThat(DocxTestSupport.manualText(currentFmea, "func_201_col_7", 8))
                    .contains("MANUAL-LIFECYCLE-FMEA");
        }

        try (XWPFDocument doc = DocxTestSupport.open(archiveDir.resolve("PU" + DocxTestData.PU_NAME + ".docx"))) {
            XWPFTable table = doc.getTables().getFirst();
            assertThat(DocxTestSupport.cellByBookmark(table, "func_201_col_4").getText())
                    .contains("Контроль диаметра")
                    .doesNotContain("NEW");
        }
    }

    private String pattern(String fileName) {
        return tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/" + fileName;
    }
}
