package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.List;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@Controller
@FxmlView("package-list-view.fxml")
@RequiredArgsConstructor
public class PackageListController {
    private final FxWeaver fxWeaver;
    private final DocpackageService docpackageService;
    private final DockPackageGuiMapper guiMapper;

    @FXML
    private VBox packagesContainer;

    @FXML
    public void initialize() {
        loadPackages();
    }

    private void loadPackages() {
        packagesContainer.getChildren().clear();
        List<DockPackageDto> packages = docpackageService.searchByPackageName("");
        for (DockPackageDto pkg : packages) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");

            Label nameLabel = new Label(pkg.packageName());
            nameLabel.setPrefWidth(400);
            nameLabel.setStyle("-fx-font-size: 14px;");

            Button openButton = new Button("Открыть");
            openButton.getStyleClass().add("newButton");
            openButton.setOnAction(e -> openPackage(pkg));

            Button deleteButton = new Button("Удалить");
            deleteButton.getStyleClass().add("delButton");
            deleteButton.setOnAction(e -> deletePackage(pkg));

            row.getChildren().addAll(nameLabel, openButton, deleteButton);
            packagesContainer.getChildren().add(row);
        }
    }

    private void openPackage(DockPackageDto dto) {
        Stage currentStage = (Stage) packagesContainer.getScene().getWindow();
        Stage mainStage = (Stage) currentStage.getOwner();
        currentStage.hide();

        DocPackageEditController controller = fxWeaver.loadController(DocPackageEditController.class);
        Pane view = fxWeaver.loadView(DocPackageEditController.class);

        controller.setPath(dto.path());
        controller.setPackageId(dto.id());
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
        Stage editStage = new Stage();
        editStage.initOwner(mainStage);
        editStage.setOnHidden(e -> {
            loadPackages();
            currentStage.show();
        });
        editStage.setScene(createScreenRatioScene(view));
        editStage.setTitle("Редактирование пакета документов");
        editStage.setResizable(false);
        editStage.show();
    }

    private void deletePackage(DockPackageDto dto) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение удаления");
        confirmAlert.setHeaderText("Вы уверены?");
        confirmAlert.setContentText("Удалить пакет документов \"" + dto.packageName() + "\"?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    docpackageService.getRepository().deleteById(dto.id());
                    loadPackages();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка при удалении: " + ex.getMessage()).showAndWait();
                }
            }
        });
    }
}
