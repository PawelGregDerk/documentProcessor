package by.vstu.isit.documentprocessor.ui;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class OperationListManagerTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already initialized
        }
    }

    @Test
    @DisplayName("addOperation/remove: добавляет блок с разделителем и удаляет его")
    void addAndRemoveOperation_updatesContainer() {
        VBox container = new VBox();
        OperationListManager manager = new OperationListManager(container, index -> {});

        manager.addOperation();
        assertThat(manager.blockCount()).isEqualTo(1);
        assertThat(container.getChildren()).hasSize(2);

        manager.addOperation();
        assertThat(manager.blockCount()).isEqualTo(2);
        assertThat(container.getChildren()).hasSize(4);

        manager.remove(container.getChildren().get(3));
        assertThat(manager.blockCount()).isEqualTo(1);
        assertThat(container.getChildren()).hasSize(2);
    }

    @Test
    @DisplayName("insertAt: вставляет блок типовой операции в нужное место")
    void insertAt_addsTypicalOperationBlock() {
        VBox container = new VBox();
        OperationListManager manager = new OperationListManager(container, index -> {});
        TypeOperFuncDto func = new TypeOperFuncDto(1L, "Функция", 1L, "Параметр", true, "");
        TypeOperDto typeOper = new TypeOperDto(
                1L, "001", "15-01", "Станок", "Оснастка", "Операция", "Ш", "15", List.of(func));

        manager.insertAt(0, typeOper);

        assertThat(manager.blockCount()).isEqualTo(1);
        VBox block = (VBox) container.getChildren().get(1);
        assertThat(block.getStyleClass()).contains("operation-block");
        assertThat(((VBox) block.getChildren().get(1)).getChildren()).hasSize(2);
    }

    @Test
    @DisplayName("wrapDeleteHandlers: кнопка удаления удаляет блок")
    void wrapDeleteHandlers_rebindsDeleteButton() {
        VBox container = new VBox();
        OperationListManager manager = new OperationListManager(container, index -> {});
        manager.addOperation();
        manager.wrapDeleteHandlers();

        VBox block = (VBox) container.getChildren().get(1);
        HBox row = (HBox) block.getChildren().get(0);
        ((Button) row.getChildren().get(8)).fire();

        assertThat(manager.blockCount()).isZero();
        assertThat(container.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("separator: создаёт кнопку добавления операции")
    void separator_createsAddOperationButton() {
        VBox container = new VBox();
        AtomicInteger openedAt = new AtomicInteger(-1);
        OperationListManager manager = new OperationListManager(container, openedAt::set);
        manager.addOperation();

        HBox separator = (HBox) container.getChildren().get(0);
        Button plus = (Button) separator.getChildren().get(0);
        // Меню не открываем — проверяем, что кнопка создана и привязана
        assertThat(plus.getText()).isEqualTo("Добавить операцию");
    }
}
