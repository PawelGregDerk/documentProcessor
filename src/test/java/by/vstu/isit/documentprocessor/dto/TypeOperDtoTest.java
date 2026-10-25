package by.vstu.isit.documentprocessor.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeOperDtoTest {

    @Test
    @DisplayName("record getters: return values correctly")
    void getters_validData_returnsMatchingValues() {
        TypeOperFuncDto func = new TypeOperFuncDto(1L, "Fn", 10L, "Param", true, "Spec");
        TypeOperDto dto = new TypeOperDto(10L, "005", "RI-005", "Станок", "Оснастка", "Фрезерование", "ШИФР-1", "05", List.of(func));

        assertEquals(10L, dto.id());
        assertEquals("005", dto.numOper());
        assertEquals("RI-005", dto.nomInstr());
        assertEquals("Станок", dto.oborud());
        assertEquals("Оснастка", dto.ostnasInstr());
        assertEquals("Фрезерование", dto.name());
        assertEquals("ШИФР-1", dto.shifr());
        assertEquals("05", dto.numZech());
        assertEquals(1, dto.funcs().size());
        assertEquals("Fn", dto.funcs().getFirst().name());
    }

    @Test
    @DisplayName("record equals and hashCode: match on all components")
    void equalsAndHashCode_identicalFields_areEqual() {
        TypeOperDto dto1 = new TypeOperDto(10L, "005", "RI-005", "M", "T", "N", "S", "Z", List.of());
        TypeOperDto dto2 = new TypeOperDto(10L, "005", "RI-005", "M", "T", "N", "S", "Z", List.of());
        TypeOperDto dto3 = new TypeOperDto(11L, "005", "RI-005", "M", "T", "N", "S", "Z", List.of());

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }
}
