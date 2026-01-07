
package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.*;
import by.vstu.isit.documentprocessor.services.docx.FmeaWordGenerator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

import static by.vstu.isit.documentprocessor.utils.ResourceHelper.*;

@Slf4j
@Controller
@FxmlView("docpackage-view.fxml")
public class DocPackageController {
    @FXML private TextField packageNameField;
    @FXML private TextField extraField;
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
                numOper, nomInstr, oborud, ostnas, name, shifr, zech, addFuncBtn, delOperBtn
        );

        // --- контейнер функций ---
        VBox funcsContainer = styled(new VBox(4), "funcs-container");

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
        delBtn.setOnAction(e -> {
            funcsContainer.getChildren().remove(funcRow);

            // если больше нет строк функций — удаляем шапку
            boolean hasDataRows = funcsContainer.getChildren().stream()
                    .anyMatch(n -> n.getStyleClass().contains("function-data-row"));
            if (!hasDataRows) {
                funcsContainer.getChildren().removeIf(
                        n -> n.getStyleClass().contains("function-header-row")
                );
            }

        });

        funcRow.getChildren().addAll(name, param, isProd, spec, delBtn);
        funcsContainer.getChildren().add(funcRow);
    }

    @FXML
    private void onSave() {

        try {
            DockPackageDto dto = collectDtoFromGui();

            new FmeaWordGenerator().generate(
                    dto,
                    "C:/Users/UserX/IdeaProjects/_vzep/FMEA.docx",
                    "C:/Users/UserX/IdeaProjects/_vzep/FMEA_filled.docx"
            );

            new Alert(Alert.AlertType.INFORMATION, "Документ сформирован").showAndWait();

        } catch (Exception ex) {
            log.error("Ошибка генерации FEMA", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    /* =======================
       GUI → DTO
       ======================= */
    private DockPackageDto collectDtoFromGui() {

        List<OperDto> operations = new ArrayList<>();

        for (Node opNode : operationsContainer.getChildren()) {

            VBox operBlock = (VBox) opNode;
            HBox operRow = (HBox) operBlock.getChildren().get(0);
            VBox funcsBox = (VBox) operBlock.getChildren().get(1);

            List<FuncDto> funcs = new ArrayList<>();

            for (Node fn : funcsBox.getChildren()) {
                if (!fn.getStyleClass().contains("function-data-row")) {
                    continue;
                }

                HBox fr = (HBox) fn;

                funcs.add(new FuncDto(
                        ((TextField) fr.getChildren().get(0)).getText(),
                        ((TextField) fr.getChildren().get(1)).getText(),
                        ((CheckBox) fr.getChildren().get(2)).isSelected(),
                        ((TextField) fr.getChildren().get(3)).getText()
                ));
            }

            operations.add(new OperDto(
                    getText(operRow, 0),
                    getText(operRow, 1),
                    getText(operRow, 2),
                    getText(operRow, 3),
                    getText(operRow, 4),
                    getText(operRow, 5),
                    getText(operRow, 6),
                    extraField.getText(),
                    funcs
            ));
        }

        return new DockPackageDto(
                packageNameField.getText(),
                puField.getText(),
                spuField.getText(),
                kpField.getText(),
                fmeaField.getText(),
                vedInstrField.getText(),
                operations
        );
    }

    private String getText(HBox box, int index) {
        return ((TextField) box.getChildren().get(index)).getText();
    }
}