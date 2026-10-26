package by.vstu.isit.documentprocessor.ui;

import by.vstu.isit.documentprocessor.controllers.DocPackageCreateController;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.ui.AlertService;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocPackageCreateUiTest {

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
        DocPackageCreateController controller = new DocPackageCreateController(
                new DockPackageFormMapper(), service, typeOperService,
                List.of(), fxWeaver, alertService);
        root = FxUiTestSupport.load(
                "/by/vstu/isit/documentprocessor/controllers/docpackage-view.fxml", controller);
        stage = FxUiTestSupport.show(root);
        assemblyUnits = FxUiTestSupport.node(root, "assemblyUnitsContainer");
        operations = FxUiTestSupport.node(root, "operationsContainer");
    }

    @AfterEach
    void tearDown() {
        FxUiTestSupport.close(stage);
    }

    @Test
    @DisplayName("Реальные клики: сборочная единица, операция и функция создаются")
    void realClicks_createAssemblyOperationAndFunction() throws Exception {
        FxUiTestSupport.click(FxUiTestSupport.button(root, "Добавить описание сборочной единицы"));
        assertThat(assemblyUnits.getChildren()).hasSize(1);

        FxUiTestSupport.click(FxUiTestSupport.button(root, "Добавить операцию"));
        assertThat(operations.getChildren()).hasSize(1);
        VBox operationBlock = (VBox) operations.getChildren().get(0);
        assertThat(operationBlock.getStyleClass()).contains("operation-block");

        FxUiTestSupport.click(FxUiTestSupport.button(root, "Добавить функцию"));
        VBox funcs = (VBox) operationBlock.getChildren().get(1);
        assertThat(funcs.getChildren()).hasSize(2);
        assertThat(funcs.getChildren().get(0).getStyleClass()).contains("function-header-row");
        assertThat(funcs.getChildren().get(1).getStyleClass()).contains("function-data-row");
    }

    @Test
    @DisplayName("Реальный клик: сохранение пустой формы показывает ошибку проверки")
    void realClick_saveEmptyForm_showsValidationError() throws Exception {
        Button save = FxUiTestSupport.button(root, "Сохранить в БД");

        for (int attempt = 0; attempt < 3; attempt++) {
            FxUiTestSupport.click(save);
            if (!org.mockito.Mockito.mockingDetails(alertService).getInvocations().isEmpty()) {
                break;
            }
        }

        verify(alertService).showError(contains("сборочную единицу"));
    }
}
