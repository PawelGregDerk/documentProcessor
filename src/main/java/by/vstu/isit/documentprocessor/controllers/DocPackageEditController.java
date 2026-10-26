package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.DocPackageSaveOrchestrator;
import by.vstu.isit.documentprocessor.services.docx.edit.AbstractDocEditor;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;
import by.vstu.isit.documentprocessor.ui.AlertService;
import by.vstu.isit.documentprocessor.ui.AssemblyUnitRowFactory;
import by.vstu.isit.documentprocessor.ui.BackgroundSave;
import by.vstu.isit.documentprocessor.ui.OperationBlockFactory;
import by.vstu.isit.documentprocessor.ui.OperationListManager;
import by.vstu.isit.documentprocessor.ui.TypeOperWindowHelper;
import by.vstu.isit.documentprocessor.validation.DockPackageDtoValidator;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.List;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@Slf4j
@Controller
@FxmlView("docpackage-edit-view.fxml")
@RequiredArgsConstructor
@Getter
public class DocPackageEditController {
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
    private Button saveBtn;
    @FXML
    private Button updateDbOnlyBtn;
    private String path;
    @Setter
    private Long packageId;
    private OperationListManager operationListManager;
    private boolean backgroundSaves;

    @Value("${out.pu.path}")
    private String outPuPath;

    private final DockPackageFormMapper formMapper;
    private final DockPackageGuiMapper guiMapper;
    private final List<AbstractDocEditor> editors;
    private final List<AbstractWordGenerator> generators;
    private final DocpackageService service;
    private final TypeOperService typeOperService;
    private final FxWeaver fxWeaver;
    private final AlertService alertService;
    private final PackageFileManager fileManager;

    public void setPath(String path) {
        this.path = path;
        if (path == null || saveBtn == null) {
            return;
        }
        try {
            boolean folderExists = fileManager.packageDirExists(outPuPath, path);
            saveBtn.setText(folderExists ? "Обновить пакет на диске" : "Создать пакет на диске");
            updateDbOnlyBtn.setDisable(folderExists);
        } catch (IllegalArgumentException ex) {
            saveBtn.setText("Создать пакет на диске");
            updateDbOnlyBtn.setDisable(false);
            alertService.showError(ex.getMessage());
        }
    }

    @FXML
    private void onAddAssemblyUnit() {
        HBox row = AssemblyUnitRowFactory.createRow(null, null,
                r -> assemblyUnitsContainer.getChildren().remove(r));
        assemblyUnitsContainer.getChildren().add(row);
    }

    @FXML
    private void initialize() {
        operationListManager = null;
        backgroundSaves = true;
    }



    @FXML
    private void onCreateTypeOper() {
        TypeOperWindowHelper.openCreate(fxWeaver);
    }

    @FXML
    private void onOpenTypeOperList() {
        openTypeOperListAt(operationList().blockCount());
    }

    private void openTypeOperListAt(int index) {
        TypeOperWindowHelper.openList(fxWeaver,
                typeOper -> operationList().insertAt(index, typeOper));
    }

    private VBox createEmptyOperationBlock() {
        return OperationBlockFactory.createEmptyOperationBlock(this::removeOperation);
    }

    private VBox createOperationBlock(TypeOperDto typeOper) {
        return OperationBlockFactory.createOperationBlock(typeOper, this::removeOperation);
    }

    @FXML
    private void onAddOperation() {
        operationList().addOperation();
    }

    /* ========= helpers: operation separators ========= */

    void rebuildSeparators() {
        operationList().rebuildSeparators();
    }

    private void removeOperation(Node block) {
        operationList().remove(block);
    }

    void wrapOperationDeleteHandlers() {
        operationList().wrapDeleteHandlers();
    }

    @FXML
    private void onSaveOrCreate() {
        try {
            var dto = formMapper.fromGui(
                    packageId,
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

            DockPackageDtoValidator.validate(dto);

            var orchestrator = orchestrator();
            var preview = orchestrator.previewUpdate(dto, packageId, path);

            if (preview.folderExists() && preview.orderChanged()) {
                boolean confirmed = alertService.confirm(
                        "Изменение порядка операций",
                        "Последовательность операций изменена",
                        "Рукописные изменения в документах будут утеряны. Вы согласны?"
                );
                if (!confirmed) {
                    return;
                }
            }

            String successMessage = preview.folderExists()
                    ? "Документы сохранены в папку " + dto.path()
                    : "Документ сформирован";

            if (!backgroundSaves) {
                orchestrator.updatePackage(dto, preview);
                alertService.showInfo(successMessage);
                refreshFromDb();
                return;
            }

            BackgroundSave.run(
                    saveBtn.getScene().getWindow(),
                    () -> orchestrator.updatePackage(dto, preview),
                    () -> {
                        try {
                            refreshFromDb();
                            alertService.showInfo(successMessage);
                        } catch (Exception ex) {
                            showSaveError(ex);
                        }
                    },
                    this::showSaveError);
        } catch (Exception ex) {
            showSaveError(ex);
        }
    }

    private void showSaveError(Throwable error) {
        log.error("Ошибка сохранения", error);
        alertService.showError("Ошибка сохранения: "
                + (error.getMessage() == null ? "неизвестная ошибка" : error.getMessage()));
    }

    private void refreshFromDb() {
        if (packageId == null) return;
        var freshDto = service.findDtoById(packageId);
        guiMapper.toGui(
                freshDto,
                packageNameField,
                sborEdNazv,
                puField,
                spuField,
                kpField,
                fmeaField,
                vedInstrField,
                operationsContainer,
                assemblyUnitsContainer
        );
        wrapOperationDeleteHandlers();
        rebuildSeparators();
        setPath(freshDto.path());
        setPackageId(freshDto.id());
    }

    @FXML
    private void onUpdateDbOnly() {
        try {
            var dto = formMapper.fromGui(
                    packageId,
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

            DockPackageDtoValidator.validate(dto);

            service.updateFullPackage(dto);

            alertService.showInfo("Данные обновлены в базе данных");
            refreshFromDb();
        } catch (Exception ex) {
            log.error("Ошибка обновления", ex);
            alertService.showError(ex.getMessage());
        }
    }

    @FXML
    void onBack() {
        Stage currentStage = (Stage) packageNameField.getScene().getWindow();
        currentStage.close();
    }

    private OperationListManager operationList() {
        if (operationListManager == null) {
            operationListManager = new OperationListManager(operationsContainer, this::openTypeOperListAt);
        }
        return operationListManager;
    }

    private DocPackageSaveOrchestrator orchestrator() {
        return new DocPackageSaveOrchestrator(service, generators, editors, fileManager, outPuPath);
    }
}
