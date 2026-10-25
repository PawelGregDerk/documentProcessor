package by.vstu.isit.documentprocessor.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SborEdDtoTest {

    @Test
    @DisplayName("record getters: return values correctly")
    void getters_validData_returnsMatchingValues() {
        SborEdDto dto = new SborEdDto(5L, 10L, "Плато", "АБВГ.123456.001");

        assertEquals(5L, dto.id());
        assertEquals(10L, dto.idDocpackage());
        assertEquals("Плато", dto.nazv());
        assertEquals("АБВГ.123456.001", dto.oboznach());
    }

    @Test
    @DisplayName("record equals and hashCode: match by record components")
    void equalsAndHashCode_identicalFields_areEqual() {
        SborEdDto dto1 = new SborEdDto(5L, 10L, "Плато", "АБВГ.123456.001");
        SborEdDto dto2 = new SborEdDto(5L, 10L, "Плато", "АБВГ.123456.001");
        SborEdDto dto3 = new SborEdDto(6L, 10L, "Плато", "АБВГ.123456.001");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }
}
