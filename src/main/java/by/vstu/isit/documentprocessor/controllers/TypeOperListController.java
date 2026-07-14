package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;
import static java.util.stream.Collectors.toList;

@Slf4j
@Controller
@FxmlView("type-oper-list-view.fxml")
@RequiredArgsConstructor
public class TypeOperListController implements Initializable {

    private static Consumer<TypeOperDto> pendingAddCallback;

    public static void setOnAddCallback(Consumer<TypeOperDto> callback) {
        pendingAddCallback = callback;
    }

    public static void clearCallback() {
        pendingAddCallback = null;
    }

    @FXML
    private TextField searchField;
    @FXML
    private VBox listContainer;

    private final TypeOperService typeOperService;
    private final FxWeaver fxWeaver;

    private List<TypeOperDto> allTypeOperations;
    private List<TypeOperDto> filteredTypeOperations;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTypeOperations();
    }

    private void loadTypeOperations() {
        try {
            allTypeOperations = typeOperService.findAll();
            filteredTypeOperations = new ArrayList<>(allTypeOperations);
            displayTypeOperations(filteredTypeOperations);
        } catch (Exception e) {
            log.error("Ошибка загрузки типовых операций", e);
            allTypeOperations = new ArrayList<>();
            filteredTypeOperations = new ArrayList<>();
            displayTypeOperations(filteredTypeOperations);
        }
    }

    private void displayTypeOperations(List<TypeOperDto> operations) {
        listContainer.getChildren().clear();

        for (TypeOperDto typeOper : operations) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(Double.MAX_VALUE);

            String labelText = (typeOper.numZech() != null ? typeOper.numZech() + " " : "") +
                               (typeOper.name() != null ? typeOper.name() : "<без названия>");
            Label nameLabel = new Label(labelText);
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            Button addBtn = styled(new Button("Добавить"), "btn-primary", "btn-small");
            addBtn.setOnAction(e -> onAddTypeOper(typeOper));

            Button editBtn = styled(new Button("Редактировать"), "btn-edit", "btn-small");
            editBtn.setOnAction(e -> onEditTypeOper(typeOper));

            Button delBtn = styled(new Button("Удалить"), "btn-danger", "btn-small");
            delBtn.setOnAction(e -> onDeleteTypeOper(typeOper));

            row.getChildren().addAll(nameLabel, addBtn, editBtn, delBtn);
            listContainer.getChildren().add(row);
        }
    }

    private void onAddTypeOper(TypeOperDto typeOper) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Добавить типовую операцию?");
        confirm.setContentText(typeOper.name());
        confirm.getButtonTypes().setAll(ButtonType.OK, new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK && pendingAddCallback != null) {
                pendingAddCallback.accept(typeOper);
            }
        });
    }

    private void onEditTypeOper(TypeOperDto typeOper) {
        TypeOperEditController.prepareForEdit(typeOper);
        Parent root = fxWeaver.loadView(TypeOperEditController.class);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Редактирование типовой операции");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        searchField.clear();
        loadTypeOperations();
    }

    private void onDeleteTypeOper(TypeOperDto typeOper) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить типовую операцию?");
        confirm.setContentText("Вы уверены, что хотите удалить \"" + typeOper.name() + "\"?");
        confirm.getButtonTypes().setAll(ButtonType.OK, new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    typeOperService.delete(typeOper.id());
                    loadTypeOperations();
                } catch (Exception ex) {
                    log.error("Ошибка удаления типовой операции", ex);
                    new Alert(Alert.AlertType.ERROR, "Ошибка удаления: " + ex.getMessage()).showAndWait();
                }
            }
        });
    }

    @FXML
    private void onSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            filteredTypeOperations = new ArrayList<>(allTypeOperations);
        } else {
            filteredTypeOperations = allTypeOperations.stream()
                    .filter(to ->
                            (to.numZech() != null && to.numZech().toLowerCase().contains(searchText)) ||
                                    (to.name() != null && to.name().toLowerCase().contains(searchText))
                    )
                    .collect(toList());
        }
        displayTypeOperations(filteredTypeOperations);
    }

    @FXML
    private void onResetSearch() {
        searchField.clear();
        filteredTypeOperations = new ArrayList<>(allTypeOperations);
        displayTypeOperations(filteredTypeOperations);
    }
}
