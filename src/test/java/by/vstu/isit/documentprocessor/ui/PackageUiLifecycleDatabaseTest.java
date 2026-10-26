package by.vstu.isit.documentprocessor.ui;

import by.vstu.isit.documentprocessor.controllers.DocPackageCreateController;
import by.vstu.isit.documentprocessor.controllers.DocPackageEditController;
import by.vstu.isit.documentprocessor.dto.DockPackageDto;
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
import by.vstu.isit.documentprocessor.services.docx.write.impl.PuWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.RiWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.SxPuWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.Table01WordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.impl.WiWordGenerator;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(properties = "spring.main.web-application-type=none")
@EnabledIf("databaseAvailable")
class PackageUiLifecycleDatabaseTest {

    private static final String LOCAL_URL =
            "jdbc:mysql://localhost:3306/vsep?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000";
    private static final Resource PU = new ClassPathResource("templates/PU.docx");
    private static final Resource FMEA = new ClassPathResource("templates/FMEA.docx");
    private static final Resource KP = new ClassPathResource("templates/KP.docx");
    private static final Resource WI = new ClassPathResource("templates/WI.docx");
    private static final Resource RI = new ClassPathResource("templates/RI.docx");
    private static final Resource SXPY = new ClassPathResource("templates/SXPU.docx");
    private static final Resource TABLE01 = new ClassPathResource("templates/Table1.docx");
    private static final String CREATE_FXML =
            "/by/vstu/isit/documentprocessor/controllers/docpackage-view.fxml";
    private static final String EDIT_FXML =
            "/by/vstu/isit/documentprocessor/controllers/docpackage-edit-view.fxml";

    @Autowired
    private DocpackageService service;

    @TempDir
    Path tempDir;

