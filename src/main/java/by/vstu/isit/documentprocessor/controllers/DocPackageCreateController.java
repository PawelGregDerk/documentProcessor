package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
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
import io.vavr.control.Try;
import org.springframework.stereotype.Controller;

import java.io.File;
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

    @Value("${out.pu.path}")
    private String outPuPath;

    @FXML
    private void initialize() {
    }

    private void addTypeOperationToRegular(TypeOperDto typeOper) {
        VBox operationBlock = styled(new VBox(5), "operation-block");
        operationBlock.setUserData(null);

        HBox operRow = styled(new HBox(6), "operation-row");
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
        growOperationFields(numOper, nomInstr, oborud, ostnas, name, shifr, zech);
        Button addFuncBtn = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        Button delOperBtn = styled(new Button("Удалить"), "btn-danger", "btn-small");

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
        isProd.setPrefWidth(120);
        StackPane checkWrap = new StackPane(isProd);
        checkWrap.setAlignment(Pos.CENTER);

        TextArea spec = styled(new TextArea(typeFunc.specCharakt() != null ? typeFunc.specCharakt() : ""), "compact");
        spec.setPromptText("Спец. характеристики");
        spec.setPrefRowCount(2);
        spec.setWrapText(true);

        Button delBtn = styled(new Button("Удалить"), "delButton");
        delBtn.setOnAction(e -> {
            VBox parent = (VBox) funcRow.getParent();
            parent.getChildren().remove(funcRow);
            cleanupFunctionHeader(parent);
        });

        funcRow.getChildren().addAll(name, param, checkWrap, spec, delBtn);
        growFunctionFields(name, param, spec);
        setFixedWidth(checkWrap, 70);
        setFixedWidth(delBtn, 90);
        return funcRow;
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
        TypeOperListController.setOnAddCallback(this::addTypeOperationToRegular);
        Stage stage = new Stage();
        Parent root = fxWeaver.loadView(TypeOperListController.class);
        stage.setScene(new Scene(root));
        stage.setTitle("Типовые операции");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        TypeOperListController.clearCallback();
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

        funcRow.getChildren().addAll(name, param, checkWrap, spec, delBtn);
        growFunctionFields(name, param, spec);
        setFixedWidth(checkWrap, 70);
        setFixedWidth(delBtn, 90);
        funcsContainer.getChildren().add(funcRow);
    }

    private void growOperationFields(Region numOper, Region nomInstr, Region oborud,
                                     Region ostnas, Region name, Region shifr, Region zech) {
        setGrow(numOper, 80);
        setGrow(nomInstr, 110);
        setGrow(oborud, 120);
        setGrow(ostnas, 140);
        setGrow(name, 160);
        setGrow(shifr, 80);
        setGrow(zech, 60);
    }

    private void growFunctionFields(Region name, Region param, Region spec) {
        setGrow(name, 140);
        setGrow(param, 140);
        setGrow(spec, 140);
    }

    private void growFunctionHeader(Label name, Label param, Label spec) {
        setGrow(name, 140);
        setGrow(param, 140);
        setGrow(spec, 140);
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

            String base = outPuPath.substring(0, outPuPath.indexOf("{0}"));
            if (new File(base + pckgDto.path()).exists()) {
                throw new IllegalStateException("Папка с оригиналами уже существует: " + base + pckgDto.path());
            }

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
            if (pckgDto.sborEds().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну сборочную единицу");
            if (pckgDto.opers().isEmpty()) throw new IllegalStateException("Необходимо указать хотя бы одну операцию");
            if (pckgDto.opers().stream().anyMatch(o -> o.funcs().isEmpty())) throw new IllegalStateException("Каждая операция должна содержать хотя бы одну функцию");

            service.saveFullPackage(pckgDto);

            new Alert(Alert.AlertType.INFORMATION, "Пакет сохранён в базу данных").showAndWait();
        } catch (Exception ex) {
            log.error("Ошибка сохранения в БД", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
