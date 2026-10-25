package by.vstu.isit.documentprocessor.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperDtoTest {

    @Test
    @DisplayName("constructor: null funcs is initialized to empty list")
    void constructor_nullFuncs_initializesEmptyList() {
        OperDto dto = new OperDto(1L, 2L, "010", "WI-1", "Mach-1", "Tool-1", "Turn", "SH-1", "01", null);

        assertNotNull(dto.funcs());
        assertTrue(dto.funcs().isEmpty());
    }

    @Test
    @DisplayName("constructor: non-null funcs retains elements")
    void constructor_validFuncs_retainsElements() {
        FuncDto f1 = new FuncDto(10L, "F1", 1L, "P1", false, "S1");
        OperDto dto = new OperDto(1L, 2L, "010", "WI-1", "Mach-1", "Tool-1", "Turn", "SH-1", "01", List.of(f1));

        assertEquals(1, dto.funcs().size());
        assertEquals("F1", dto.funcs().getFirst().name());
    }

    @Test
    @DisplayName("record getters: return values correctly")
    void getters_validData_returnsMatchingValues() {
        OperDto dto = new OperDto(1L, 2L, "010", "WI-1", "Mach-1", "Tool-1", "Turn", "SH-1", "01", List.of());

        assertEquals(1L, dto.id());
        assertEquals(2L, dto.idDocPackage());
        assertEquals("010", dto.numOper());
        assertEquals("WI-1", dto.nomInstr());
        assertEquals("Mach-1", dto.oborud());
        assertEquals("Tool-1", dto.ostnasInstr());
        assertEquals("Turn", dto.name());
        assertEquals("SH-1", dto.shifr());
        assertEquals("01", dto.numZech());
    }
}
