package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxWeaver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock
    private FxWeaver fxWeaver;
    @Mock
    private DockPackageGuiMapper guiMapper;
    @Mock
    private DocpackageService docpackageService;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Already initialized
        }
    }

    @Test
    @DisplayName("constructor and dependency injection: correctly sets final fields")
    void constructor_setsInjectedFields() {
        MainController controller = new MainController(fxWeaver, guiMapper, docpackageService);

        assertSame(fxWeaver, ReflectionTestUtils.getField(controller, "fxWeaver"));
        assertSame(guiMapper, ReflectionTestUtils.getField(controller, "guiMapper"));
        assertSame(docpackageService, ReflectionTestUtils.getField(controller, "docpackageService"));

        VBox box = new VBox();
        ReflectionTestUtils.setField(controller, "mainVBox", box);
        assertSame(box, ReflectionTestUtils.getField(controller, "mainVBox"));
    }
}