    private Stage createStage;
    private Stage editStage;
    private Long packageId;
    private final SilentAlerts alerts = new SilentAlerts();

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
        }
        return props;
    }

    @BeforeAll
    static void initJavaFx() {
        FxUiTestSupport.startToolkit();
    }

    @AfterEach
    void tearDown() {
        FxUiTestSupport.close(createStage);
        FxUiTestSupport.close(editStage);
        if (packageId != null) {
            try {
                service.delete(packageId);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("UI: создать пакет и трижды отредактировать с удалением операции из середины")
    void createAndEditPackageThroughUi() throws Exception {
        String marker = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String packageName = "UI-" + marker;
        String path = "ui-" + marker;
        String base = tempDir.toAbsolutePath().toString().replace('\\', '/');
        String puPattern = base + "/{0}/PU{1}.docx";
        String fmeaPattern = base + "/{0}/FMEA{1}.docx";
        String kpPattern = base + "/{0}/КП{1}.docx";
        String wiPattern = base + "/{0}/ВИ{1}.docx";
        String riPattern = base + "/{0}/{1}/{2}.docx";
        String sxpyPattern = base + "/{0}/СХПУ{1}.docx";
        String table01Pattern = base + "/{0}/Table01.docx";

        List<AbstractWordGenerator> generators = generators(
                puPattern, fmeaPattern, kpPattern, wiPattern, riPattern, sxpyPattern, table01Pattern);
        List<AbstractDocEditor> editors = editors(
                puPattern, fmeaPattern, kpPattern, wiPattern, riPattern, sxpyPattern, table01Pattern);

        FxWeaver fxWeaver = mock(FxWeaver.class);
        TypeOperService typeOperService = mock(TypeOperService.class);
        DockPackageFormMapper formMapper = new DockPackageFormMapper();
        DockPackageGuiMapper guiMapper = new DockPackageGuiMapper();

        DocPackageCreateController createController = new DocPackageCreateController(
                formMapper, service, typeOperService, generators, fxWeaver, alerts);
        ReflectionTestUtils.setField(createController, "outPuPath", puPattern);
        Parent createRoot = FxUiTestSupport.load(CREATE_FXML, createController);
        createStage = FxUiTestSupport.show(createRoot);

        setText(createRoot, "packageNameField", packageName);
        setText(createRoot, "packagePathField", path);
        setText(createRoot, "sborEdNazv", "Вал");
        setText(createRoot, "puField", DocxTestData.PU_NAME);
        setText(createRoot, "spuField", DocxTestData.SPU_NAME);
        setText(createRoot, "kpField", DocxTestData.KP_NAME);
        setText(createRoot, "fmeaField", DocxTestData.FMEA_NAME);
        setText(createRoot, "vedInstrField", DocxTestData.VED_I_NAME);

        addAssemblyUnit(createRoot, "АБВГ.123456.001");
        addAssemblyUnit(createRoot, "АБВГ.123456.002");
        addAssemblyUnit(createRoot, "АБВГ.123456.003");

        addOperation(createRoot, "005", "Токарная", "Т005",
                new F("Контроль диаметра", "Ø20±0,1", true, "S"),
                new F("Контроль температуры", "20±2 °C", false, ""));
        addOperation(createRoot, "010", "Фрезерная", "Т010",
                new F("Контроль длины", "100±0,5", true, ""),
                new F("Контроль шероховатости", "Ra 1,6", false, "K"));
        addOperation(createRoot, "015", "Сверлильная", "Т015",
                new F("Контроль отверстия", "Ø8H7", true, "S"));

        FxUiTestSupport.click(FxUiTestSupport.button(createRoot, "Создать пакет на диске и в БД"));
        await("создание пакета завершено",
                () -> !alerts.infos.isEmpty() || !alerts.errors.isEmpty());
        assertThat(alerts.errors).isEmpty();

        DockPackageDto saved = service.searchByPackageName(packageName).stream()
                .filter(dto -> dto.packageName().equals(packageName))
                .findFirst()
                .orElseThrow();
        packageId = saved.id();
        Path packageDir = tempDir.resolve(path);
        Path puFile = packageDir.resolve("PU" + DocxTestData.PU_NAME + ".docx");
        Path fmeaFile = packageDir.resolve("FMEA" + DocxTestData.FMEA_NAME + ".docx");
        assertThat(puFile).exists();
        assertThat(packageDir.resolve("ri/005 Токарная.docx")).exists();
        assertThat(packageDir.resolve("Table01.docx")).exists();

        long firstOperId = saved.opers().get(0).id();
        long firstFuncId = saved.opers().get(0).funcs().get(0).id();
        DocxTestSupport.putManualText(puFile, "oper_" + firstOperId + "_col_0", 3, "РУЧНАЯ-ПОМЕТКА-ПУ");
        DocxTestSupport.putManualText(fmeaFile, "func_" + firstFuncId + "_col_7", 8, "РУЧНАЯ-ПОМЕТКА-FMEA");
        FxUiTestSupport.close(createStage);
        createStage = null;

        DocPackageEditController editController = new DocPackageEditController(
                formMapper, guiMapper, editors, generators, service,
                typeOperService, fxWeaver, alerts, new PackageFileManager());
        ReflectionTestUtils.setField(editController, "outPuPath", puPattern);
        Parent editRoot = FxUiTestSupport.load(EDIT_FXML, editController);
        editStage = FxUiTestSupport.show(editRoot);
        ReflectionTestUtils.setField(editController, "packageId", packageId);
        ReflectionTestUtils.setField(editController, "path", path);
        FxUiTestSupport.runOnFx(() -> ReflectionTestUtils.invokeMethod(editController, "refreshFromDb"));

        VBox operations = FxUiTestSupport.node(editRoot, "operationsContainer");

        VBox firstBlock = blockByNum(operations, "005");
        VBox firstFuncs = (VBox) firstBlock.getChildren().get(1);
        FxUiTestSupport.setText(firstDataRow(firstFuncs).getChildren().get(0), "Контроль диаметра NEW");
        addFunction(firstBlock, new F("Новый контроль", "NEW", false, ""));
        saveAndWait(FxUiTestSupport.node(editRoot, "saveBtn"));

        DockPackageDto afterFirst = service.findDtoById(packageId);
        assertThat(afterFirst.opers().get(0).funcs()).hasSize(3);
        assertThat(DocxTestSupport.manualText(puFile, "oper_" + firstOperId + "_col_0", 3))
                .contains("РУЧНАЯ-ПОМЕТКА-ПУ");
        assertThat(DocxTestSupport.manualText(fmeaFile, "func_" + firstFuncId + "_col_7", 8))
                .contains("РУЧНАЯ-ПОМЕТКА-FMEA");

        VBox middleBlock = blockByNum(operations, "010");
        press((Button) ((HBox) middleBlock.getChildren().get(0)).getChildren().get(8));
        saveAndWait(FxUiTestSupport.node(editRoot, "saveBtn"));

        DockPackageDto afterMiddleRemoval = service.findDtoById(packageId);
        assertThat(afterMiddleRemoval.opers()).hasSize(2);
        assertThat(afterMiddleRemoval.opers())
                .extracting(oper -> oper.numOper())
                .containsExactly("005", "015");
        try (XWPFDocument pu = DocxTestSupport.open(puFile);
             XWPFDocument fmea = DocxTestSupport.open(fmeaFile)) {
            assertThat(DocxTestSupport.bodyContains(pu, "РУЧНАЯ-ПОМЕТКА-ПУ")).isFalse();
            assertThat(DocxTestSupport.bodyContains(fmea, "РУЧНАЯ-ПОМЕТКА-FMEA")).isFalse();
        }
        assertThat(packageDir.resolve("ri/010 Фрезерная.docx")).doesNotExist();
        assertThat(packageDir.resolve("Предыдущая версия " + packageName)).exists();

        long cycle3OperId = afterMiddleRemoval.opers().get(0).id();
        long cycle3FuncId = afterMiddleRemoval.opers().get(0).funcs().get(0).id();
        DocxTestSupport.putManualText(puFile, "oper_" + cycle3OperId + "_col_0", 3, "РУЧНАЯ-ПОМЕТКА-ЦИКЛ-3");
        DocxTestSupport.putManualText(fmeaFile, "func_" + cycle3FuncId + "_col_7", 8, "РУЧНАЯ-ПОМЕТКА-ЦИКЛ-3");
        press(FxUiTestSupport.button(editRoot, "Добавить операцию"));
        VBox addedBlock = lastBlock(operations);
        fillOperation(addedBlock, "020", "Сверлильная", "Т020");
        addFunction(addedBlock, new F("Контроль нового отверстия", "Ø10H7", true, "S"));
        saveAndWait(FxUiTestSupport.node(editRoot, "saveBtn"));

        DockPackageDto afterThirdEdit = service.findDtoById(packageId);
        assertThat(afterThirdEdit.opers()).hasSize(3);
        assertThat(DocxTestSupport.manualText(puFile, "oper_" + cycle3OperId + "_col_0", 3))
                .contains("РУЧНАЯ-ПОМЕТКА-ЦИКЛ-3");
        assertThat(DocxTestSupport.manualText(fmeaFile, "func_" + cycle3FuncId + "_col_7", 8))
                .contains("РУЧНАЯ-ПОМЕТКА-ЦИКЛ-3");
        assertThat(packageDir.resolve("ri/020 Сверлильная.docx")).exists();
        assertThat(alerts.errors).isEmpty();
    }

    private void saveAndWait(Node button) throws Exception {
        int infosBefore = alerts.infos.size();
        int errorsBefore = alerts.errors.size();
        FxUiTestSupport.click(button);
        await("сохранение завершено",
                () -> alerts.infos.size() > infosBefore || alerts.errors.size() > errorsBefore);
        assertThat(alerts.errors).hasSize(errorsBefore);
    }

    private void await(String description, BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Не дождались: " + description + ". Ошибки: " + alerts.errors);
    }

    private void press(Button button) {
        FxUiTestSupport.runOnFx(button::fire);
    }

    private void setText(Parent root, String id, String text) {
        FxUiTestSupport.setText(FxUiTestSupport.node(root, id), text);
    }

    private void addAssemblyUnit(Parent root, String code) throws Exception {
        FxUiTestSupport.click(FxUiTestSupport.button(root, "Добавить описание сборочной единицы"));
        VBox container = FxUiTestSupport.node(root, "assemblyUnitsContainer");
        HBox row = (HBox) container.getChildren().getLast();
        FxUiTestSupport.setText(row.getChildren().get(0), code);
    }

    private void addOperation(Parent root, String numOper, String name, String shifr, F... functions)
            throws Exception {
        FxUiTestSupport.click(FxUiTestSupport.button(root, "Добавить операцию"));
        VBox container = FxUiTestSupport.node(root, "operationsContainer");
        VBox block = lastBlock(container);
        fillOperation(block, numOper, name, shifr);
        for (F function : functions) {
            addFunction(block, function);
        }
    }

    private void fillOperation(VBox block, String numOper, String name, String shifr) {
        HBox row = (HBox) block.getChildren().get(0);
        FxUiTestSupport.setText(row.getChildren().get(0), numOper);
        FxUiTestSupport.setText(row.getChildren().get(1), "15-132/" + numOper);
        FxUiTestSupport.setText(row.getChildren().get(2), "Станок " + numOper);
        FxUiTestSupport.setText(row.getChildren().get(3), "Оснастка " + numOper);
        FxUiTestSupport.setText(row.getChildren().get(4), name);
        FxUiTestSupport.setText(row.getChildren().get(5), shifr);
        FxUiTestSupport.setText(row.getChildren().get(6), "15");
    }

    private void addFunction(VBox block, F function) throws Exception {
        press(FxUiTestSupport.button(block, "Добавить функцию"));
        VBox funcs = (VBox) block.getChildren().get(1);
        HBox row = lastDataRow(funcs);
        FxUiTestSupport.setText(row.getChildren().get(0), function.name());
        FxUiTestSupport.setText(row.getChildren().get(1), function.param());
        FxUiTestSupport.setText(row.getChildren().get(3), function.spec());
        StackPane checkWrap = (StackPane) row.getChildren().get(2);
        FxUiTestSupport.setSelected(checkWrap.getChildren().get(0), function.product());
    }

    private VBox blockByNum(VBox container, String numOper) {
        return container.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("operation-block"))
                .map(VBox.class::cast)
                .filter(block -> ((TextArea) ((HBox) block.getChildren().get(0))
                        .getChildren().get(0)).getText().equals(numOper))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Операция не найдена: " + numOper));
    }

    private VBox lastBlock(VBox container) {
        return container.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("operation-block"))
                .map(VBox.class::cast)
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    private HBox firstDataRow(VBox funcs) {
        return funcs.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("function-data-row"))
                .map(HBox.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private HBox lastDataRow(VBox funcs) {
        return funcs.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("function-data-row"))
                .map(HBox.class::cast)
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    private List<AbstractWordGenerator> generators(String pu, String fmea, String kp, String wi,
                                                   String ri, String sxpy, String table01) {
        return List.of(
                new PuWordGenerator(PU, pu),
                new FmeaWordGenerator(FMEA, fmea),
                DocxTestSupport.kpGenerator(KP, kp),
                new WiWordGenerator(WI, wi),
                new RiWordGenerator(RI, ri),
                new SxPuWordGenerator(SXPY, sxpy),
                new Table01WordGenerator(TABLE01, table01)
        );
    }

    private List<AbstractDocEditor> editors(String pu, String fmea, String kp, String wi,
                                            String ri, String sxpy, String table01) {
        SxPuDocEditor sxPuEditor = new SxPuDocEditor(sxpy);
        ReflectionTestUtils.setField(sxPuEditor, "sxPuWordGenerator", new SxPuWordGenerator(SXPY, sxpy));
        return List.of(
                new PuDocEditor(pu),
                new FmeaDocEditor(fmea),
                new KpDocEditor(kp),
                new WiDocEditor(wi),
                new RiDocEditor(ri, RI, new RiWordGenerator(RI, ri)),
                sxPuEditor,
                new Table01DocEditor(table01, new Table01WordGenerator(TABLE01, table01))
        );
    }

    private record F(String name, String param, boolean product, String spec) {
    }

    private static class SilentAlerts implements AlertService {
        private final List<String> errors = new CopyOnWriteArrayList<>();
        private final List<String> infos = new CopyOnWriteArrayList<>();

        @Override
        public void showError(String message) {
            errors.add(message);
        }

        @Override
        public void showInfo(String message) {
            infos.add(message);
        }

        @Override
        public boolean confirm(String title, String header, String content) {
            return true;
        }
    }
}
