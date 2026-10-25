package by.vstu.isit.documentprocessor.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AssemblyUnitRowFactoryTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already initialized
        }
    }

    @Test
    @DisplayName("createRow: builds field and delete button, keeps value and user data")
    void createRow_buildsRowWithValueAndUserData() {
        AtomicReference<Node> removed = new AtomicReference<>();
        HBox row = AssemblyUnitRowFactory.createRow("АБВГ.123", 42L, removed::set);

        assertThat(row.getUserData()).isEqualTo(42L);
        assertThat(row.getChildren()).hasSize(2);
        assertThat(row.getChildren().get(0)).isInstanceOf(TextField.class);
        assertThat(((TextField) row.getChildren().get(0)).getText()).isEqualTo("АБВГ.123");
        assertThat(row.getChildren().get(1)).isInstanceOf(Button.class);

        ((Button) row.getChildren().get(1)).fire();

        assertThat(removed.get()).isSameAs(row);
    }

    @Test
    @DisplayName("createRow: empty value shows prompt")
    void createRow_emptyValue_showsPrompt() {
        HBox row = AssemblyUnitRowFactory.createRow(null, null, node -> {});

        TextField field = (TextField) row.getChildren().get(0);
        assertThat(field.getText()).isEmpty();
        assertThat(field.getPromptText()).isEqualTo("Обозначение сборочной единицы");
    }
}
