package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.docx.edit.AbstractDocEditor;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import io.vavr.control.Try;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.List;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@Slf4j
@Controller
@FxmlView("docpackage-edit-view.fxml")
@RequiredArgsConstructor
@Getter
public class DocPackageEditController {
    @FXML
    private TextField packageNameField;
    @FXML
    private TextField puField;
    @FXML
    private TextField spuField;
    @FXML
    private TextField kpField;
    @FXML
    private TextField fmeaField;
    @FXML
    private TextField vedInstrField;
    @FXML
    private TextField sborEdNazv;
    @FXML
    private VBox assemblyUnitsContainer;
    @FXML
    private VBox operationsContainer;
    @Setter
    private String path;
    @Setter
    private Long packageId;

    private final DockPackageFormMapper formMapper;
    private final List<AbstractWordGenerator> generators;
    private final List<AbstractDocEditor> editors;
    private final DocpackageService service;

    @FXML
    private void onAddAssemblyUnit() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        TextField codeField = styled(new TextField(), "compact");
        codeField.setPromptText("Обозначение сборочной единицы");
        codeField.prefWidthProperty().bind(row.widthProperty().multiply(0.75));

        Button deleteButton = styled(new Button("Удалить"), "delButton");
        deleteButton.setOnAction(e -> assemblyUnitsContainer.getChildren().remove(row));

        row.getChildren().addAll(codeField, deleteButton);
        assemblyUnitsContainer.getChildren().add(row);
    }

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

    private void addFunctionRow(VBox funcsContainer) {
        // если первая функция — добавляем шапку
        if (funcsContainer.getChildren().isEmpty()) {
            HBox header = styled(new HBox(6), "function-header");
            header.getStyleClass().add("function-header-row");
            header.getChildren().addAll(
                    styled(new Label("Описание функции"), "table-header"),
                    styled(new Label("Параметры / Требования"), "table-header"),
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
            var dto = formMapper.fromGui(
                    packageId,
                    packageNameField,
                    path,
                    sborEdNazv,
                    puField,
                    spuField,
                    kpField,
                    fmeaField,
                    vedInstrField,
                    operationsContainer,
                    assemblyUnitsContainer
            );

            if (dto.sborEds().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну сборочную единицу");
            if (dto.opers().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну операцию");
            if (dto.opers().stream().anyMatch(o -> o.funcs().isEmpty())) throw new IllegalStateException("Каждая операция должна содержать хотя бы одну функцию");
            var originalDto = service.findDtoById(packageId);
            var savedDto = service.updateFullPackage(dto);
            editors.forEach(e -> Try.run(() -> e.edit(originalDto, savedDto))
                    .onFailure(ex -> log.error("Ошибка редактирования документа", ex))
                    .getOrElseThrow(ex -> new RuntimeException(ex)));
            new Alert(Alert.AlertType.INFORMATION, "Документы сохранены в папку копия_" + dto.path()).showAndWait();
        } catch (Exception ex) {
            log.error("Ошибка сохранения", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void onBack() {
        Stage currentStage = (Stage) packageNameField.getScene().getWindow();
        currentStage.close();
    }
}
