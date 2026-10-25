package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeOperListControllerTest {

    @Mock
    private TypeOperService typeOperService;

    @Mock
    private FxWeaver fxWeaver;

    private TypeOperListController controller;
    private TextField searchField;
    private VBox listContainer;

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
        controller = new TypeOperListController(typeOperService, fxWeaver);
        searchField = new TextField();
        listContainer = new VBox();
        ReflectionTestUtils.setField(controller, "searchField", searchField);
        ReflectionTestUtils.setField(controller, "listContainer", listContainer);
    }

    @Test
    @DisplayName("initAddCallback: sets and clears add callback")
    void initAddCallback_setsAndClearsCallback() {
        AtomicReference<TypeOperDto> received = new AtomicReference<>();
        controller.initAddCallback(received::set);

        TypeOperDto dto = new TypeOperDto(1L, "001", "15-01", "M", "T", "Name", "S", "15", List.of());
        var callback = (java.util.function.Consumer<TypeOperDto>) ReflectionTestUtils.getField(controller, "addCallback");
        assertNotNull(callback);
        callback.accept(dto);
        assertEquals(dto, received.get());

        controller.initAddCallback(null);
        assertNull(ReflectionTestUtils.getField(controller, "addCallback"));
    }

    @Test
    @DisplayName("initialize: loads type operations from service and displays them in listContainer")
    void initialize_loadsDataFromService() {
        TypeOperDto op1 = new TypeOperDto(1L, "01", "WI-1", "Mach", "Tool", "Токарная", "SH", "15", List.of());
        TypeOperDto op2 = new TypeOperDto(2L, "02", "WI-2", "Mach", "Tool", "Фрезерная", "SH", "12", List.of());
        when(typeOperService.findAll()).thenReturn(List.of(op1, op2));

        controller.initialize(null, null);

        assertEquals(2, listContainer.getChildren().size());
    }

    @Test
    @DisplayName("onSearch: filters displayed operations by name or shop number")
    void onSearch_filtersListByName() {
        TypeOperDto op1 = new TypeOperDto(1L, "01", "WI-1", "Mach", "Tool", "Токарная", "SH", "15", List.of());
        TypeOperDto op2 = new TypeOperDto(2L, "02", "WI-2", "Mach", "Tool", "Фрезерная", "SH", "12", List.of());
        when(typeOperService.findAll()).thenReturn(List.of(op1, op2));

        controller.initialize(null, null);
        assertEquals(2, listContainer.getChildren().size());

        searchField.setText("токар");
        ReflectionTestUtils.invokeMethod(controller, "onSearch");

        assertEquals(1, listContainer.getChildren().size());

        searchField.setText("12");
        ReflectionTestUtils.invokeMethod(controller, "onSearch");

        assertEquals(1, listContainer.getChildren().size());
    }

    @Test
    @DisplayName("onResetSearch: clears search field and restores all operations")
    void onResetSearch_restoresAllOperations() {
        TypeOperDto op1 = new TypeOperDto(1L, "01", "WI-1", "Mach", "Tool", "Токарная", "SH", "15", List.of());
        TypeOperDto op2 = new TypeOperDto(2L, "02", "WI-2", "Mach", "Tool", "Фрезерная", "SH", "12", List.of());
        when(typeOperService.findAll()).thenReturn(List.of(op1, op2));

        controller.initialize(null, null);

        searchField.setText("несуществующий");
        ReflectionTestUtils.invokeMethod(controller, "onSearch");
        assertEquals(0, listContainer.getChildren().size());

        ReflectionTestUtils.invokeMethod(controller, "onResetSearch");
        assertEquals("", searchField.getText());
        assertEquals(2, listContainer.getChildren().size());
    }
}
