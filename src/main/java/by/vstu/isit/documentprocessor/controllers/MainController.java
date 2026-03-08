package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.docx.read.DocxPackageReader;
import by.vstu.isit.documentprocessor.utils.GlobalConsts;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Slf4j
@Controller
@FxmlView("main-view.fxml")
@RequiredArgsConstructor
public class MainController {
    private final FxWeaver fxWeaver;
    private final DockPackageGuiMapper guiMapper;
    private final DocxPackageReader reader;

    @FXML
    private VBox mainVBox;

    public void createDocPackage() {
        openDocPackageForm(DocPackageCreateController.class, "Создание пакета документов");
    }

    @FXML
    public void loadFromFile() {
        try {
            // DOCX → DTO
            var dto = reader.read();

            // открыть форму
            DocPackageEditController controller = openDocPackageForm(
                    DocPackageEditController.class,
                    "Редактирование пакета документов"
            );
            controller.setPath(dto.path());
            controller.setOriginalDto(dto);
            guiMapper.toGui(
                    dto,
                    controller.getPackageNameField(),
                    controller.getSborEdNazv(),
                    controller.getPuField(),
                    controller.getSpuField(),
                    controller.getKpField(),
                    controller.getFmeaField(),
                    controller.getVedInstrField(),
                    controller.getOperationsContainer(),
                    controller.getAssemblyUnitsContainer()
            );

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private <T> T openDocPackageForm(Class<T> controllerClass, String title) {
        Stage parentStage = (Stage) mainVBox.getScene().getWindow();
        parentStage.hide();

        T controller = fxWeaver.loadController(controllerClass);
        Pane view = fxWeaver.loadView(controllerClass);

        Stage childStage = new Stage();
        childStage.initOwner(parentStage);
        childStage.setOnHidden(e -> parentStage.show());
        childStage.setScene(new Scene(view, 1280, 1024));
        childStage.setTitle(title);
        childStage.setResizable(false);
        addIcon(childStage, controllerClass);
        childStage.show();

        return controller;
    }

    private <T> void addIcon(Stage stage, Class<T> tClass) {
        var iconPath = GlobalConsts.getICON_PATH();
        stage.getIcons().add(new Image(Objects.requireNonNull(tClass.getResourceAsStream(iconPath))));
    }
}
