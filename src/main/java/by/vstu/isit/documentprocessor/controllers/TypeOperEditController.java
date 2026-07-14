package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.utils.FileUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
@Controller
@FxmlView("type-oper-edit-view.fxml")
public class TypeOperEditController implements Initializable {

    private static TypeOperDto pendingEditData;

    public static void prepareForEdit(TypeOperDto dto) {
        pendingEditData = dto;
    }

    public static void prepareForCreate() {
        pendingEditData = null;
    }

    private final TypeOperService typeOperService;

    public TypeOperEditController(TypeOperService typeOperService) {
        this.typeOperService = typeOperService;
    }

    @FXML private TextArea numOperField;
    @FXML private TextArea nomInstrField;
    @FXML private TextArea oborudField;
    @FXML private TextArea ostnasInstrField;
    @FXML private TextArea nameField;
    @FXML private TextArea shifrField;
    @FXML private TextArea numZechField;

    @FXML private VBox funcContainer;
    @FXML private HBox funcHeader;

    private List<HBox> funcRows;
    private Long editId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        funcRows = new ArrayList<>();

        Platform.runLater(() -> {
            var stage = (javafx.stage.Stage) numOperField.getScene().getWindow();
            stage.setOnShown(e -> {
                stage.setMinWidth(stage.getWidth());
                stage.setMinHeight(stage.getHeight());
            });
        });

        addFunctionHeader();

