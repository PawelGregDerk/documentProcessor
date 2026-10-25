package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageListControllerTest {

    @Mock
    private FxWeaver fxWeaver;

    @Mock
    private DocpackageService docpackageService;

    @Mock
    private DockPackageGuiMapper guiMapper;

    private PackageListController controller;
    private VBox packagesContainer;
    private TextField searchField;

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
        controller = new PackageListController(fxWeaver, docpackageService, guiMapper);
        packagesContainer = new VBox();
        searchField = new TextField();

        ReflectionTestUtils.setField(controller, "packagesContainer", packagesContainer);
        ReflectionTestUtils.setField(controller, "searchField", searchField);
    }

    @Test
    @DisplayName("initialize: loads packages from service and renders rows in packagesContainer")
    void initialize_loadsPackagesAndRendersRows() {
        SborEdDto sborEd = new SborEdDto(1L, 1L, "Деталь", "АБВГ.123");
        DockPackageDto pkg = new DockPackageDto(1L, "Пакет-01", "path", "pu", "spu", "kp", "fm", "vi",
                List.of(), List.of(sborEd));

        when(docpackageService.searchByPackageName("")).thenReturn(new ArrayList<>(List.of(pkg)));

        controller.initialize();

        assertEquals(1, packagesContainer.getChildren().size());
    }

    @Test
    @DisplayName("onResetSearch: clears searchField and reloads all packages")
    void onResetSearch_clearsFieldAndReloads() {
        when(docpackageService.searchByPackageName("")).thenReturn(new ArrayList<>());

        searchField.setText("custom query");
        ReflectionTestUtils.invokeMethod(controller, "onResetSearch");

        assertEquals("", searchField.getText());
    }

    @Test
    @DisplayName("onSearch: searches by package name and assembly designation")
    void onSearch_queriesServicesAndRendersDistinctResults() {
        DockPackageDto pkg1 = new DockPackageDto(1L, "Пакет-01", "path", "pu", "spu", "kp", "fm", "vi", List.of(), List.of());
        DockPackageDto pkg2 = new DockPackageDto(2L, "Пакет-02", "path", "pu", "spu", "kp", "fm", "vi", List.of(), List.of());

        searchField.setText("Пакет");
        when(docpackageService.searchByPackageName("Пакет")).thenReturn(new ArrayList<>(List.of(pkg1)));
        when(docpackageService.searchByOboznach("Пакет")).thenReturn(new ArrayList<>(List.of(pkg2)));

        ReflectionTestUtils.invokeMethod(controller, "onSearch");

        assertEquals(2, packagesContainer.getChildren().size());
    }
}
