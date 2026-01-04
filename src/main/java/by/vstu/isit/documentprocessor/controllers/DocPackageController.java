
package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.services.interfaces.DocpackageService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;

import static by.vstu.isit.documentprocessor.utils.ResourceHelper.*;

@Slf4j
@Controller
@FxmlView("docpackage-view.fxml")
@RequiredArgsConstructor
public class DocPackageController {

    private final DocpackageService docpackageService;

    private Long currentDocpackageId; // для обновления

    @FXML private TextField packageNameField;
    @FXML private TextField puField;
    @FXML private TextField spuField;
    @FXML private TextField kpField;
    @FXML private TextField fmeaField;
    @FXML private TextField vedInstrField;

    @FXML private VBox operationsContainer;

    // ======================
    // Добавление операции
    // ======================
    @FXML
    private void onAddOperation() {

        VBox operationBlock = styled(new VBox(5), "operation-block");

        // --- строка операции ---
        HBox operRow = styled(new HBox(6), "operation-row");
        operRow.setAlignment(Pos.CENTER_LEFT);

        TextField numOper = styled(new TextField(), "compact");
        numOper.setPromptText("№ операции");

        TextField nomInstr = styled(new TextField(), "compact");
        nomInstr.setPromptText("№ инструкции");

        TextField oborud = new TextField();
        oborud.setPromptText("Оборудование");

        TextField ostnas = new TextField();
        ostnas.setPromptText("Оснастка / Инструмент");

        TextField name = new TextField();
        name.setPromptText("Наименование");

        TextField shifr = styled(new TextField(), "compact");
        shifr.setPromptText("Шифр");

        TextField zech = styled(new TextField(), "compact");
        zech.setPromptText("Цех");

        Button addFuncBtn = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        addFuncBtn.setPrefWidth(140);
        Button delOperBtn = styled(new Button("Удалить"), "btn-danger", "btn-small");
        delOperBtn.setPrefWidth(90);

        operRow.getChildren().addAll(
                numOper, nomInstr, oborud, ostnas, name, shifr, zech,
                addFuncBtn, delOperBtn
        );

        // --- контейнер функций ---
        VBox funcsContainer = styled(new VBox(4), "functions-container");

        addFuncBtn.setOnAction(e -> addFunctionRow(funcsContainer));
        delOperBtn.setOnAction(e -> operationsContainer.getChildren().remove(operationBlock));

        operationBlock.getChildren().addAll(operRow, funcsContainer);
        operationsContainer.getChildren().add(operationBlock);
    }

    // ======================
    // Добавление функции
    // ======================
    private void addFunctionRow(VBox funcsContainer) {

        // если первая функция — добавляем шапку
        if (funcsContainer.getChildren().isEmpty()) {
            HBox header = styled(new HBox(6), "function-header");
            header.getStyleClass().add("function-header-row");
            header.getChildren().addAll(
                    styled(new Label("Описание функции"), "table-header"),
                    styled( new Label("Параметры / Требования"), "table-header"),
                    styled(new Label("Продукт"), "table-header"),
                    styled(new Label("Спец. характеристики"), "table-header")
            );
            funcsContainer.getChildren().add(header);
        }

        HBox funcRow = styled(new HBox(6), "function-row");
        funcRow.getStyleClass().add("function-data-row");
        funcRow.setAlignment(Pos.CENTER_LEFT);

        TextField name = styled(new TextField(), "compact");
        name.setPromptText("Описание");

        TextField param = styled(new TextField(), "no-compact");
        param.setPromptText("Параметры");

        CheckBox isProd = new CheckBox();

        TextField spec = styled(new TextField(), "compact");
        spec.setPromptText("Спец. характеристики");

        Button delBtn = styled(new Button("Удалить"), "delButton");
        delBtn.setOnAction(e -> {funcsContainer.getChildren().remove(funcRow);});

        funcRow.getChildren().addAll(name, param, isProd, spec, delBtn);
        funcsContainer.getChildren().add(funcRow);
    }

