package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.docx.DocxDocumentService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@FxmlView("docpackage-create-view.fxml")
public class DocPackageCreateController extends DocPackageBaseController {

    @FXML
    private TextField packagePathField;

    public DocPackageCreateController(
            DockPackageFormMapper formMapper,
            List<DocxDocumentService> services
    ) {
        super(formMapper, services);
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
    }
}

