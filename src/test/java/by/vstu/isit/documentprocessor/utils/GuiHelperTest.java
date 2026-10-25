package by.vstu.isit.documentprocessor.utils;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

    @Test
    @DisplayName("setGrow: sets min width, max width and horizontal grow")
    void setGrow_setsMinWidthAndHgrow() {
        Region region = new Region();

        GuiHelper.setGrow(region, 120);

        assertEquals(120, region.getMinWidth());
        assertEquals(Double.MAX_VALUE, region.getMaxWidth());
        assertEquals(Priority.ALWAYS, HBox.getHgrow(region));
    }

    @Test
    @DisplayName("setFixedWidth: sets min, pref and max width")
    void setFixedWidth_setsAllWidths() {
        Region region = new Region();

        GuiHelper.setFixedWidth(region, 70);

        assertEquals(70, region.getMinWidth());
        assertEquals(70, region.getPrefWidth());
        assertEquals(70, region.getMaxWidth());
    }

    @Test
    @DisplayName("growFunctionHeader: all three labels get width and horizontal grow")
    void growFunctionHeader_setsWidthsForAllLabels() {
        Label name = new Label("name");
        Label param = new Label("param");
        Label spec = new Label("spec");

        GuiHelper.growFunctionHeader(name, param, spec);

        for (Label label : new Label[]{name, param, spec}) {
            assertEquals(140, label.getMinWidth());
            assertEquals(Double.MAX_VALUE, label.getMaxWidth());
            assertEquals(Priority.ALWAYS, HBox.getHgrow(label));
        }
    }
}
