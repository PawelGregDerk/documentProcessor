package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DocPackageCreateControllerTest {

    @Mock
    private DockPackageFormMapper formMapper;
    @Mock
    private DocpackageService service;
    @Mock
    private TypeOperService typeOperService;
    @Mock
    private FxWeaver fxWeaver;
    @Mock
    private AlertService alertService;

    private DocPackageCreateController controller;
    private TextField packageNameField;
    private TextField puField;
    private TextField spuField;
    private TextField kpField;
    private TextField fmeaField;
    private TextField vedInstrField;
    private TextField sborEdNazv;
    private TextField packagePathField;
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
        controller = new DocPackageCreateController(
                formMapper, service, typeOperService, List.of(), fxWeaver, alertService
        );

        packageNameField = new TextField();
        puField = new TextField();
        spuField = new TextField();
        kpField = new TextField();
        fmeaField = new TextField();
        vedInstrField = new TextField();
        sborEdNazv = new TextField();
        packagePathField = new TextField();
        assemblyUnitsContainer = new VBox();
        operationsContainer = new VBox();

        ReflectionTestUtils.setField(controller, "packageNameField", packageNameField);
        ReflectionTestUtils.setField(controller, "puField", puField);
        ReflectionTestUtils.setField(controller, "spuField", spuField);
        ReflectionTestUtils.setField(controller, "kpField", kpField);
        ReflectionTestUtils.setField(controller, "fmeaField", fmeaField);
        ReflectionTestUtils.setField(controller, "vedInstrField", vedInstrField);
        ReflectionTestUtils.setField(controller, "sborEdNazv", sborEdNazv);
        ReflectionTestUtils.setField(controller, "packagePathField", packagePathField);
        ReflectionTestUtils.setField(controller, "assemblyUnitsContainer", assemblyUnitsContainer);
        ReflectionTestUtils.setField(controller, "operationsContainer", operationsContainer);
    }

    @Test
    @DisplayName("onAddAssemblyUnit: adds new row into assemblyUnitsContainer")
    void onAddAssemblyUnit_addsNewRow() {
        ReflectionTestUtils.invokeMethod(controller, "onAddAssemblyUnit");
        assertEquals(1, assemblyUnitsContainer.getChildren().size());

        ReflectionTestUtils.invokeMethod(controller, "onAddAssemblyUnit");
        assertEquals(2, assemblyUnitsContainer.getChildren().size());
    }

    @Test
    @DisplayName("onAddOperation: adds new operation block into operationsContainer")
    void onAddOperation_addsNewBlock() {
        ReflectionTestUtils.invokeMethod(controller, "onAddOperation");
        assertEquals(1, operationsContainer.getChildren().size());
    }

    @Test
    @DisplayName("addTypeOperationToRegular: converts TypeOperDto into UI block in operationsContainer")
    void addTypeOperationToRegular_convertsTypeOperDto() {
        TypeOperFuncDto func = new TypeOperFuncDto(1L, "Func 1", 10L, "Param 1", true, "Spec 1");
        TypeOperDto typeOper = new TypeOperDto(10L, "001", "15-01", "Mach", "Tool", "Oper", "SH", "15", List.of(func));

        ReflectionTestUtils.invokeMethod(controller, "addTypeOperationToRegular", typeOper);

        assertEquals(1, operationsContainer.getChildren().size());
    }
}
