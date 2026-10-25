package by.vstu.isit.documentprocessor.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuncDtoTest {

    @Test
    @DisplayName("record getters: return values passed to constructor")
    void getters_validData_returnsMatchingValues() {
        FuncDto dto = new FuncDto(1L, "Function A", 100L, "Param 1", true, "Spec 1");

        assertEquals(1L, dto.id());
        assertEquals("Function A", dto.name());
        assertEquals(100L, dto.idOper());
        assertEquals("Param 1", dto.param());
        assertTrue(dto.isProd());
        assertEquals("Spec 1", dto.specCharakt());
    }

    @Test
    @DisplayName("record equals and hashCode: equals based on all components")
    void equalsAndHashCode_identicalFields_areEqual() {
        FuncDto dto1 = new FuncDto(1L, "Function A", 100L, "Param 1", true, "Spec 1");
        FuncDto dto2 = new FuncDto(1L, "Function A", 100L, "Param 1", true, "Spec 1");
        FuncDto dto3 = new FuncDto(2L, "Function A", 100L, "Param 1", true, "Spec 1");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }
}
