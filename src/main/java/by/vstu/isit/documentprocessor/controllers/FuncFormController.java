package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.repositories.OperRepository;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;
import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.repositories.FuncRepository;
import javafx.fxml.FXML;

@Slf4j
@Controller
@FxmlView("func-form.fxml")
@RequiredArgsConstructor
public class FuncFormController {

    private final FuncRepository funcRepository;
    private final OperRepository operRepository;

    @FXML private TextField idFuncField;
    @FXML private ComboBox<Oper> operBox;
    @FXML private TextArea nameField;
    @FXML private TextArea paramField;
    @FXML private CheckBox isProdCheckBox;
    @FXML private TextField specCharaktField;

    @FXML
    private void initialize() {
//        operBox.getItems().addAll(operRepository.findAll());

        operBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Oper item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : item.getNumOper() + " — " + item.getName());
            }
        });

        operBox.setButtonCell(operBox.getCellFactory().call(null));
    }

    @FXML
    private void handleSave() {
        Func func = new Func();

        func.setIdFunc(Long.valueOf(idFuncField.getText()));
        func.setIdOper(operBox.getValue().getIdOper());
        func.setName(nameField.getText());
        func.setParam(paramField.getText());
        func.setIsProd(isProdCheckBox.isSelected());
        func.setSpecCharakt(specCharaktField.getText());

    //    funcRepository.save(func);

        clearForm();
    }

    private void clearForm() {
        idFuncField.clear();
        operBox.setValue(null);
        nameField.clear();
        paramField.clear();
        isProdCheckBox.setSelected(false);
        specCharaktField.clear();
    }
}