    // ======================
    // Сохранение / Обновление
    // ======================
    @FXML
    private void onSave() {
        try {
            Docpackage docpackage = Docpackage.builder()
                    .idDocPackage(currentDocpackageId)
                    .packageName(packageNameField.getText())
                    .path(packageNameField.getText()) // временно пока не добавим путь сохранения
                    .PUName(puField.getText())
                    .SPUName(spuField.getText())
                    .KPName(kpField.getText())
                    .FEMAName(fmeaField.getText())
                    .vedIName(vedInstrField.getText())
                    .opers(collectOperations())
                    .build();
            docpackage.getOpers().forEach(oper -> oper.setDocpackage(docpackage));

            if (currentDocpackageId == null) {
                docpackageService.saveDocpackage(docpackage);
                showAlert("Успех", "Пакет документов создан успешно", Alert.AlertType.INFORMATION);
                log.info("Docpackage created successfully");
            } else {
                docpackageService.updateDocpackage(docpackage);
                showAlert("Успех", "Пакет документов обновлен успешно", Alert.AlertType.INFORMATION);
                log.info("Docpackage updated successfully");
            }
        } catch (Exception e) {
            log.error("Error saving docpackage", e);
            showAlert("Ошибка", "Не удалось сохранить пакет документов: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // cбор всех операций
    private List<Oper> collectOperations() {  
        List<Oper> opers = new ArrayList<>();
        for (Node node : operationsContainer.getChildren()) {
            if (node instanceof VBox operationBlock) {
                Oper oper = collectOperFromBlock(operationBlock);
                if (oper != null) {
                    opers.add(oper);
                }
            }
        }
        return opers;
    }

    // cбор одной операции
    private Oper collectOperFromBlock(VBox operationBlock) {
        if (operationBlock.getChildren().size() < 2) return null;

        HBox operRow = (HBox) operationBlock.getChildren().get(0);
        VBox funcsContainer = (VBox) operationBlock.getChildren().get(1);

        List<Node> fields = operRow.getChildren();
        if (fields.size() < 9) return null;

        TextField numOperField = (TextField) fields.get(0);
        TextField nomInstrField = (TextField) fields.get(1);
        TextField oborudField = (TextField) fields.get(2);
        TextField ostnasField = (TextField) fields.get(3);
        TextField nameField = (TextField) fields.get(4);
        TextField shifrField = (TextField) fields.get(5);
        TextField zechField = (TextField) fields.get(6);

        Oper oper = Oper.builder()
                .numOper(numOperField.getText())
                .nomInstr(nomInstrField.getText())
                .oborud(oborudField.getText())
                .ostnasInstr(ostnasField.getText())
                .name(nameField.getText())
                .shifr(shifrField.getText())
                .numZech(zechField.getText())
                .funcs(collectFuncsFromContainer(funcsContainer))
                .build();
        oper.getFuncs().forEach(func -> func.setOper(oper));
        return oper;
    }

    // cбор функций из контейнера
    private List<Func> collectFuncsFromContainer(VBox funcsContainer) {
        List<Func> funcs = new ArrayList<>();
        for (Node node : funcsContainer.getChildren()) {
            if (node instanceof HBox funcRow && !funcRow.getStyleClass().contains("function-header-row")) {
                Func func = collectFuncFromRow(funcRow);
                if (func != null) {
                    funcs.add(func);
                }
            }
        }
        return funcs;
    }

    // cбор функций из контейнера
    private Func collectFuncFromRow(HBox funcRow) {
        List<Node> fields = funcRow.getChildren();
        if (fields.size() < 4) return null;

        TextField nameField = (TextField) fields.get(0);
        TextField paramField = (TextField) fields.get(1);
        CheckBox isProdBox = (CheckBox) fields.get(2);
        TextField specField = (TextField) fields.get(3);

        return Func.builder()
                .name(nameField.getText())
                .param(paramField.getText())
                .isProd(isProdBox.isSelected())
                .specCharakt(specField.getText())
                .build();
    }

    // ======================
    // Загрузка для редактирования
    // ======================
    public void loadDocpackageForEdit(Long id) {
        docpackageService.findDocpackageById(id).ifPresent(docpackage -> {
            currentDocpackageId = docpackage.getIdDocPackage();
            packageNameField.setText(docpackage.getPackageName());
            puField.setText(docpackage.getPUName());
            spuField.setText(docpackage.getSPUName());
            kpField.setText(docpackage.getKPName());
            fmeaField.setText(docpackage.getFEMAName());
            vedInstrField.setText(docpackage.getVedIName());
        });
    }

    // ======================
    // Удаление
    // ======================
    public void deleteDocpackage(Long id) {
        try {
            docpackageService.deleteDocpackage(id);
            log.info("Docpackage deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting docpackage", e);
        }
    }

    // ======================
    // Получить все
    // ======================
    public List<Docpackage> getAllDocpackages() {
        return docpackageService.findAllDocpackages();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}