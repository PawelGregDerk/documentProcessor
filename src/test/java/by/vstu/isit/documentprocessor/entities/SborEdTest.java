package by.vstu.isit.documentprocessor.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SborEdTest {

    @Test
    @DisplayName("builder and getters/setters: construct and retrieve fields")
    void builder_validData_setsAllFields() {
        Docpackage pkg = Docpackage.builder().id(2L).build();

        SborEd sborEd = SborEd.builder()
                .id(1L)
                .nazv("Корпус")
                .oboznach("АБВГ.543210.001")
                .docpackage(pkg)
                .build();

        assertEquals(1L, sborEd.getId());
        assertEquals("Корпус", sborEd.getNazv());
        assertEquals("АБВГ.543210.001", sborEd.getOboznach());
        assertSame(pkg, sborEd.getDocpackage());
    }

    @Test
    @DisplayName("equals and hashCode: equal by id")
    void equalsAndHashCode_sameId_areEqual() {
        SborEd s1 = SborEd.builder().id(10L).nazv("N1").build();
        SborEd s2 = SborEd.builder().id(10L).nazv("N2").build();
        SborEd s3 = SborEd.builder().id(20L).nazv("N1").build();

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1, s3);
    }

    @Test
    @DisplayName("toString: includes fields")
    void toString_includesFields() {
        SborEd s = SborEd.builder().id(10L).nazv("Корпус").oboznach("Обозн").build();
        String str = s.toString();

        assertTrue(str.contains("Корпус"));
        assertTrue(str.contains("Обозн"));
    }
}
