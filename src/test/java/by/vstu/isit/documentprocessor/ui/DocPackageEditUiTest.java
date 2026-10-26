package by.vstu.isit.documentprocessor.ui;

import by.vstu.isit.documentprocessor.controllers.DocPackageEditController;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocPackageEditUiTest {

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

    private Parent root;
    private Stage stage;
    private VBox assemblyUnits;
    private VBox operations;

    @BeforeAll
    static void initJavaFx() {
        FxUiTestSupport.startToolkit();
    }

    @BeforeEach
    void setUp() throws Exception {
        DocPackageEditController controller = new DocPackageEditController(
                new DockPackageFormMapper(), guiMapper, List.of(), List.of(),
                service, typeOperService, fxWeaver, alertService, new PackageFileManager());
        root = FxUiTestSupport.load(
                "/by/vstu/isit/documentprocessor/controllers/docpackage-edit-view.fxml", controller);
        stage = FxUiTestSupport.show(root);
        assemblyUnits = FxUiTestSupport.node(root, "assemblyUnitsContainer");
        operations = FxUiTestSupport.node(root, "operationsContainer");
    }

    @AfterEach
    void tearDown() {
        FxUiTestSupport.close(stage);
    }

    @Test
    @DisplayName("Реальные клики: в редактировании создаются сборочная единица, операция и функция")
    void realClicks_createAssemblyOperationAndFunction() throws Exception {
        int assembliesBefore = assemblyUnits.getChildren().size();
        FxUiTestSupport.clickUntil(
                FxUiTestSupport.button(root, "Добавить описание сборочной единицы"),
                () -> assemblyUnits.getChildren().size() > assembliesBefore);
        assertThat(assemblyUnits.getChildren()).hasSize(1);

        int operationsBefore = operations.getChildren().size();
        FxUiTestSupport.clickUntil(
                FxUiTestSupport.button(root, "Добавить операцию"),
                () -> operations.getChildren().size() > operationsBefore);
        assertThat(operations.getChildren()).hasSize(2);

        VBox operationBlock = operations.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("operation-block"))
                .map(node -> (VBox) node)
                .findFirst()
                .orElseThrow();
        VBox funcs = (VBox) operationBlock.getChildren().get(1);
        int funcsBefore = funcs.getChildren().size();
        FxUiTestSupport.clickUntil(
                FxUiTestSupport.button(root, "Добавить функцию"),
                () -> funcs.getChildren().size() > funcsBefore);
        assertThat(funcs.getChildren()).hasSize(2);
    }

    @Test
    @DisplayName("Повторное открытие окна: операции добавляются в новое окно")
    void reopen_usesNewOperationsContainer() throws Exception {
        DocPackageEditController shared = new DocPackageEditController(
                new DockPackageFormMapper(), guiMapper, List.of(), List.of(),
                service, typeOperService, fxWeaver, alertService, new PackageFileManager());
        Parent first = FxUiTestSupport.load(
                "/by/vstu/isit/documentprocessor/controllers/docpackage-edit-view.fxml", shared);
        Stage firstStage = FxUiTestSupport.show(first);
        VBox firstOps = FxUiTestSupport.node(first, "operationsContainer");
        FxUiTestSupport.runOnFx(() -> ReflectionTestUtils.invokeMethod(shared, "onAddOperation"));
        int firstBlocks = operationBlocks(firstOps);

        Parent second = FxUiTestSupport.load(
                "/by/vstu/isit/documentprocessor/controllers/docpackage-edit-view.fxml", shared);
        Stage secondStage = FxUiTestSupport.show(second);
        VBox secondOps = FxUiTestSupport.node(second, "operationsContainer");
        FxUiTestSupport.runOnFx(() -> ReflectionTestUtils.invokeMethod(shared, "onAddOperation"));

        assertThat(operationBlocks(secondOps)).isEqualTo(1);
        assertThat(operationBlocks(firstOps)).isEqualTo(firstBlocks);

        FxUiTestSupport.runOnFx(() -> ReflectionTestUtils.invokeMethod(shared, "rebuildSeparators"));
        AtomicBoolean hasSeparator = new AtomicBoolean();
        FxUiTestSupport.runOnFx(() -> hasSeparator.set(secondOps.getChildren().stream()
                .filter(HBox.class::isInstance)
                .anyMatch(node -> node.lookupAll(".button").stream()
                        .anyMatch(button -> button instanceof Button btn
                                && "Добавить операцию".equals(btn.getText())))));
        assertThat(hasSeparator).isTrue();

        FxUiTestSupport.close(secondStage);
        FxUiTestSupport.close(firstStage);
    }

    private static int operationBlocks(VBox container) {
        AtomicInteger count = new AtomicInteger();
        FxUiTestSupport.runOnFx(() -> count.set((int) container.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("operation-block"))
                .count()));
        return count.get();
    }

    @Test
    @DisplayName("Реальный клик: обновление пустой формы показывает ошибку проверки")
    void realClick_updateEmptyForm_showsValidationError() throws Exception {
        Button update = FxUiTestSupport.node(root, "updateDbOnlyBtn");

        for (int attempt = 0; attempt < 3; attempt++) {
            FxUiTestSupport.click(update);
            if (!org.mockito.Mockito.mockingDetails(alertService).getInvocations().isEmpty()) {
                break;
            }
        }

        verify(alertService).showError(contains("сборочную единицу"));
    }
}
