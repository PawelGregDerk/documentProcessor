package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Controller
@FxmlView("type-oper-edit-view.fxml")
public class TypeOperEditController implements Initializable {

    private final TypeOperService typeOperService;

    public TypeOperEditController(TypeOperService typeOperService) {
        this.typeOperService = typeOperService;
    }

    @FXML private TextField numOperField;
    @FXML private TextField nomInstrField;
    @FXML private TextField oborudField;
    @FXML private TextField ostnasInstrField;
    @FXML private TextField nameField;
    @FXML private TextField shifrField;
    @FXML private TextField numZechField;

    @FXML private VBox funcContainer;

    private List<HBox> funcRows;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        funcRows = new ArrayList<>();

        // если нужно сразу одна строка
        addFunctionRow();
    }

    @FXML
    public void onAddFunction() {
        addFunctionRow();
    }

    private void addFunctionRow() {
        TextField name = new TextField();
        name.setPromptText("Функция");

        TextField param = new TextField();
        param.setPromptText("Параметры");

        CheckBox isProd = new CheckBox("Prod");

        TextField spec = new TextField();
        spec.setPromptText("Spec");

        Button del = new Button("X");

        HBox row = new HBox(10, name, param, isProd, spec, del);

        del.setOnAction(e -> {
            funcContainer.getChildren().remove(row);
            funcRows.remove(row);
        });

        funcRows.add(row);
        funcContainer.getChildren().add(row);
    }

    @FXML
    public void onSave() {

        TypeOper entity = new TypeOper();

        entity.setNumOper(numOperField.getText());
        entity.setNomInstr(nomInstrField.getText());
        entity.setOborud(oborudField.getText());
        entity.setOstnasInstr(ostnasInstrField.getText());
        entity.setName(nameField.getText());
        entity.setShifr(shifrField.getText());
        entity.setNumZech(numZechField.getText());

        List<TypeOperFunc> functions = new ArrayList<>();
        for (HBox row : funcRows) {
            TextField nameField = (TextField) row.getChildren().get(0);
            TextField paramField = (TextField) row.getChildren().get(1);
            CheckBox isProd = (CheckBox) row.getChildren().get(2);
            TextField specField = (TextField) row.getChildren().get(3);

            TypeOperFunc func = new TypeOperFunc();
            func.setName(nameField.getText());
            func.setParam(paramField.getText());
            func.setIsProd(isProd.isSelected());
            func.setSpecCharakt(specField.getText());
// или логика из UI

            func.setTypeOper(entity);

            functions.add(func);
        }

        entity.setTypeOperFuncs(functions);
        typeOperService.save(entity);
    }

    @FXML
    public void onCancel() {
        ((javafx.stage.Stage) numOperField.getScene().getWindow()).close();
    }
}