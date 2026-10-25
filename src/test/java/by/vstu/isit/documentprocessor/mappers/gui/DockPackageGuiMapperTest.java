package by.vstu.isit.documentprocessor.mappers.gui;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DockPackageGuiMapperTest {

    private final DockPackageGuiMapper mapper = new DockPackageGuiMapper();

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    @Test
    @DisplayName("toGui: populates JavaFX text fields and containers from DockPackageDto")
    void toGui_validDto_populatesControls() {
        TextField packageName = new TextField();
        TextField sborEdNazv = new TextField();
        TextField pu = new TextField();
        TextField spu = new TextField();
        TextField kp = new TextField();
        TextField fmea = new TextField();
        TextField vedInstr = new TextField();
        VBox operationsContainer = new VBox();
        VBox assemblyUnitsContainer = new VBox();

        FuncDto func = new FuncDto(1L, "Функция 1", 10L, "Параметр 1", true, "Спец");
        OperDto oper = new OperDto(10L, 1L, "001", "15-01", "Станок", "Оснастка", "Точение", "Ш-1", "15", List.of(func));
        SborEdDto sborEd = new SborEdDto(50L, 1L, "Корпус", "АБВГ.001");

        DockPackageDto dto = new DockPackageDto(1L, "Пакет 1", "path/pkg", "ПУ-01", "СХПУ-01", "КП-01", "FMEA-01", "15-01",
                List.of(oper), List.of(sborEd));

        mapper.toGui(dto, packageName, sborEdNazv, pu, spu, kp, fmea, vedInstr, operationsContainer, assemblyUnitsContainer);

        assertEquals("Пакет 1", packageName.getText());
        assertEquals("ПУ-01", pu.getText());
        assertEquals("СХПУ-01", spu.getText());
        assertEquals("КП-01", kp.getText());
        assertEquals("FMEA-01", fmea.getText());
        assertEquals("15-01", vedInstr.getText());
        assertEquals("Корпус", sborEdNazv.getText());

        assertEquals(1, assemblyUnitsContainer.getChildren().size());
        assertEquals(1, operationsContainer.getChildren().size());
    }

    @Test
    @DisplayName("toGui: clears existing items in containers before populating")
    void toGui_existingItemsInContainers_clearsBeforePopulating() {
        TextField packageName = new TextField();
        TextField sborEdNazv = new TextField();
        TextField pu = new TextField();
        TextField spu = new TextField();
        TextField kp = new TextField();
        TextField fmea = new TextField();
        TextField vedInstr = new TextField();
        VBox operationsContainer = new VBox();
        VBox assemblyUnitsContainer = new VBox();

        operationsContainer.getChildren().add(new VBox());
        assemblyUnitsContainer.getChildren().add(new VBox());

        DockPackageDto emptyDto = new DockPackageDto(2L, "Empty", "path", "P", "S", "K", "F", "V", List.of(), List.of());

        mapper.toGui(emptyDto, packageName, sborEdNazv, pu, spu, kp, fmea, vedInstr, operationsContainer, assemblyUnitsContainer);

        assertEquals(0, operationsContainer.getChildren().size());
        assertEquals(0, assemblyUnitsContainer.getChildren().size());
    }
}
