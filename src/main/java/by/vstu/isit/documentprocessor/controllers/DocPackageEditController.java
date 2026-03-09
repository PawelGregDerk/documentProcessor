package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.docx.DocxDocumentService;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@FxmlView("docpackage-edit-view.fxml")
public class DocPackageEditController extends DocPackageBaseController {
    @Setter
    private String path;

    public DocPackageEditController(
            DockPackageFormMapper formMapper,
            List<DocxDocumentService> services
    ) {
        super(formMapper, services);
    }

    @Override
    protected void onSave() {
        dto = formMapper.fromGui(
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
        save();
    }

    public void setOriginalDto(by.vstu.isit.documentprocessor.dto.DockPackageDto originalDto) {
        this.originalDto = originalDto;
    }
    
    @FXML
    protected void onBack() {
        Stage currentStage = (Stage) packageNameField.getScene().getWindow();
        currentStage.close();
    }
}

