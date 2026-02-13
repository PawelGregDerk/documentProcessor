package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.docx.read.DocxPackageReader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
//import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import static by.vstu.isit.documentprocessor.utils.MessageCodes.*;
import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@Slf4j
@Controller
@FxmlView("main-view.fxml")
@RequiredArgsConstructor
public class MainController {
    private final FxWeaver fxWeaver;
    private final DockPackageGuiMapper guiMapper;
    private final DocxPackageReader reader;
    private final DocpackageService docpackageService;

    @FXML
    private VBox mainVBox;

    public void createDocPackage() {
        loadStage(DocPackageController.class, fxWeaver, mainVBox, NEW_DOC_PACKAGE);
   }

    @FXML
    public void loadFromFile() {

        try {
            var dto = docpackageService.getLastPackageDto();
            if (dto == null) {
                throw new IllegalStateException("В базе данных нет пакетов документов");
            }
            // DOCX → DTO
            // var dto = reader.read();

            // открыть форму
            DocPackageController controller = openDocPackageForm("Редактирование пакета документов");

            // DTO → GUI
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

    private DocPackageController openDocPackageForm(String title) {

        DocPackageController controller =
                fxWeaver.loadController(DocPackageController.class);

        var view = fxWeaver.loadView(DocPackageController.class);

        mainVBox.getChildren().setAll(view);

        ((Stage) mainVBox.getScene().getWindow()).setTitle(title);

        return controller;
    }

}
