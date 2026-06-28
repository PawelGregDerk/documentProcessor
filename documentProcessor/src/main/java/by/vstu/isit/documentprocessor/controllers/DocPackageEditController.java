package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.docx.edit.AbstractDocEditor;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import io.vavr.control.Try;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
import java.util.ArrayList;
import java.util.List;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;
import static java.util.stream.Collectors.toList;

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
    private ToggleButton regularModeBtn;
    @FXML
    private ToggleButton typeModeBtn;
    @FXML
    private VBox regularPanel;
    @FXML
    private VBox typePanel;
    @FXML
    private Button addTypeOperBtn;
    @FXML
    private VBox typeOperationsListContainer;
    @FXML
    private TextField typeOperSearchField;
    @FXML
    private Button saveToDiskBtn;
    private String path;
    @Setter
    private Long packageId;

    @Value("${out.pu.path}")
    private String outPuPath;

    private final DockPackageFormMapper formMapper;
    private final List<AbstractDocEditor> editors;
    private final List<AbstractWordGenerator> generators;
    private final DocpackageService service;
    private final TypeOperService typeOperService;
    private final FxWeaver fxWeaver;

    private final List<TypeOperDto> selectedTypeOperations = new ArrayList<>();
    private List<TypeOperDto> allTypeOperations;
    private List<TypeOperDto> filteredTypeOperations;

    public void setPath(String path) {
        this.path = path;
        if (path != null && saveToDiskBtn != null) {
            String base = outPuPath.substring(0, outPuPath.indexOf("{0}"));
            saveToDiskBtn.setDisable(new File(base + path).exists());
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
        ToggleGroup group = new ToggleGroup();
        regularModeBtn.setToggleGroup(group);
        typeModeBtn.setToggleGroup(group);

        regularModeBtn.setSelected(true);

        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                group.selectToggle(oldVal);
                return;
            }

            if (newVal == regularModeBtn) {
                regularPanel.setVisible(true);
                regularPanel.setManaged(true);
                typePanel.setVisible(false);
                typePanel.setManaged(false);
            } else if (newVal == typeModeBtn) {
                regularPanel.setVisible(false);
                regularPanel.setManaged(false);
                typePanel.setVisible(true);
                typePanel.setManaged(true);
                loadTypeOperations();
            }
        });
    }

    private void loadTypeOperations() {
        try {
            allTypeOperations = typeOperService.findAll();
            filteredTypeOperations = new ArrayList<>(allTypeOperations);
            displayTypeOperations(filteredTypeOperations);
        } catch (Exception e) {
            log.error("Ошибка загрузки типовых операций", e);
            allTypeOperations = new ArrayList<>();
            filteredTypeOperations = new ArrayList<>();
            displayTypeOperations(filteredTypeOperations);
        }
    }

    private void displayTypeOperations(List<TypeOperDto> operations) {
        typeOperationsListContainer.getChildren().clear();

        for (TypeOperDto typeOper : operations) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(Double.MAX_VALUE);

            Label nameLabel = new Label(
                    typeOper.name() != null ? typeOper.name() : "<без названия>"
            );
            nameLabel.setWrapText(true);
            nameLabel.setPrefWidth(500);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            Button addBtn = styled(new Button("Добавить"), "btn-primary", "btn-small");
            addBtn.setOnAction(e -> addTypeOperationToRegular(typeOper));

            row.getChildren().addAll(nameLabel, addBtn);
            typeOperationsListContainer.getChildren().add(row);
        }
    }

    private void addTypeOperationToRegular(TypeOperDto typeOper) {
        VBox operationBlock = styled(new VBox(5), "operation-block");
        operationBlock.setUserData(null);
        operationBlock.setMaxWidth(Double.MAX_VALUE);

        HBox operRow = styled(new HBox(6), "operation-row");
        operRow.setMaxWidth(Double.MAX_VALUE);
        operRow.setAlignment(Pos.CENTER_LEFT);

        TextField numOper = styled(new TextField(typeOper.numOper()), "compact");
        TextField nomInstr = styled(new TextField(typeOper.nomInstr()), "compact");
        TextField oborud = new TextField(typeOper.oborud());
        TextField ostnas = new TextField(typeOper.ostnasInstr());
        TextField name = new TextField(typeOper.name());
        TextField shifr = styled(new TextField(typeOper.shifr()), "compact");
        TextField zech = styled(new TextField(typeOper.numZech()), "compact");

        List<TextField> fields = List.of(numOper, nomInstr, oborud, ostnas, name, shifr, zech);
        fields.forEach(f -> {
            HBox.setHgrow(f, Priority.ALWAYS);
            f.setMaxWidth(Double.MAX_VALUE);
        });

        Button addFuncBtn = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        addFuncBtn.setPrefWidth(140);
        Button delOperBtn = styled(new Button("Удалить"), "btn-danger", "btn-small");
        delOperBtn.setPrefWidth(90);

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
        delOperBtn.setOnAction(e -> operationsContainer.getChildren().remove(operationBlock));

        operationBlock.getChildren().addAll(operRow, funcsContainer);
        operationsContainer.getChildren().add(operationBlock);
    }

    private HBox createFunctionRowFromType(TypeOperFuncDto typeFunc) {
        HBox funcRow = styled(new HBox(6), "function-row");
        funcRow.getStyleClass().add("function-data-row");
        funcRow.setAlignment(Pos.CENTER_LEFT);

        TextField name = styled(new TextField(typeFunc.name() != null ? typeFunc.name() : ""), "compact");
        name.setPromptText("Описание");
        TextField param = styled(new TextField(typeFunc.param() != null ? typeFunc.param() : ""), "no-compact");
        param.setPromptText("Параметры");
        CheckBox isProd = new CheckBox();
        isProd.setSelected(typeFunc.isProd());
        TextField spec = styled(new TextField(typeFunc.specCharakt() != null ? typeFunc.specCharakt() : ""), "compact");
        spec.setPromptText("Спец. характеристики");

        List<javafx.scene.Node> growFields = List.of(name, param, spec);
        for (var node : growFields) {
            if (node instanceof TextField tf) {
                HBox.setHgrow(tf, Priority.ALWAYS);
                tf.setMaxWidth(Double.MAX_VALUE);
            }
        }

        Button delBtn = styled(new Button("Удалить"), "delButton");
        delBtn.setOnAction(e -> {
            VBox parent = (VBox) funcRow.getParent();
            parent.getChildren().remove(funcRow);
            removeFunctionHeaderIfEmpty(parent);
        });

        funcRow.getChildren().addAll(name, param, isProd, spec, delBtn);
        return funcRow;
    }

    @FXML
    private void onSearchTypeOper() {
        String searchText = typeOperSearchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            filteredTypeOperations = new ArrayList<>(allTypeOperations);
        } else {
            filteredTypeOperations = allTypeOperations.stream()
                    .filter(to ->
                            (to.numOper() != null && to.numOper().toLowerCase().contains(searchText)) ||
                                    (to.name() != null && to.name().toLowerCase().contains(searchText))
                    )
                    .collect(toList());
        }
        displayTypeOperations(filteredTypeOperations);
    }

    @FXML
    private void onResetTypeOperSearch() {
        typeOperSearchField.clear();
        filteredTypeOperations = new ArrayList<>(allTypeOperations);
        displayTypeOperations(filteredTypeOperations);
    }

    @FXML
    private void onAddTypeOper() {
        Stage stage = new Stage();
        Parent root = fxWeaver.loadView(TypeOperEditController.class);
        stage.setScene(new Scene(root));
        stage.setTitle("Создание типовой операции");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        loadTypeOperations();
    }

    @FXML
    private void onAddOperation() {
        VBox operationBlock = styled(new VBox(5), "operation-block");
        operationBlock.setUserData(null);
        operationBlock.setMaxWidth(Double.MAX_VALUE);
        HBox operRow = styled(new HBox(6), "operation-row");
        operRow.setMaxWidth(Double.MAX_VALUE);
        operRow.setAlignment(Pos.CENTER_LEFT);
        TextField numOper = styled(new TextField(), "compact");
        numOper.setPromptText("№ операции");
        TextField nomInstr = styled(new TextField(), "compact");
        nomInstr.setPromptText("№ инструкции");
        TextField oborud = new TextField();
        oborud.setPromptText("Оборудование");
        TextField ostnas = new TextField();
        ostnas.setPromptText("Оснастка / Инструмент");
        TextField name = new TextField();
        name.setPromptText("Наименование");
        TextField shifr = styled(new TextField(), "compact");
        shifr.setPromptText("Шифр");
        TextField zech = styled(new TextField(), "compact");
        zech.setPromptText("Цех");

        Button addFuncBtn = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        addFuncBtn.setPrefWidth(140);
        Button delOperBtn = styled(new Button("Удалить"), "btn-danger", "btn-small");
        delOperBtn.setPrefWidth(90);

        List<TextField> fields = List.of(
                numOper, nomInstr, oborud, ostnas, name, shifr, zech
        );

        fields.forEach(f -> {
            HBox.setHgrow(f, Priority.ALWAYS);
            f.setMaxWidth(Double.MAX_VALUE);
        });

        operRow.getChildren().addAll(
                numOper, nomInstr, oborud, ostnas, name, shifr, zech, addFuncBtn, delOperBtn
        );

        VBox funcsContainer = styled(new VBox(4), "funcs-container");

        addFuncBtn.setOnAction(e -> addFunctionRow(funcsContainer));
        delOperBtn.setOnAction(e -> operationsContainer.getChildren().remove(operationBlock));
        operationBlock.getChildren().addAll(operRow, funcsContainer);
        operationsContainer.getChildren().add(operationBlock);
    }

    private void addFunctionRow(VBox funcsContainer) {
        addFunctionHeader(funcsContainer);

        HBox funcRow = styled(new HBox(6), "function-row");
        funcRow.getStyleClass().add("function-data-row");
        funcRow.setAlignment(Pos.CENTER_LEFT);

        TextField name = styled(new TextField(), "compact");
        name.setPromptText("Описание");
        TextField param = styled(new TextField(), "no-compact");
        param.setPromptText("Параметры");
        CheckBox isProd = new CheckBox();
        TextField spec = styled(new TextField(), "compact");
        spec.setPromptText("Спец. характеристики");

        List<javafx.scene.Node> growFields = List.of(name, param, spec);
        for (var node : growFields) {
            if (node instanceof TextField tf) {
                HBox.setHgrow(tf, Priority.ALWAYS);
                tf.setMaxWidth(Double.MAX_VALUE);
            }
        }

        Button delBtn = styled(new Button("Удалить"), "delButton");
        delBtn.setOnAction(e -> {
            funcsContainer.getChildren().remove(funcRow);
            removeFunctionHeaderIfEmpty(funcsContainer);
        });

        funcRow.getChildren().addAll(name, param, isProd, spec, delBtn);
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

    @FXML
    private void onSave() {
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
            var savedDto = service.updateFullPackage(dto);

            editors.forEach(e -> Try.run(() -> e.edit(originalDto, savedDto))
                    .onFailure(ex -> log.error("Ошибка редактирования документа", ex))
                    .getOrElseThrow(ex -> new RuntimeException(ex)));

            new Alert(Alert.AlertType.INFORMATION, "Документы сохранены в папку копия_" + dto.path()).showAndWait();
        } catch (Exception ex) {
            log.error("Ошибка сохранения", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
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
        } catch (Exception ex) {
            log.error("Ошибка обновления", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onSaveToDiskOnly() {
        try {
            var pckgDto = formMapper.fromGui(
                    null,
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
            if (pckgDto.sborEds().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну сборочную единицу");
            if (pckgDto.opers().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну операцию");
            if (pckgDto.opers().stream().anyMatch(o -> o.funcs().isEmpty())) throw new IllegalStateException("Каждая операция должна содержать хотя бы одну функцию");

            String base = outPuPath.substring(0, outPuPath.indexOf("{0}"));
            if (new File(base + pckgDto.path()).exists()) {
                throw new IllegalStateException("Папка с оригиналами уже существует: " + base + pckgDto.path());
            }

            generators.forEach(g -> Try.run(() -> g.generate(pckgDto))
                    .onFailure(ex -> log.error("Ошибка генерации документа", ex))
                    .getOrElseThrow(ex -> new RuntimeException(ex)));
            new Alert(Alert.AlertType.INFORMATION, "Документ сформирован").showAndWait();
        } catch (Exception ex) {
            log.error("Ошибка генерации", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void onBack() {
        Stage currentStage = (Stage) packageNameField.getScene().getWindow();
        currentStage.close();
    }
}
