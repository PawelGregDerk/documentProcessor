package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.DocPackageSaveOrchestrator;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;
import by.vstu.isit.documentprocessor.ui.AlertService;
import by.vstu.isit.documentprocessor.ui.AssemblyUnitRowFactory;
import by.vstu.isit.documentprocessor.ui.BackgroundSave;
import by.vstu.isit.documentprocessor.ui.OperationBlockFactory;
import by.vstu.isit.documentprocessor.ui.TypeOperWindowHelper;
import by.vstu.isit.documentprocessor.validation.DockPackageDtoValidator;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Value;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.List;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@Slf4j
@Controller
@FxmlView("docpackage-view.fxml")
@RequiredArgsConstructor
public class DocPackageCreateController {
    @FXML
    private TextField packageNameField;
    @FXML
    private TextField puField;
    @FXML
    private TextField spuField;
    @FXML
    private TextField kpField;
    @FXML
    private TextField fmeaField;
    @FXML
    private TextField vedInstrField;
    @FXML
    private TextField sborEdNazv;
    @FXML
    private VBox assemblyUnitsContainer;
    @FXML
    private VBox operationsContainer;
    @FXML
    private TextField packagePathField;
    private final DockPackageFormMapper formMapper;
    private final DocpackageService service;
    private final TypeOperService typeOperService;
    private final List<AbstractWordGenerator> generators;
    private final FxWeaver fxWeaver;
    private final AlertService alertService;
    private boolean backgroundSaves;

    @Value("${out.pu.path}")
    private String outPuPath;

    @FXML
    private void initialize() {
        backgroundSaves = true;
    }

    private void addTypeOperationToRegular(TypeOperDto typeOper) {
        operationsContainer.getChildren().add(OperationBlockFactory.createOperationBlock(
                typeOper, block -> operationsContainer.getChildren().remove(block)));
    }

    @FXML
    private void onAddAssemblyUnit() {
        HBox row = AssemblyUnitRowFactory.createRow(null, null,
                r -> assemblyUnitsContainer.getChildren().remove(r));
        assemblyUnitsContainer.getChildren().add(row);
    }

    @FXML
    private void onCreateTypeOper() {
        TypeOperWindowHelper.openCreate(fxWeaver);
    }

    @FXML
    private void onOpenTypeOperList() {
        TypeOperWindowHelper.openList(fxWeaver, this::addTypeOperationToRegular);
    }

    @FXML
    private void onAddOperation() {
        operationsContainer.getChildren().add(OperationBlockFactory.createEmptyOperationBlock(
                block -> operationsContainer.getChildren().remove(block)));
    }

    @FXML
    private void onSave() {
        DockPackageDto dto;
        try {
            dto = formMapper.fromGui(
                    null,
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
            DockPackageDtoValidator.validate(dto);
        } catch (Exception ex) {
            showSaveError(ex);
            return;
        }

        if (!backgroundSaves) {
            try {
                orchestrator().createPackage(dto);
                alertService.showInfo("Документ сформирован");
            } catch (Exception ex) {
                showSaveError(ex);
            }
            return;
        }

        BackgroundSave.run(
                packageNameField.getScene().getWindow(),
                () -> orchestrator().createPackage(dto),
                () -> alertService.showInfo("Документ сформирован"),
                this::showSaveError);
    }

    private void showSaveError(Throwable error) {
        log.error("Ошибка генерации", error);
        alertService.showError(error.getMessage() == null ? "Ошибка сохранения" : error.getMessage());
    }

    @FXML
    private void onSaveToDbOnly() {
        try {
            var pckgDto = formMapper.fromGui(
                    null,
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
            DockPackageDtoValidator.validate(pckgDto);

            service.saveFullPackage(pckgDto);

            alertService.showInfo("Пакет сохранён в базу данных");
        } catch (Exception ex) {
            log.error("Ошибка сохранения в БД", ex);
            alertService.showError(ex.getMessage());
        }
    }

    private DocPackageSaveOrchestrator orchestrator() {
        return new DocPackageSaveOrchestrator(
                service, generators, List.of(), new PackageFileManager(), outPuPath);
    }
}
