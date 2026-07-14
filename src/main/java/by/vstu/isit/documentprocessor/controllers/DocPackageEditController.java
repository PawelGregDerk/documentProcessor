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
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        TextField codeField = styled(new TextField(), "compact");
        codeField.setPromptText("Обозначение сборочной единицы");
        codeField.prefWidthProperty().bind(row.widthProperty().multiply(0.75));
        HBox.setHgrow(codeField, Priority.ALWAYS);
        Button deleteButton = styled(new Button("Удалить"), "delButton");
        deleteButton.setOnAction(e -> assemblyUnitsContainer.getChildren().remove(row));

        row.getChildren().addAll(codeField, deleteButton);
        assemblyUnitsContainer.getChildren().add(row);
    }

    @FXML
    private void initialize() {
    }



    private HBox createFunctionRowFromType(TypeOperFuncDto typeFunc) {
        HBox funcRow = styled(new HBox(6), "function-row");
        funcRow.getStyleClass().add("function-data-row");
        funcRow.setAlignment(Pos.CENTER_LEFT);

        TextArea name = styled(new TextArea(typeFunc.name() != null ? typeFunc.name() : ""), "compact");
        name.setPromptText("Описание");
        name.setPrefRowCount(2);
        name.setWrapText(true);
        TextArea param = styled(new TextArea(typeFunc.param() != null ? typeFunc.param() : ""), "compact");
        param.setPromptText("Параметры");
        param.setPrefRowCount(2);
        param.setWrapText(true);
        CheckBox isProd = new CheckBox();
        isProd.setSelected(typeFunc.isProd());
        StackPane checkWrap = new StackPane(isProd);
        checkWrap.setAlignment(Pos.CENTER);
        TextArea spec = styled(new TextArea(typeFunc.specCharakt() != null ? typeFunc.specCharakt() : ""), "compact");
        spec.setPromptText("Спец. характеристики");
        spec.setPrefRowCount(2);
        spec.setWrapText(true);

        setGrow(name, 140);
        setGrow(param, 140);
        setGrow(spec, 140);

        Button delBtn = styled(new Button("Удалить"), "delButton");
        delBtn.setOnAction(e -> {
            VBox parent = (VBox) funcRow.getParent();
            parent.getChildren().remove(funcRow);
            removeFunctionHeaderIfEmpty(parent);
        });

        funcRow.getChildren().addAll(name, param, checkWrap, spec, delBtn);
        setFixedWidth(checkWrap, 70);
        setFixedWidth(delBtn, 90);
        return funcRow;
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
        VBox operationBlock = styled(new VBox(5), "operation-block");
        operationBlock.setUserData(null);
        operationBlock.setMaxWidth(Double.MAX_VALUE);
        HBox operRow = styled(new HBox(6), "operation-row");
        operRow.setMaxWidth(Double.MAX_VALUE);
        operRow.setAlignment(Pos.CENTER_LEFT);
        TextArea numOper = styled(new TextArea(), "compact");
        numOper.setPromptText("№ операции");
        numOper.setPrefRowCount(2);
        numOper.setWrapText(true);
        TextArea nomInstr = styled(new TextArea(), "compact");
        nomInstr.setPromptText("№ инструкции");
        nomInstr.setPrefRowCount(2);
        nomInstr.setWrapText(true);
        TextArea oborud = styled(new TextArea(), "compact");
        oborud.setPromptText("Оборудование");
        oborud.setPrefRowCount(2);
        oborud.setWrapText(true);
        TextArea ostnas = styled(new TextArea(), "compact");
        ostnas.setPromptText("Оснастка / Инструмент");
        ostnas.setPrefRowCount(2);
        ostnas.setWrapText(true);
        TextArea name = styled(new TextArea(), "compact");
        name.setPromptText("Наименование");
        name.setPrefRowCount(2);
        name.setWrapText(true);
        TextArea shifr = styled(new TextArea(), "compact");
        shifr.setPromptText("Шифр");
        shifr.setPrefRowCount(2);
        shifr.setWrapText(true);
        TextArea zech = styled(new TextArea(), "compact");
        zech.setPromptText("Цех");
        zech.setPrefRowCount(2);
        zech.setWrapText(true);

        Button addFuncBtn = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        Button delOperBtn = styled(new Button("Удалить"), "btn-danger", "btn-small");

        setGrow(numOper, 80);
        setGrow(nomInstr, 110);
        setGrow(oborud, 120);
        setGrow(ostnas, 140);
        setGrow(name, 160);
        setGrow(shifr, 80);
        setGrow(zech, 60);

        operRow.getChildren().addAll(
                numOper, nomInstr, oborud, ostnas, name, shifr, zech, addFuncBtn, delOperBtn
        );

        VBox funcsContainer = styled(new VBox(4), "funcs-container");

        addFuncBtn.setOnAction(e -> addFunctionRow(funcsContainer));
        delOperBtn.setOnAction(e -> removeOperation(operationBlock));

        operationBlock.getChildren().addAll(operRow, funcsContainer);
        return operationBlock;
    }

    private VBox createOperationBlock(TypeOperDto typeOper) {
        VBox operationBlock = styled(new VBox(5), "operation-block");
        operationBlock.setUserData(null);
        operationBlock.setMaxWidth(Double.MAX_VALUE);

        HBox operRow = styled(new HBox(6), "operation-row");
        operRow.setMaxWidth(Double.MAX_VALUE);
        operRow.setAlignment(Pos.CENTER_LEFT);

        TextArea numOper = styled(new TextArea(typeOper.numOper()), "compact");
        numOper.setPrefRowCount(2);
        numOper.setWrapText(true);
        TextArea nomInstr = styled(new TextArea(typeOper.nomInstr()), "compact");
        nomInstr.setPrefRowCount(2);
        nomInstr.setWrapText(true);
        TextArea oborud = styled(new TextArea(typeOper.oborud()), "compact");
        oborud.setPrefRowCount(2);
        oborud.setWrapText(true);
        TextArea ostnas = styled(new TextArea(typeOper.ostnasInstr()), "compact");
        ostnas.setPrefRowCount(2);
        ostnas.setWrapText(true);
        TextArea name = styled(new TextArea(typeOper.name()), "compact");
        name.setPrefRowCount(2);
        name.setWrapText(true);
        TextArea shifr = styled(new TextArea(typeOper.shifr()), "compact");
        shifr.setPrefRowCount(2);
        shifr.setWrapText(true);
        TextArea zech = styled(new TextArea(typeOper.numZech()), "compact");
        zech.setPrefRowCount(2);
        zech.setWrapText(true);

        List<Node> fields = List.of(numOper, nomInstr, oborud, ostnas, name, shifr, zech);
        setGrow(numOper, 80);
        setGrow(nomInstr, 110);
        setGrow(oborud, 120);
        setGrow(ostnas, 140);
        setGrow(name, 160);
        setGrow(shifr, 80);
        setGrow(zech, 60);

        Button addFuncBtn = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        Button delOperBtn = styled(new Button("Удалить"), "btn-danger", "btn-small");

        operRow.getChildren().addAll(
                numOper, nomInstr, oborud, ostnas, name, shifr, zech, addFuncBtn, delOperBtn
        );

        VBox funcsContainer = styled(new VBox(4), "funcs-container");

        if (typeOper.funcs() != null && !typeOper.funcs().isEmpty()) {
            addFunctionHeader(funcsContainer);
            for (TypeOperFuncDto typeFunc : typeOper.funcs()) {
                HBox funcRow = createFunctionRowFromType(typeFunc);
                funcsContainer.getChildren().add(funcRow);
            }
        }

        addFuncBtn.setOnAction(e -> addFunctionRow(funcsContainer));
        delOperBtn.setOnAction(e -> removeOperation(operationBlock));

        operationBlock.getChildren().addAll(operRow, funcsContainer);
        return operationBlock;
    }

    @FXML
    private void onAddOperation() {
        insertOperationAt(getOperationBlocks().size(), null);
    }

    private void addFunctionRow(VBox funcsContainer) {
        addFunctionHeader(funcsContainer);

        HBox funcRow = styled(new HBox(6), "function-row");
        funcRow.getStyleClass().add("function-data-row");
        funcRow.setAlignment(Pos.CENTER_LEFT);

        TextArea name = styled(new TextArea(), "compact");
        name.setPromptText("Описание");
        name.setPrefRowCount(2);
        name.setWrapText(true);
        TextArea param = styled(new TextArea(), "compact");
        param.setPromptText("Параметры");
        param.setPrefRowCount(2);
        param.setWrapText(true);
        CheckBox isProd = new CheckBox();
        StackPane checkWrap = new StackPane(isProd);
        checkWrap.setAlignment(Pos.CENTER);
        TextArea spec = styled(new TextArea(), "compact");
        spec.setPromptText("Спец. характеристики");
        spec.setPrefRowCount(2);
        spec.setWrapText(true);

        setGrow(name, 140);
        setGrow(param, 140);
        setGrow(spec, 140);

        Button delBtn = styled(new Button("Удалить"), "delButton");
        delBtn.setOnAction(e -> {
            funcsContainer.getChildren().remove(funcRow);
            removeFunctionHeaderIfEmpty(funcsContainer);
        });

        funcRow.getChildren().addAll(name, param, checkWrap, spec, delBtn);
        setFixedWidth(checkWrap, 70);
        setFixedWidth(delBtn, 90);
        funcsContainer.getChildren().add(funcRow);
    }

    private void addFunctionHeader(VBox funcsContainer) {
        boolean hasHeader = funcsContainer.getChildren().stream()
                .anyMatch(n -> n.getStyleClass().contains("function-header-row"));
        if (!hasHeader) {
            HBox header = styled(new HBox(6), "function-header");
            header.getStyleClass().add("function-header-row");
            header.setMaxWidth(Double.MAX_VALUE);
            header.getChildren().addAll(
                    styled(new Label("Описание функции"), "table-header"),
                    styled(new Label("Параметры / Требования"), "table-header"),
                    styled(new Label("Продукт"), "table-header"),
                    styled(new Label("Спец. характеристики"), "table-header")
            );
            Region buttonGap = new Region();
            header.getChildren().add(buttonGap);
            growFunctionHeader(
                    (Label) header.getChildren().get(0),
                    (Label) header.getChildren().get(1),
                    (Label) header.getChildren().get(3)
            );
            setFixedWidth((Region) header.getChildren().get(2), 70);
            setFixedWidth(buttonGap, 90);
            funcsContainer.getChildren().add(header);
        }
    }

    private void removeFunctionHeaderIfEmpty(VBox funcsContainer) {
        boolean hasDataRows = funcsContainer.getChildren().stream()
                .anyMatch(n -> n.getStyleClass().contains("function-data-row"));
        if (!hasDataRows) {
            funcsContainer.getChildren().removeIf(
                    n -> n.getStyleClass().contains("function-header-row")
            );
        }
    }

    private void setGrow(Region region, double minWidth) {
        region.setMinWidth(minWidth);
        region.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(region, Priority.ALWAYS);
    }

    private void setFixedWidth(Region region, double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }

    private void growFunctionHeader(Label name, Label param, Label spec) {
        setGrow(name, 140);
        setGrow(param, 140);
        setGrow(spec, 140);
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
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Изменение порядка операций");
                confirm.setHeaderText("Последовательность операций изменена");
                confirm.setContentText("Рукописные изменения в документах будут утеряны. Вы согласны?");

                ButtonType yesBtn = new ButtonType("Да", ButtonBar.ButtonData.YES);
                ButtonType cancelBtn = new ButtonType("Отмена", ButtonBar.ButtonData.NO);
                confirm.getButtonTypes().setAll(yesBtn, cancelBtn);

                var response = confirm.showAndWait();
                if (response.isEmpty() || response.get() != yesBtn) {
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

                new Alert(Alert.AlertType.INFORMATION, "Документы сохранены в папку " + dto.path()).showAndWait();
            } else {
                generators.forEach(g -> Try.run(() -> g.generate(savedDto))
                        .onFailure(ex -> log.error("Ошибка генерации документа", ex))
                        .getOrElseThrow(ex -> new RuntimeException(ex)));
                new Alert(Alert.AlertType.INFORMATION, "Документ сформирован").showAndWait();
            }

            refreshFromDb();
        } catch (Exception ex) {
            log.error("Ошибка сохранения", ex);
            new Alert(Alert.AlertType.ERROR, "Ошибка сохранения: " + ex.getMessage()).showAndWait();
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

            new Alert(Alert.AlertType.INFORMATION, "Данные обновлены в базе данных").showAndWait();
            refreshFromDb();
        } catch (Exception ex) {
            log.error("Ошибка обновления", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
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
