package by.vstu.isit.documentprocessor.ui;

import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.function.Consumer;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@UtilityClass
public class OperationBlockFactory {

    private static final double[] OPERATION_WIDTHS = {80, 110, 120, 140, 160, 80, 60};
    private static final String[] OPERATION_PROMPTS = {
            "№ операции", "№ инструкции", "Оборудование", "Оснастка / Инструмент",
            "Наименование", "Шифр", "Цех"
    };

    public VBox createEmptyOperationBlock(Consumer<Node> onDelete) {
        return createOperationBlock(new String[7], true, null, onDelete);
    }

    public VBox createOperationBlock(TypeOperDto typeOper, Consumer<Node> onDelete) {
        VBox block = createOperationBlock(new String[]{
                typeOper.numOper(), typeOper.nomInstr(), typeOper.oborud(),
                typeOper.ostnasInstr(), typeOper.name(), typeOper.shifr(), typeOper.numZech()
        }, false, null, onDelete);
        addTypeFunctions(block, typeOper.funcs());
        return block;
    }

    public VBox createOperationBlock(OperDto dto, Consumer<Node> onDelete) {
        VBox block = createOperationBlock(new String[]{
                dto.numOper(), dto.nomInstr(), dto.oborud(),
                dto.ostnasInstr(), dto.name(), dto.shifr(), dto.numZech()
        }, false, dto.id(), onDelete);
        addDtoFunctions(block, dto.funcs());
        return block;
    }

    public void addFunctionRow(VBox funcs) {
        FunctionHeaderManager.ensureHeader(funcs);
        funcs.getChildren().add(createFunctionRow("", "", false, "", null));
    }

    public HBox createFunctionRowFromType(TypeOperFuncDto func) {
        return createFunctionRow(
                func.name(), func.param(), Boolean.TRUE.equals(func.isProd()), func.specCharakt(), null);
    }

    public HBox createFunctionRow(FuncDto func) {
        return createFunctionRow(
                func.name(), func.param(), Boolean.TRUE.equals(func.isProd()), func.specCharakt(), func.id());
    }

    private VBox createOperationBlock(String[] values, boolean withPrompts,
                                      Long userData, Consumer<Node> onDelete) {
        VBox block = styled(new VBox(5), "operation-block");
        block.setUserData(userData);
        block.setMaxWidth(Double.MAX_VALUE);

        HBox row = createOperationRow(values, withPrompts);
        Button addFuncBtn = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        Button delOperBtn = styled(new Button("Удалить"), "btn-danger", "btn-small");
        row.getChildren().addAll(addFuncBtn, delOperBtn);

        VBox funcs = styled(new VBox(4), "funcs-container");
        funcs.setMaxWidth(Double.MAX_VALUE);

        addFuncBtn.setOnAction(e -> addFunctionRow(funcs));
        delOperBtn.setOnAction(e -> onDelete.accept(block));

        block.getChildren().addAll(row, funcs);
        return block;
    }

    private HBox createOperationRow(String[] values, boolean withPrompts) {
        HBox row = styled(new HBox(6), "operation-row");
        row.setMaxWidth(Double.MAX_VALUE);
        row.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < OPERATION_WIDTHS.length; i++) {
            TextArea field = styled(new TextArea(values[i] != null ? values[i] : ""), "compact");
            field.setPrefRowCount(2);
            field.setWrapText(true);
            if (withPrompts) {
                field.setPromptText(OPERATION_PROMPTS[i]);
            }
            setGrow(field, OPERATION_WIDTHS[i]);
            row.getChildren().add(field);
        }
        return row;
    }

    private void addTypeFunctions(VBox block, List<TypeOperFuncDto> funcs) {
        if (funcs == null || funcs.isEmpty()) {
            return;
        }
        VBox container = funcsContainer(block);
        FunctionHeaderManager.ensureHeader(container);
        funcs.forEach(func -> container.getChildren().add(createFunctionRowFromType(func)));
    }

    private void addDtoFunctions(VBox block, List<FuncDto> funcs) {
        if (funcs == null || funcs.isEmpty()) {
            return;
        }
        VBox container = funcsContainer(block);
        FunctionHeaderManager.ensureHeader(container);
        funcs.forEach(func -> container.getChildren().add(createFunctionRow(func)));
    }

    private VBox funcsContainer(VBox block) {
        return (VBox) block.getChildren().get(1);
    }

    private HBox createFunctionRow(String nameValue, String paramValue,
                                   boolean isProdValue, String specValue, Long userData) {
        HBox row = styled(new HBox(6), "function-row");
        row.getStyleClass().add("function-data-row");
        row.setUserData(userData);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setAlignment(Pos.CENTER_LEFT);

        TextArea name = styled(new TextArea(nameValue != null ? nameValue : ""), "compact");
        name.setPromptText("Описание");
        name.setPrefRowCount(2);
        name.setWrapText(true);
        TextArea param = styled(new TextArea(paramValue != null ? paramValue : ""), "compact");
        param.setPromptText("Параметры");
        param.setPrefRowCount(2);
        param.setWrapText(true);
        CheckBox isProd = new CheckBox();
        isProd.setSelected(isProdValue);
        isProd.setPrefWidth(120);
        StackPane checkWrap = new StackPane(isProd);
        checkWrap.setAlignment(Pos.CENTER);
        TextArea spec = styled(new TextArea(specValue != null ? specValue : ""), "compact");
        spec.setPromptText("Спец. характеристики");
        spec.setPrefRowCount(2);
        spec.setWrapText(true);

        Button delBtn = styled(new Button("Удалить"), "delButton");
        delBtn.setOnAction(e -> {
            VBox parent = (VBox) row.getParent();
            if (parent != null) {
                parent.getChildren().remove(row);
                FunctionHeaderManager.cleanupIfEmpty(parent);
            }
        });

        row.getChildren().addAll(name, param, checkWrap, spec, delBtn);
        setGrow(name, 140);
        setGrow(param, 140);
        setGrow(spec, 140);
        setFixedWidth(checkWrap, 70);
        setFixedWidth(delBtn, 90);
        return row;
    }
}
