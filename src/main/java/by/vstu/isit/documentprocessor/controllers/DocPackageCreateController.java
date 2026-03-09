package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.docx.DocxDocumentService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@FxmlView("docpackage-create-view.fxml")
public class DocPackageCreateController extends DocPackageBaseController {
    private final DocpackageService docpackageService;
    @FXML
    private TextField packagePathField;

    public DocPackageCreateController(
            DockPackageFormMapper formMapper,
            List<DocxDocumentService> services,
            DocpackageService docpackageService
    ) {
        super(formMapper, services);
        this.docpackageService = docpackageService;
    }

    @Override
    protected void onSave() {
        dto = formMapper.fromGui(
                packageNameField,
                packagePathField.getText(),
                sborEdNazv,
                puField,
                spuField,
                kpField,
                fmeaField,
                vedInstrField,
                operationsContainer,
                assemblyUnitsContainer
        );
        save();
        try {
            docpackageService.saveFullPackage(dto);
            new Alert(Alert.AlertType.INFORMATION, "Документ сохранён в БД").showAndWait();
        } catch (Exception ex) {
            log.error("Ошибка сохранения в БД", ex);
        }
    }
}

