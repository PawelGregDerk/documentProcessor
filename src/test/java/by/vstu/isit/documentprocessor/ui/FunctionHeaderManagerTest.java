package by.vstu.isit.documentprocessor.ui;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionHeaderManagerTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already initialized
        }
    }

    @Test
    @DisplayName("ensureHeader: adds header only once")
    void ensureHeader_addsHeaderOnlyOnce() {
        VBox funcs = new VBox();

        FunctionHeaderManager.ensureHeader(funcs);
        FunctionHeaderManager.ensureHeader(funcs);

        assertThat(funcs.getChildren()).hasSize(1);
        assertThat(funcs.getChildren().getFirst().getStyleClass())
                .contains("function-header", "function-header-row");
    }

    @Test
    @DisplayName("cleanupIfEmpty: removes header when there are no data rows")
    void cleanupIfEmpty_removesHeaderWhenNoDataRows() {
        VBox funcs = new VBox();
        FunctionHeaderManager.ensureHeader(funcs);

        FunctionHeaderManager.cleanupIfEmpty(funcs);

        assertThat(funcs.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("cleanupIfEmpty: keeps header while data rows exist")
    void cleanupIfEmpty_keepsHeaderWhenDataRowsExist() {
        VBox funcs = new VBox();
        FunctionHeaderManager.ensureHeader(funcs);
        HBox row = new HBox();
        row.getStyleClass().add("function-data-row");
        funcs.getChildren().add(row);

        FunctionHeaderManager.cleanupIfEmpty(funcs);

        assertThat(funcs.getChildren()).hasSize(2);
    }
}
