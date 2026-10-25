package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TypeOperEditControllerTest {

    @Mock
    private TypeOperService typeOperService;

    private TypeOperEditController controller;
    private TextArea numOperField;
    private TextArea nomInstrField;
    private TextArea oborudField;
    private TextArea ostnasInstrField;
    private TextArea nameField;
    private TextArea shifrField;
    private TextArea numZechField;
    private VBox funcContainer;
    private HBox funcHeader;

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
        controller = new TypeOperEditController(typeOperService);
        numOperField = new TextArea();
        nomInstrField = new TextArea();
        oborudField = new TextArea();
        ostnasInstrField = new TextArea();
        nameField = new TextArea();
        shifrField = new TextArea();
        numZechField = new TextArea();
        funcContainer = new VBox();
        funcHeader = new HBox();

        ReflectionTestUtils.setField(controller, "numOperField", numOperField);
        ReflectionTestUtils.setField(controller, "nomInstrField", nomInstrField);
        ReflectionTestUtils.setField(controller, "oborudField", oborudField);
        ReflectionTestUtils.setField(controller, "ostnasInstrField", ostnasInstrField);
        ReflectionTestUtils.setField(controller, "nameField", nameField);
        ReflectionTestUtils.setField(controller, "shifrField", shifrField);
        ReflectionTestUtils.setField(controller, "numZechField", numZechField);
        ReflectionTestUtils.setField(controller, "funcContainer", funcContainer);
        ReflectionTestUtils.setField(controller, "funcHeader", funcHeader);
        ReflectionTestUtils.setField(controller, "funcRows", new ArrayList<HBox>());
    }

    @Test
    @DisplayName("initForEdit/initForCreate: sets and clears edit data")
    void initForEditAndCreate_setsAndClearsData() {
        TypeOperDto dto = new TypeOperDto(1L, "001", "15-01", "M", "T", "Name", "S", "15", List.of());

        controller.initForEdit(dto);
        assertEquals(dto, ReflectionTestUtils.getField(controller, "editData"));

        controller.initForCreate();
        assertNull(ReflectionTestUtils.getField(controller, "editData"));
    }

    @Test
    @DisplayName("populateFromDto: populates all TextAreas and function rows from DTO")
    void populateFromDto_validDto_populatesAllFields() {
        TypeOperFuncDto func = new TypeOperFuncDto(10L, "Func 1", 1L, "Param 1", true, "Spec 1");
        TypeOperDto dto = new TypeOperDto(1L, "010", "15-01/1", "Станок ЧПУ", "Резец", "Точение", "Т-1", "15", List.of(func));

        ReflectionTestUtils.invokeMethod(controller, "populateFromDto", dto);

        assertEquals("010", numOperField.getText());
        assertEquals("15-01/1", nomInstrField.getText());
        assertEquals("Станок ЧПУ", oborudField.getText());
        assertEquals("Резец", ostnasInstrField.getText());
        assertEquals("Точение", nameField.getText());
        assertEquals("Т-1", shifrField.getText());
        assertEquals("15", numZechField.getText());

        assertEquals(1, funcContainer.getChildren().size());
        List<HBox> rows = (List<HBox>) ReflectionTestUtils.getField(controller, "funcRows");
        assertNotNull(rows);
        assertEquals(1, rows.size());
    }

    @Test
    @DisplayName("onAddFunction: adds new function row to funcContainer and funcRows")
    void onAddFunction_addsNewRow() {
        controller.onAddFunction();

        assertEquals(1, funcContainer.getChildren().size());
        List<HBox> rows = (List<HBox>) ReflectionTestUtils.getField(controller, "funcRows");
        assertNotNull(rows);
        assertEquals(1, rows.size());
    }
}
