package by.vstu.isit.documentprocessor.utils;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GuiHelperTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Already initialized
        }
    }

    @Test
    @DisplayName("styled: adds styles to node without duplicate classes")
    void styled_addsNewStyles_avoidsDuplicates() {
        Label label = new Label("Test");
        label.getStyleClass().add("existing-class");

        GuiHelper.styled(label, "new-class-1", "existing-class", "new-class-2");

        assertTrue(label.getStyleClass().contains("existing-class"));
        assertTrue(label.getStyleClass().contains("new-class-1"));
        assertTrue(label.getStyleClass().contains("new-class-2"));

        // Verify "existing-class" was not duplicated
        long count = label.getStyleClass().stream().filter("existing-class"::equals).count();
        assertEquals(1, count);
    }
}
