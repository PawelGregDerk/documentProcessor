
package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import javafx.fxml.FXML;
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
import io.vavr.control.Try;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;
import static java.util.stream.Collectors.toList;

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
    // Элементы для переключения режимов
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

    // Элементы для типовых операций
    @FXML
    private VBox typeOperationsListContainer;
    @FXML
    private TextField typeOperSearchField;

    private final DockPackageFormMapper formMapper;
    private final DocpackageService service;
    private final TypeOperService typeOperService;
    private final List<AbstractWordGenerator> generators;
    private final List<TypeOperDto> selectedTypeOperations = new ArrayList<>();
    private final FxWeaver fxWeaver;

    private List<TypeOperDto> allTypeOperations;
    private List<TypeOperDto> filteredTypeOperations;

    @FXML
    private void initialize() {
        ToggleGroup group = new ToggleGroup();
        regularModeBtn.setToggleGroup(group);
        typeModeBtn.setToggleGroup(group);

        regularModeBtn.setSelected(true);

        // Запрещаем снятие выделения с уже выбранной кнопки
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                // Если пытаются снять выделение - возвращаем старую выбранную кнопку
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

    private void addSelectedTypeOperation() {
        new Alert(Alert.AlertType.INFORMATION, "Выберите типовую операцию из списка и нажмите 'Добавить' у неё").showAndWait();
    }

    private void loadTypeOperations() {
        try {
            System.out.println("=== ЗАГРУЗКА ТИПОВЫХ ОПЕРАЦИЙ ===");
            allTypeOperations = typeOperService.findAll();
            System.out.println("Загружено типовых операций: " + allTypeOperations.size());

            for (TypeOperDto to : allTypeOperations) {
                System.out.println("  - №" + to.numOper() + ": " + to.name());
            }

            filteredTypeOperations = new ArrayList<>(allTypeOperations);
            displayTypeOperations(filteredTypeOperations);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки типовых операций: " + e.getMessage());
            e.printStackTrace();
            allTypeOperations = new ArrayList<>();
            filteredTypeOperations = new ArrayList<>();
            displayTypeOperations(filteredTypeOperations);
        }
    }

    private void displayTypeOperations(List<TypeOperDto> operations) {
        if (typeOperationsListContainer == null) {
            return;
        }

        typeOperationsListContainer.getChildren().clear();

        for (TypeOperDto typeOper : operations) {

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(Double.MAX_VALUE);

            Label nameLabel = new Label(
                    typeOper.name() != null
                            ? typeOper.name()
                            : "<без названия>"
            );

            nameLabel.setWrapText(true);
            nameLabel.setPrefWidth(500);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            Button addBtn = styled(
                    new Button("Добавить"),
                    "btn-primary",
                    "btn-small"
            );

            addBtn.setOnAction(e -> addTypeOperationToRegular(typeOper));

            row.getChildren().addAll(nameLabel, addBtn);

            typeOperationsListContainer.getChildren().add(row);
        }
    }

    private Label createLabel(String text, double width) {
        Label label = new Label(text != null ? text : "");
        label.setPrefWidth(width);
        label.setWrapText(true);
        return label;
    }

    private void addTypeOperationToRegular(TypeOperDto typeOper) {
        VBox operationBlock = styled(new VBox(5), "operation-block");
        operationBlock.setUserData(null);

        HBox operRow = styled(new HBox(6), "operation-row");
        operRow.setAlignment(Pos.CENTER_LEFT);

        TextField numOper = styled(new TextField(typeOper.numOper()), "compact");
        TextField nomInstr = styled(new TextField(typeOper.nomInstr()), "compact");
        TextField oborud = styled(new TextField(typeOper.oborud()), "compact");
        TextField ostnas = styled(new TextField(typeOper.ostnasInstr()), "compact");
        TextField name = styled(new TextField(typeOper.name()), "compact");
        TextField shifr = styled(new TextField(typeOper.shifr()), "compact");
        TextField zech = styled(new TextField(typeOper.numZech()), "compact");
        growOperationFields(numOper, nomInstr, oborud, ostnas, name, shifr, zech);
        Button addFuncBtn = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        addFuncBtn.setPrefWidth(140);
        Button delOperBtn = styled(new Button("Удалить"), "btn-danger", "btn-small");
        delOperBtn.setPrefWidth(90);

        operRow.getChildren().addAll(
                numOper, nomInstr, oborud, ostnas, name, shifr, zech, addFuncBtn, delOperBtn
        );

        VBox funcsContainer = styled(new VBox(4), "funcs-container");

        if (typeOper.funcs() != null && !typeOper.funcs().isEmpty()) {
            ensureFunctionHeader(funcsContainer);
            for (var typeFunc : typeOper.funcs()) {
                HBox funcRow = createFunctionRowFromType(typeFunc);
                funcsContainer.getChildren().add(funcRow);
            }
        }

        addFuncBtn.setOnAction(e -> addFunctionRow(funcsContainer));
        delOperBtn.setOnAction(e -> operationsContainer.getChildren().remove(operationBlock));

        operationBlock.getChildren().addAll(operRow, funcsContainer);
        operationsContainer.getChildren().add(operationBlock);
    }

    private TextField createTextField(String value, double width) {
        TextField field = styled(new TextField(), "compact");
        field.setText(value != null ? value : "");
        field.setPrefWidth(width);
        return field;
    }

    private HBox createFunctionRowFromType(TypeOperFuncDto typeFunc) {
        HBox funcRow = styled(new HBox(6), "function-row");
        funcRow.getStyleClass().add("function-data-row");
        funcRow.setUserData(null);

        TextField name = styled(new TextField(typeFunc.name() != null ? typeFunc.name() : ""), "compact");
        name.setPromptText("Описание");
        name.setPrefWidth(140);

        TextField param = styled(new TextField(typeFunc.param() != null ? typeFunc.param() : ""), "no-compact");
        param.setPromptText("Параметры");
        param.setPrefWidth(140);

        CheckBox isProd = new CheckBox();
        isProd.setSelected(typeFunc.isProd());
        isProd.setPrefWidth(120);

        TextField spec = styled(new TextField(typeFunc.specCharakt() != null ? typeFunc.specCharakt() : ""), "compact");
        spec.setPromptText("Спец. характеристики");
        spec.setPrefWidth(140);

        Button delBtn = styled(new Button("Удалить"), "delButton");
        delBtn.setOnAction(e -> {
            VBox parent = (VBox) funcRow.getParent();
            parent.getChildren().remove(funcRow);
            cleanupFunctionHeader(parent);
        });

        funcRow.getChildren().addAll(name, param, isProd, spec, delBtn);
        growFunctionFields(name, param, spec);
        setFixedWidth(isProd, 70);
        setFixedWidth(delBtn, 90);
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
    private void onAddAssemblyUnit() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        TextField codeField = styled(new TextField(), "compact");
        codeField.setPromptText("Обозначение сборочной единицы");
        codeField.prefWidthProperty().bind(row.widthProperty().multiply(0.75));

        Button deleteButton = styled(new Button("Удалить"), "delButton");
        deleteButton.setOnAction(e -> assemblyUnitsContainer.getChildren().remove(row));

        row.getChildren().addAll(codeField, deleteButton);
        assemblyUnitsContainer.getChildren().add(row);
    }

    @FXML
    private void onAddTypeOper() {

        Stage stage = new Stage();

        Parent root = fxWeaver.loadView(TypeOperEditController.class);

        stage.setScene(new Scene(root));
        stage.setTitle("Создание типовой операции");
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.showAndWait();

        // после закрытия — обновляем список
        loadTypeOperations();
    }

    @FXML
    private void onAddOperation() {
        VBox operationBlock = styled(new VBox(5), "operation-block");
        operationBlock.setUserData(null);
        operationBlock.setMaxWidth(Double.MAX_VALUE);
        // --- строка операции ---
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

        operRow.getChildren().addAll(
                numOper, nomInstr, oborud, ostnas, name, shifr, zech, addFuncBtn, delOperBtn
        );
        growOperationFields(numOper, nomInstr, oborud, ostnas, name, shifr, zech);

        // --- контейнер функций ---
        VBox funcsContainer = styled(new VBox(4), "funcs-container");
        funcsContainer.setMaxWidth(Double.MAX_VALUE);

        addFuncBtn.setOnAction(e -> addFunctionRow(funcsContainer));
        delOperBtn.setOnAction(e -> operationsContainer.getChildren().remove(operationBlock));
        operationBlock.getChildren().addAll(operRow, funcsContainer);
        operationsContainer.getChildren().add(operationBlock);
    }

    private void addFunctionRow(VBox funcsContainer) {
        // если первая функция — добавляем шапку
        if (funcsContainer.getChildren().isEmpty()) {
            ensureFunctionHeader(funcsContainer);
        }

        HBox funcRow = styled(new HBox(6), "function-row");
        funcRow.getStyleClass().add("function-data-row");
        funcRow.setMaxWidth(Double.MAX_VALUE);
        funcRow.setAlignment(Pos.CENTER_LEFT);

        TextField name = styled(new TextField(), "compact");
        name.setPromptText("Описание");
        TextField param = styled(new TextField(), "no-compact");
        param.setPromptText("Параметры");
        CheckBox isProd = new CheckBox();
        TextField spec = styled(new TextField(), "compact");
        spec.setPromptText("Спец. характеристики");

        Button delBtn = styled(new Button("Удалить"), "delButton");
        delBtn.setOnAction(e -> {
            funcsContainer.getChildren().remove(funcRow);
            // если больше нет строк функций — удаляем шапку
            boolean hasDataRows = funcsContainer.getChildren().stream()
                    .anyMatch(n -> n.getStyleClass().contains("function-data-row"));
            if (!hasDataRows) {
                funcsContainer.getChildren().removeIf(
                        n -> n.getStyleClass().contains("function-header-row")
                );
            }

        });

        funcRow.getChildren().addAll(name, param, isProd, spec, delBtn);
        growFunctionFields(name, param, spec);
        setFixedWidth(isProd, 70);
        setFixedWidth(delBtn, 90);
        funcsContainer.getChildren().add(funcRow);
    }

    private void growOperationFields(TextField numOper, TextField nomInstr, TextField oborud,
                                     TextField ostnas, TextField name, TextField shifr, TextField zech) {
        setGrow(numOper, 80);
        setGrow(nomInstr, 110);
        setGrow(oborud, 120);
        setGrow(ostnas, 140);
        setGrow(name, 160);
        setGrow(shifr, 80);
        setGrow(zech, 60);
    }

    private void growFunctionFields(TextField name, TextField param, TextField spec) {
        setGrow(name, 180);
        setGrow(param, 220);
        setGrow(spec, 180);
    }

    private void growFunctionHeader(Label name, Label param, Label spec) {
        setGrow(name, 180);
        setGrow(param, 220);
        setGrow(spec, 180);
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

    private void ensureFunctionHeader(VBox funcsContainer) {
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

    private void cleanupFunctionHeader(VBox funcsContainer) {
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
            if (pckgDto.sborEds().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну сборочную единицу");
            if (pckgDto.opers().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну операцию");
            if (pckgDto.opers().stream().anyMatch(o -> o.funcs().isEmpty())) throw new IllegalStateException("Каждая операция должна содержать хотя бы одну функцию");

            var dto = service.saveFullPackage(pckgDto);
            generators.forEach(g -> Try.run(() -> g.generate(dto))
                    .onFailure(ex -> log.error("Ошибка генерации документа", ex))
                    .getOrElseThrow(ex -> new RuntimeException(ex)));
            new Alert(Alert.AlertType.INFORMATION, "Документ сформирован").showAndWait();
        } catch (Exception ex) {
            log.error("Ошибка генерации", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
