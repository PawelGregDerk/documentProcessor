package by.vstu.isit.documentprocessor.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeOperFuncDtoTest {

    @Test
    @DisplayName("record getters: return values correctly")
    void getters_validData_returnsMatchingValues() {
        TypeOperFuncDto dto = new TypeOperFuncDto(1L, "Func name", 2L, "Param val", false, "Spec val");

        assertEquals(1L, dto.id());
        assertEquals("Func name", dto.name());
        assertEquals(2L, dto.idTypeOper());
        assertEquals("Param val", dto.param());
        assertFalse(dto.isProd());
        assertEquals("Spec val", dto.specCharakt());
    }

    @Test
    @DisplayName("record equals and hashCode: match on all components")
    void equalsAndHashCode_identicalFields_areEqual() {
        TypeOperFuncDto dto1 = new TypeOperFuncDto(1L, "Func name", 2L, "Param val", false, "Spec val");
        TypeOperFuncDto dto2 = new TypeOperFuncDto(1L, "Func name", 2L, "Param val", false, "Spec val");
        TypeOperFuncDto dto3 = new TypeOperFuncDto(2L, "Func name", 2L, "Param val", false, "Spec val");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }
}
