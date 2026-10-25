package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.docx.edit.AbstractDocEditor;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.ui.AlertService;
import by.vstu.isit.documentprocessor.ui.AssemblyUnitRowFactory;
import by.vstu.isit.documentprocessor.ui.OperationBlockFactory;
import io.vavr.control.Try;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

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
    private int pendingInsertIndex;

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

    public void setPath(String path) {
        this.path = path;
        if (path != null && saveBtn != null) {
            String base = outPuPath.substring(0, outPuPath.indexOf("{0}"));
            boolean folderExists = new File(base + path).exists();
            saveBtn.setText(folderExists ? "Обновить пакет на диске" : "Создать пакет на диске");
            updateDbOnlyBtn.setDisable(folderExists);
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
    }



    @FXML
    private void onCreateTypeOper() {
        TypeOperEditController.prepareForCreate();
        Stage stage = new Stage();
        Parent root = fxWeaver.loadView(TypeOperEditController.class);
        stage.setScene(new Scene(root));
        stage.setTitle("Создание типовой операции");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    private void onOpenTypeOperList() {
        openTypeOperListAt(getOperationBlocks().size());
    }

    private void openTypeOperListAt(int index) {
        pendingInsertIndex = index;
        TypeOperListController.setOnAddCallback(typeOper -> insertOperationAt(pendingInsertIndex, typeOper));
        Stage stage = new Stage();
        Parent root = fxWeaver.loadView(TypeOperListController.class);
        stage.setScene(new Scene(root));
        stage.setTitle("Типовые операции");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        TypeOperListController.clearCallback();
    }

    private VBox createEmptyOperationBlock() {
        return OperationBlockFactory.createEmptyOperationBlock(this::removeOperation);
    }

    private VBox createOperationBlock(TypeOperDto typeOper) {
        return OperationBlockFactory.createOperationBlock(typeOper, this::removeOperation);
    }

    @FXML
    private void onAddOperation() {
        insertOperationAt(getOperationBlocks().size(), null);
    }

    /* ========= helpers: operation separators ========= */

    private List<Node> getOperationBlocks() {
        return operationsContainer.getChildren().stream()
                .filter(n -> n.getStyleClass().contains("operation-block"))
                .collect(Collectors.toList());
    }

    void rebuildSeparators() {
        var blocks = getOperationBlocks();
        operationsContainer.getChildren().clear();
        for (int i = 0; i < blocks.size(); i++) {
            operationsContainer.getChildren().add(createSeparator(i));
            operationsContainer.getChildren().add(blocks.get(i));
        }
    }

    private HBox createSeparator(int insertIndex) {
        HBox sep = new HBox();
        sep.setAlignment(Pos.CENTER);
        sep.setStyle("-fx-border-color: #ddd; -fx-border-width: 1 0 0 0; -fx-padding: 2;");

        Button plusBtn = new Button("Добавить операцию");
        plusBtn.getStyleClass().addAll("btn-small", "btn-add");

        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("btn-add");
        MenuItem regularItem = new MenuItem("Обычная операция");
        regularItem.setOnAction(e -> { menu.hide(); insertOperationAt(insertIndex, null); });
        MenuItem typeItem = new MenuItem("Типовая операция");
        typeItem.setOnAction(e -> { menu.hide(); openTypeOperListAt(insertIndex); });
        menu.getItems().addAll(regularItem, typeItem);

        plusBtn.setOnAction(e -> {
            double w = plusBtn.getWidth();
            menu.setStyle("-fx-pref-width: " + w + "px; -fx-min-width: " + w + "px;");
            menu.show(plusBtn, Side.BOTTOM, 0, 0);
        });

        sep.getChildren().add(plusBtn);
        return sep;
    }

    private void insertOperationAt(int index, TypeOperDto typeOper) {
        var blocks = getOperationBlocks();
        VBox newBlock = typeOper != null ? createOperationBlock(typeOper) : createEmptyOperationBlock();
        blocks.add(index, newBlock);
        operationsContainer.getChildren().clear();
        operationsContainer.getChildren().addAll(blocks);
        rebuildSeparators();
    }

    private void removeOperation(Node block) {
        operationsContainer.getChildren().remove(block);
        rebuildSeparators();
    }

    void wrapOperationDeleteHandlers() {
        for (var block : getOperationBlocks()) {
            VBox vb = (VBox) block;
            if (vb.getChildren().isEmpty()) continue;
            Node firstChild = vb.getChildren().get(0);
            if (!(firstChild instanceof HBox operRow)) continue;
            for (var child : operRow.getChildren()) {
                if (child instanceof Button btn && "Удалить".equals(btn.getText())) {
                    btn.setOnAction(e -> removeOperation(block));
                    break;
                }
            }
        }
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

            if (dto.sborEds().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну сборочную единицу");
            if (dto.opers().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну операцию");
            if (dto.opers().stream().anyMatch(o -> o.funcs().isEmpty())) throw new IllegalStateException("Каждая операция должна содержать хотя бы одну функцию");

            var originalDto = service.findDtoById(packageId);

            String base = outPuPath.substring(0, outPuPath.indexOf("{0}"));
            Path packageDir = Path.of(base, path);
            boolean folderExists = Files.exists(packageDir);

            if (folderExists && checkOrderChanged(dto, originalDto)) {
                boolean confirmed = alertService.confirm(
                        "Изменение порядка операций",
                        "Последовательность операций изменена",
                        "Рукописные изменения в документах будут утеряны. Вы согласны?"
                );
                if (!confirmed) {
                    return;
                }
            }

            var result = service.updateFullPackage(dto);
            var savedDto = result.dto();

            if (folderExists) {
                String oldPackageName = originalDto.packageName();
                String archiveRelPath = path + "/Предыдущая версия " + oldPackageName;
                Path archiveDir = packageDir.resolve("Предыдущая версия " + oldPackageName);
                archivePackageDir(packageDir, archiveDir);
                deleteDirContents(packageDir, archiveDir);

                if (result.orderChanged()) {
                    generators.forEach(g -> Try.run(() -> g.generate(savedDto))
                            .onFailure(ex -> log.error("Ошибка генерации документа", ex))
                            .getOrElseThrow(ex -> new RuntimeException(ex)));
                } else {
                    editors.forEach(e -> {
                        e.setSourceFolderOverride(archiveRelPath);
                        Try.run(() -> e.edit(originalDto, savedDto))
                                .onFailure(ex -> log.error("Ошибка редактирования документа", ex))
                                .getOrElseThrow(ex -> new RuntimeException(ex));
                    });
                }

                alertService.showInfo("Документы сохранены в папку " + dto.path());
            } else {
                generators.forEach(g -> Try.run(() -> g.generate(savedDto))
                        .onFailure(ex -> log.error("Ошибка генерации документа", ex))
                        .getOrElseThrow(ex -> new RuntimeException(ex)));
                alertService.showInfo("Документ сформирован");
            }

            refreshFromDb();
        } catch (Exception ex) {
            log.error("Ошибка сохранения", ex);
            alertService.showError("Ошибка сохранения: " + ex.getMessage());
        }
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

    private void archivePackageDir(Path sourceDir, Path archiveDir) throws IOException {
        if (Files.exists(archiveDir)) {
            deleteDirRecursively(archiveDir);
        }
        Files.createDirectories(archiveDir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path entry : stream) {
                if (entry.equals(archiveDir)) continue;
                if (entry.getFileName().toString().startsWith("Предыдущая версия")) continue;
                copyRecursively(entry, archiveDir.resolve(entry.getFileName()));
            }
        }
    }

    private void copyToPackageDir(Path sourceDir, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path entry : stream) {
                copyRecursively(entry, targetDir.resolve(entry.getFileName()));
            }
        }
    }

    private void deleteDirContents(Path dir, Path exclude) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (entry.equals(exclude)) continue;
                if (Files.isDirectory(entry)) {
                    deleteDirRecursively(entry);
                } else {
                    Files.delete(entry);
                }
            }
        }
    }

    private void copyRecursively(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            Files.createDirectories(target);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                for (Path entry : stream) {
                    copyRecursively(entry, target.resolve(entry.getFileName()));
                }
            }
        } else {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
        copyHiddenAttr(source, target);
    }

    private void copyHiddenAttr(Path source, Path target) {
        try {
            if ((Boolean) Files.getAttribute(source, "dos:hidden")) {
                Files.setAttribute(target, "dos:hidden", true);
            }
        } catch (Exception ignored) {}
    }

    private void deleteDirRecursively(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        deleteDirRecursively(entry);
                    } else {
                        Files.delete(entry);
                    }
                }
            }
            Files.delete(dir);
        }
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

            if (dto.sborEds().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну сборочную единицу");
            if (dto.opers().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну операцию");
            if (dto.opers().stream().anyMatch(o -> o.funcs().isEmpty())) throw new IllegalStateException("Каждая операция должна содержать хотя бы одну функцию");

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

    private boolean checkOrderChanged(DockPackageDto dto, DockPackageDto originalDto) {
        var oldIds = originalDto.opers().stream().map(OperDto::id).toList();
        for (int i = 0; i < dto.opers().size(); i++) {
            Long id = dto.opers().get(i).id();
            if (id != null) {
                int oldIdx = oldIds.indexOf(id);
                if (oldIdx != i) return true;
            }
        }
        return false;
    }
}
