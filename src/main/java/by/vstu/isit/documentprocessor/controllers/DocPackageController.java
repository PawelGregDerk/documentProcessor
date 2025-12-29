
package by.vstu.isit.documentprocessor.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import static by.vstu.isit.documentprocessor.utils.ResourceHelper.*;

@Slf4j
@Controller
@FxmlView("docpackage-view.fxml")
public class DocPackageController {

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
        delBtn.setOnAction(e -> {
            funcsContainer.getChildren().remove(funcRow);

            // если больше нет строк функций — удаляем шапку
            boolean hasDataRows = funcsContainer.getChildren().stream()
                    .anyMatch(n -> n.getStyleClass().contains("function-data-row"));

            if (!hasDataRows) {
                funcsContainer.getChildren().removeIf(n -> n.getStyleClass().contains("function-header-row"));
            }
        });

        funcRow.getChildren().addAll(name, param, isProd, spec, delBtn);
        funcsContainer.getChildren().add(funcRow);
    }

    // ======================
    // Сохранение (заглушка)
    // ======================
    @FXML
    private void onSave() {
        System.out.println("Сохранение пакета документов");
        // здесь позже будет сбор данных + JPA
    }
}