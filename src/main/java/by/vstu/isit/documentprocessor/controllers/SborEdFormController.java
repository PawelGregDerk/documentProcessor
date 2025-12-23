package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.SborEd;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
import by.vstu.isit.documentprocessor.repositories.SborEdRepository;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@FxmlView("sbored-form.fxml")
@RequiredArgsConstructor
public class SborEdFormController {
    private final SborEdRepository sborEdRepository;
    private final DocpackageRepository docpackageRepository;

    @FXML private TextField idField;
    @FXML private ComboBox<Docpackage> docpackageBox;
    @FXML private TextField nazvField;
    @FXML private TextField oboznachField;

    @FXML
    private void initialize() {
        //docpackageBox.getItems().addAll(docpackageRepository.findAll());

        docpackageBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Docpackage item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : item.getPackageName());
            }
        });

        docpackageBox.setButtonCell(docpackageBox.getCellFactory().call(null));
    }

    @FXML
    private void handleSave() {
        SborEd sborEd = new SborEd();

        sborEd.setId(Long.valueOf(idField.getText()));
        sborEd.setIdDocpackage(docpackageBox.getValue().getIdDocPackage());
        sborEd.setNazv(nazvField.getText());
        sborEd.setOboznach(oboznachField.getText());

   //     sborEdRepository.save(sborEd);

        clearForm();
    }

    private void clearForm() {
        idField.clear();
        docpackageBox.setValue(null);
        nazvField.clear();
        oboznachField.clear();
    }
}
