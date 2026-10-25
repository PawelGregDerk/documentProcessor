package by.vstu.isit.documentprocessor.ui;

import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class OperationBlockFactoryTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already initialized
        }
    }

    @Test
    @DisplayName("createEmptyOperationBlock: builds row, function container and delete callback")
    void createEmptyOperationBlock_buildsBlock() {
        AtomicReference<Node> removed = new AtomicReference<>();

        VBox block = OperationBlockFactory.createEmptyOperationBlock(removed::set);

        assertThat(block.getStyleClass()).contains("operation-block");
        assertThat(block.getChildren()).hasSize(2);
        HBox row = (HBox) block.getChildren().get(0);
        VBox funcs = (VBox) block.getChildren().get(1);
        assertThat(row.getChildren()).hasSize(9);
        assertThat(funcs.getChildren()).isEmpty();
        assertThat(((TextArea) row.getChildren().get(0)).getPromptText()).isEqualTo("№ операции");

        ((Button) row.getChildren().get(8)).fire();
        assertThat(removed.get()).isSameAs(block);
    }

    @Test
    @DisplayName("createOperationBlock: fills fields and adds header with function row")
    void createOperationBlock_fromTypeOper_fillsFieldsAndFunctions() {
        TypeOperFuncDto func = new TypeOperFuncDto(1L, "Функция", 10L, "Параметр", true, "Спец");
        TypeOperDto typeOper = new TypeOperDto(
                10L, "001", "15-01", "Станок", "Оснастка", "Операция", "Ш", "15", List.of(func));

        VBox block = OperationBlockFactory.createOperationBlock(typeOper, node -> {});

        HBox row = (HBox) block.getChildren().get(0);
        assertThat(((TextArea) row.getChildren().get(0)).getText()).isEqualTo("001");
        assertThat(((TextArea) row.getChildren().get(4)).getText()).isEqualTo("Операция");

        VBox funcs = (VBox) block.getChildren().get(1);
        assertThat(funcs.getChildren()).hasSize(2);
        assertThat(funcs.getChildren().get(0).getStyleClass()).contains("function-header-row");

        HBox functionRow = (HBox) funcs.getChildren().get(1);
        assertThat(functionRow.getStyleClass()).contains("function-data-row");
        assertThat(((TextArea) functionRow.getChildren().get(0)).getText()).isEqualTo("Функция");
        assertThat(((TextArea) functionRow.getChildren().get(1)).getText()).isEqualTo("Параметр");
        assertThat(((TextArea) functionRow.getChildren().get(3)).getText()).isEqualTo("Спец");
        CheckBox isProd = (CheckBox) ((StackPane) functionRow.getChildren().get(2)).getChildren().getFirst();
        assertThat(isProd.isSelected()).isTrue();
    }

    @Test
    @DisplayName("addFunctionRow: adds one header and rows, removes header after last row")
    void addFunctionRow_addsHeaderAndRows() {
        VBox funcs = new VBox();

        OperationBlockFactory.addFunctionRow(funcs);
        OperationBlockFactory.addFunctionRow(funcs);

        assertThat(funcs.getChildren()).hasSize(3);
        assertThat(funcs.getChildren().get(0).getStyleClass()).contains("function-header-row");
        assertThat(funcs.getChildren().get(1).getStyleClass()).contains("function-data-row");
        assertThat(funcs.getChildren().get(2).getStyleClass()).contains("function-data-row");

        HBox firstRow = (HBox) funcs.getChildren().get(1);
        ((Button) firstRow.getChildren().get(4)).fire();
        assertThat(funcs.getChildren()).hasSize(2);

        HBox secondRow = (HBox) funcs.getChildren().get(1);
        ((Button) secondRow.getChildren().get(4)).fire();
        assertThat(funcs.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("createOperationBlock: keeps OperDto id on block and function row")
    void createOperationBlock_fromOper_keepsIds() {
        FuncDto func = new FuncDto(7L, "Функция", 5L, "Параметр", false, "Спец");
        OperDto dto = new OperDto(
                5L, 1L, "005", "15-132/005", "Станок", "Оснастка", "Операция", "Ш", "15", List.of(func));

        VBox block = OperationBlockFactory.createOperationBlock(dto, node -> {});

        assertThat(block.getUserData()).isEqualTo(5L);
        VBox funcs = (VBox) block.getChildren().get(1);
        HBox functionRow = (HBox) funcs.getChildren().get(1);
        assertThat(functionRow.getUserData()).isEqualTo(7L);
    }
}
