package by.vstu.isit.documentprocessor.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
import by.vstu.isit.documentprocessor.repositories.OperRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;

@Slf4j
@Controller
@FxmlView("oper-form.fxml")
@RequiredArgsConstructor
public class OperFormController {

    private final OperRepository operRepository;
    private final DocpackageRepository docpackageRepository;

    @FXML private TextField idOperField;
    @FXML private ComboBox<Docpackage> docPackageBox;
    @FXML private TextField numOperField;
    @FXML private TextField nomInstrField;
    @FXML private TextArea oborudField;
    @FXML private TextArea ostnasInstrField;
    @FXML private TextField nameField;
    @FXML private TextField shifrField;
    @FXML private TextField numZechField;

    @FXML
    private void initialize() {
   //     docPackageBox.getItems().addAll(docpackageRepository.findAll());

        docPackageBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Docpackage item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : item.getPackageName());
            }
        });

        docPackageBox.setButtonCell(docPackageBox.getCellFactory().call(null));
    }

    @FXML
    private void handleSave() {
        Oper oper = new Oper();

        oper.setIdOper(Long.valueOf(idOperField.getText()));

        Docpackage selected = docPackageBox.getValue();
        oper.setIdDocPackage(selected != null ? selected.getIdDocPackage() : null);

        oper.setNumOper(numOperField.getText());
        oper.setNomInstr(nomInstrField.getText());
        oper.setOborud(oborudField.getText());
        oper.setOstnasInstr(ostnasInstrField.getText());
        oper.setName(nameField.getText());
        oper.setShifr(shifrField.getText());
        oper.setNumZech(numZechField.getText());

    //    operRepository.save(oper);

        clearForm();
    }

    private void clearForm() {
        idOperField.clear();
        docPackageBox.setValue(null);
        numOperField.clear();
        nomInstrField.clear();
        oborudField.clear();
        ostnasInstrField.clear();
        nameField.clear();
        shifrField.clear();
        numZechField.clear();
    }
}
