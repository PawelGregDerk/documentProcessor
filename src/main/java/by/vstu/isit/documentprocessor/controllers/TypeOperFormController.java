package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.repositories.TypeOperRepository;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@FxmlView("typeOper-form.fxml")
@RequiredArgsConstructor
public class TypeOperFormController {

    private final TypeOperRepository typeOperRepository;

    @FXML private TextField idField;
    @FXML private TextField numOperField;
    @FXML private TextField nomInstrField;
    @FXML private TextField oborudField;
    @FXML private TextField ostnasInstrField;
    @FXML private TextField nameField;
    @FXML private TextField shifrField;
    @FXML private TextField numZechField;

    @FXML
    private void handleSave() {
        TypeOper typeOper = new TypeOper();
        typeOper.setIdTypeOper(Long.valueOf(idField.getText()));
        typeOper.setNumOper(numOperField.getText());
        typeOper.setNomInstr(nomInstrField.getText());
        typeOper.setOborud(oborudField.getText());
        typeOper.setOstnasInstr(ostnasInstrField.getText());
        typeOper.setName(nameField.getText());
        typeOper.setShifr(shifrField.getText());
        typeOper.setNumZech(numZechField.getText());

        typeOperRepository.save(typeOper);
        clearForm();
    }

    private void clearForm() {
        idField.clear();
        numOperField.clear();
        nomInstrField.clear();
        oborudField.clear();
        ostnasInstrField.clear();
        nameField.clear();
        shifrField.clear();
        numZechField.clear();
    }
}
