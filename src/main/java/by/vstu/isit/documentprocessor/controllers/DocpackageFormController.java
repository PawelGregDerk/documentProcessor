package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@FxmlView("docpackage-form.fxml")
@RequiredArgsConstructor
public class DocpackageFormController {
    private final DocpackageRepository repository;

    @FXML private TextField idField;
    @FXML private TextField packageNameField;
    @FXML private TextField pathField;
    @FXML private TextField puNameField;
    @FXML private TextField spuNameField;
    @FXML private TextField kpNameField;
    @FXML private TextField fmeaNameField;
    @FXML private TextField vedINameField;

    @FXML
    private void handleSave() {
        Docpackage docpackage = new Docpackage();

        docpackage.setIdDocPackage(Long.valueOf(idField.getText()));
        docpackage.setPackageName(packageNameField.getText());
        docpackage.setPath(pathField.getText());
        docpackage.setPUName(puNameField.getText());
        docpackage.setSPUName(spuNameField.getText());
        docpackage.setKPName(kpNameField.getText());
        docpackage.setFEMAName(fmeaNameField.getText());
        docpackage.setVedIName(vedINameField.getText());

      //  repository.save(docpackage);

        clearForm();
    }

    private void clearForm() {
        idField.clear();
        packageNameField.clear();
        pathField.clear();
        puNameField.clear();
        spuNameField.clear();
        kpNameField.clear();
        fmeaNameField.clear();
        vedINameField.clear();
    }
}
