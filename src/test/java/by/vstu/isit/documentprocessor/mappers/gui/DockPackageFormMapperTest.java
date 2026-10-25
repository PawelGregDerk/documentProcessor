package by.vstu.isit.documentprocessor.mappers.gui;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DockPackageFormMapperTest {

    private final DockPackageFormMapper mapper = new DockPackageFormMapper();

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    @Test
    @DisplayName("fromGui: maps form fields into DockPackageDto correctly")
    void fromGui_validControls_mapsToDto() {
        TextField packageName = new TextField("Package 1");
        TextField sborEdNazv = new TextField("Корпус");
        TextField pu = new TextField("ПУ 01");
        TextField spu = new TextField("СХПУ 01");
        TextField kp = new TextField("КП 01");
        TextField fmea = new TextField("FMEA 01");
        TextField vedInstr = new TextField("15-01");

        VBox opersContainer = new VBox();
        VBox operBlock = new VBox();
        operBlock.setUserData(10L);

        HBox operRow = new HBox();
        operRow.getChildren().addAll(
                new TextField("010"),      // numOper
                new TextField("15-01/1"),  // nomInstr
                new TextField("Станок"),   // oborud
                new TextField("Фреза"),    // ostnasInstr
                new TextField("Фрезерование"), // name
                new TextField("Ш-1"),      // shifr
                new TextField("15")        // numZech
        );

        VBox funcsContainer = new VBox();
        HBox funcRow = new HBox();
        funcRow.getStyleClass().add("function-data-row");
        funcRow.setUserData(100L);
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        StackPane checkWrap = new StackPane(checkBox);
        funcRow.getChildren().addAll(
                new TextField("Снять фаску"), // name
                new TextField("0.5 мм"),      // param
                checkWrap,                    // isProd
                new TextField("Чистота")      // specCharakt
        );
        funcsContainer.getChildren().add(funcRow);

        operBlock.getChildren().addAll(operRow, funcsContainer);
        opersContainer.getChildren().add(operBlock);

        VBox assemblyContainer = new VBox();
        HBox sborRow = new HBox();
        sborRow.setUserData(50L);
        sborRow.getChildren().add(new TextField("АБВГ.123456.001"));
        assemblyContainer.getChildren().add(sborRow);

        DockPackageDto dto = mapper.fromGui(
                1L,
                packageName,
                "/path/to/pkg",
                sborEdNazv,
                pu,
                spu,
                kp,
                fmea,
                vedInstr,
                opersContainer,
                assemblyContainer
        );

        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals("Package 1", dto.packageName());
        assertEquals("_path_to_pkg", dto.path());
        assertEquals("ПУ 01", dto.puName());
        assertEquals("СХПУ 01", dto.spuName());
        assertEquals("КП 01", dto.kpName());
        assertEquals("FMEA 01", dto.fmeaName());
        assertEquals("15-01", dto.vedIName());

        // Verify operations mapping
        assertEquals(1, dto.opers().size());
        var operDto = dto.opers().getFirst();
        assertEquals(10L, operDto.id());
        assertEquals("010", operDto.numOper());
        assertEquals("Фрезерование", operDto.name());

        // Verify functions mapping
        assertEquals(1, operDto.funcs().size());
        var funcDto = operDto.funcs().getFirst();
        assertEquals(100L, funcDto.id());
        assertEquals("Снять фаску", funcDto.name());
        assertTrue(funcDto.isProd());

        // Verify assembly unit mapping
        assertEquals(1, dto.sborEds().size());
        var sborEdDto = dto.sborEds().getFirst();
        assertEquals(50L, sborEdDto.id());
        assertEquals("Корпус", sborEdDto.nazv());
        assertEquals("АБВГ.123456.001", sborEdDto.oboznach());
    }
}
