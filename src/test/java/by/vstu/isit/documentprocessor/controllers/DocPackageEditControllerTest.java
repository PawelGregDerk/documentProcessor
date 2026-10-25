package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;
import by.vstu.isit.documentprocessor.ui.AlertService;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxWeaver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DocPackageEditControllerTest {

    @Mock
    private DocpackageService docpackageService;
    @Mock
    private TypeOperService typeOperService;
    @Mock
    private DockPackageFormMapper formMapper;
    @Mock
    private DockPackageGuiMapper guiMapper;
    @Mock
    private FxWeaver fxWeaver;
    @Mock
    private AlertService alertService;

    private DocPackageEditController controller;
    private TextField packageNameField;
    private TextField puField;
    private TextField spuField;
    private TextField kpField;
    private TextField fmeaField;
    private TextField vedInstrField;
    private TextField sborEdNazv;
    private VBox assemblyUnitsContainer;
    private VBox operationsContainer;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Already initialized
        }
    }

    @BeforeEach
    void setUp() {
        controller = new DocPackageEditController(
                formMapper, guiMapper, List.of(), List.of(), docpackageService, typeOperService, fxWeaver, alertService,
                new PackageFileManager()
        );

        packageNameField = new TextField();
        puField = new TextField();
        spuField = new TextField();
        kpField = new TextField();
        fmeaField = new TextField();
        vedInstrField = new TextField();
        sborEdNazv = new TextField();
        assemblyUnitsContainer = new VBox();
        operationsContainer = new VBox();

        ReflectionTestUtils.setField(controller, "packageNameField", packageNameField);
        ReflectionTestUtils.setField(controller, "puField", puField);
        ReflectionTestUtils.setField(controller, "spuField", spuField);
        ReflectionTestUtils.setField(controller, "kpField", kpField);
        ReflectionTestUtils.setField(controller, "fmeaField", fmeaField);
        ReflectionTestUtils.setField(controller, "vedInstrField", vedInstrField);
        ReflectionTestUtils.setField(controller, "sborEdNazv", sborEdNazv);
        ReflectionTestUtils.setField(controller, "assemblyUnitsContainer", assemblyUnitsContainer);
        ReflectionTestUtils.setField(controller, "operationsContainer", operationsContainer);
    }

    @Test
    @DisplayName("path and packageId: setters and getters store values")
    void pathAndPackageId_settersAndGetters_workCorrectly() {
        controller.setPath("C:/my/package/path");
        assertEquals("C:/my/package/path", controller.getPath());

        controller.setPackageId(42L);
        assertEquals(42L, controller.getPackageId());
    }

    @Test
    @DisplayName("onAddAssemblyUnit: adds new row into assemblyUnitsContainer")
    void onAddAssemblyUnit_addsRow() {
        ReflectionTestUtils.invokeMethod(controller, "onAddAssemblyUnit");
        assertEquals(1, assemblyUnitsContainer.getChildren().size());
    }

    @Test
    @DisplayName("createEmptyOperationBlock: constructs empty operation VBox block")
    void createEmptyOperationBlock_returnsPopulatedBlock() {
        VBox block = ReflectionTestUtils.invokeMethod(controller, "createEmptyOperationBlock");
        assertNotNull(block);
        assertTrue(block.getStyleClass().contains("operation-block"));
        assertEquals(2, block.getChildren().size());
    }

    @Test
    @DisplayName("createOperationBlock: constructs operation VBox block from TypeOperDto")
    void createOperationBlock_convertsTypeOperDto() {
        TypeOperFuncDto func = new TypeOperFuncDto(1L, "Func 1", 10L, "Param 1", false, "Spec 1");
        TypeOperDto typeOper = new TypeOperDto(10L, "001", "15-01", "Mach", "Tool", "Oper", "SH", "15", List.of(func));

        VBox block = ReflectionTestUtils.invokeMethod(controller, "createOperationBlock", typeOper);

        assertNotNull(block);
        assertTrue(block.getStyleClass().contains("operation-block"));
        assertEquals(2, block.getChildren().size());
    }
}