        if (pendingEditData != null) {
            populateFromDto(pendingEditData);
            pendingEditData = null;
        } else {
            editId = null;
            addFunctionRow();
        }
    }

    private void addFunctionHeader() {
        funcHeader.setPadding(new Insets(2, 0, 2, 0));

        Label lblName = styled(new Label("Описание функции"), "table-header");
        Label lblParam = styled(new Label("Параметры"), "table-header");
        Label lblProd = styled(new Label("Продукт"), "table-header");
        Label lblSpec = styled(new Label("Спец. характеристики"), "table-header");
        Region gap = new Region();

        funcHeader.getChildren().addAll(lblName, lblParam, lblProd, lblSpec, gap);

        setGrow(lblName, 160);
        setGrow(lblParam, 160);
        setFixedWidth(lblProd, 70);
        setGrow(lblSpec, 140);
        setFixedWidth(gap, 90);
    }

    private void populateFromDto(TypeOperDto dto) {
        editId = dto.id();
        numOperField.setText(dto.numOper());
        nomInstrField.setText(dto.nomInstr());
        oborudField.setText(dto.oborud());
        ostnasInstrField.setText(dto.ostnasInstr());
        nameField.setText(dto.name());
        shifrField.setText(dto.shifr());
        numZechField.setText(dto.numZech());

        if (dto.funcs() != null) {
            for (TypeOperFuncDto funcDto : dto.funcs()) {
                addFunctionRow(funcDto);
            }
        }
    }

    @FXML
    public void onAddFunction() {
        addFunctionRow();
    }

    private void addFunctionRow() {
        addFunctionRow(null);
    }

    private void addFunctionRow(TypeOperFuncDto funcDto) {
        TextArea name = styled(new TextArea(), "compact");
        name.setPromptText("Описание функции");
        name.setPrefRowCount(2);
        name.setWrapText(true);
        if (funcDto != null) name.setText(funcDto.name());

        TextArea param = styled(new TextArea(), "compact");
        param.setPromptText("Параметры");
        param.setPrefRowCount(2);
        param.setWrapText(true);
        if (funcDto != null) param.setText(funcDto.param());

        CheckBox isProd = new CheckBox();
        if (funcDto != null) isProd.setSelected(funcDto.isProd());
        StackPane checkWrap = new StackPane(isProd);
        checkWrap.setAlignment(Pos.CENTER);

        Label prodLabel = new Label("Prod");
        prodLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        VBox prodBox = new VBox(2, prodLabel, checkWrap);
        prodBox.setAlignment(Pos.CENTER);

        TextArea spec = styled(new TextArea(), "compact");
        spec.setPromptText("Spec");
        spec.setPrefRowCount(2);
        spec.setWrapText(true);
        if (funcDto != null) spec.setText(funcDto.specCharakt());

        Button del = styled(new Button("Удалить"), "btn-danger", "btn-small");

        HBox row = new HBox(10, name, param, prodBox, spec, del);
        row.setAlignment(Pos.CENTER_LEFT);

        setGrow(name, 160);
        setGrow(param, 160);
        setGrow(spec, 140);
        setFixedWidth(prodBox, 70);
        setFixedWidth(del, 90);

        del.setOnAction(e -> {
            funcContainer.getChildren().remove(row);
            funcRows.remove(row);
        });

        funcRows.add(row);
        funcContainer.getChildren().add(row);
    }

    @FXML
    public void onSave() {
        try {
            if (numOperField.getText().isBlank()) throw new IllegalArgumentException("Поле 'Номер операции' не заполнено");
            if (nomInstrField.getText().isBlank()) throw new IllegalArgumentException("Поле 'Номенклатура инструкции' не заполнено");
            if (oborudField.getText().isBlank()) throw new IllegalArgumentException("Поле 'Оборудование' не заполнено");
            if (ostnasInstrField.getText().isBlank()) throw new IllegalArgumentException("Поле 'Оснастка/инструмент' не заполнено");
            if (nameField.getText().isBlank()) throw new IllegalArgumentException("Поле 'Название' не заполнено");
            if (shifrField.getText().isBlank()) throw new IllegalArgumentException("Поле 'Шифр' не заполнено");
            if (numZechField.getText().isBlank()) throw new IllegalArgumentException("Поле 'Номер цеха' не заполнено");
            if (funcRows.isEmpty()) throw new IllegalArgumentException("Не добавлено ни одной функции");

            TypeOper entity;
            if (editId != null) {
                entity = typeOperService.getById(editId);
                entity.getTypeOperFuncs().clear();
            } else {
                entity = new TypeOper();
            }

            entity.setNumOper(FileUtils.sanitize(numOperField.getText()));
            entity.setNomInstr(nomInstrField.getText());
            entity.setOborud(oborudField.getText());
            entity.setOstnasInstr(ostnasInstrField.getText());
            entity.setName(FileUtils.sanitize(nameField.getText()));
            entity.setShifr(shifrField.getText());
            entity.setNumZech(numZechField.getText());

            List<TypeOperFunc> functions = new ArrayList<>();
            for (HBox row : funcRows) {
                TextArea nameField = (TextArea) row.getChildren().get(0);
                TextArea paramField = (TextArea) row.getChildren().get(1);
                VBox prodBox = (VBox) row.getChildren().get(2);
                StackPane checkWrap = (StackPane) prodBox.getChildren().get(1);
                CheckBox isProd = (CheckBox) checkWrap.getChildren().get(0);
                TextArea specField = (TextArea) row.getChildren().get(3);

                TypeOperFunc func = new TypeOperFunc();
                func.setName(nameField.getText());
                func.setParam(paramField.getText());
                func.setIsProd(isProd.isSelected());
                func.setSpecCharakt(specField.getText());
                func.setTypeOper(entity);

                functions.add(func);
            }

            entity.setTypeOperFuncs(functions);
            typeOperService.save(entity);

            String message = editId != null ? "Типовая операция обновлена" : "Типовая операция создана";
            new Alert(Alert.AlertType.INFORMATION, message).showAndWait();

            ((javafx.stage.Stage) numOperField.getScene().getWindow()).close();
        } catch (Exception e) {
            String msg = editId != null ? "Ошибка при обновлении типовой операции" : "Ошибка при создании типовой операции";
            new Alert(Alert.AlertType.ERROR, msg + ":\n" + e.getMessage()).showAndWait();
            log.error("Ошибка сохранения типовой операции", e);
        }
    }

    @FXML
    public void onCancel() {
        ((javafx.stage.Stage) numOperField.getScene().getWindow()).close();
    }

    private void setGrow(Region region, double minWidth) {
        region.setMinWidth(minWidth);
        region.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(region, Priority.ALWAYS);
    }

    private void setFixedWidth(Region region, double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }
}
