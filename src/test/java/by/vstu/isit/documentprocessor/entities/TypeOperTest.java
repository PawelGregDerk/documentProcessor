package by.vstu.isit.documentprocessor.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeOperTest {

    @Test
    @DisplayName("builder and getters/setters: construct and retrieve fields")
    void builder_validData_setsAllFields() {
        List<TypeOperFunc> funcs = new ArrayList<>();

        TypeOper typeOper = TypeOper.builder()
                .id(1L)
                .numOper("010")
                .nomInstr("15-001/010")
                .oborud("Печь")
                .ostnasInstr("Тигель")
                .name("Пайка")
                .shifr("ПК-1")
                .numZech("15")
                .typeOperFuncs(funcs)
                .build();

        assertEquals(1L, typeOper.getId());
        assertEquals("010", typeOper.getNumOper());
        assertEquals("15-001/010", typeOper.getNomInstr());
        assertEquals("Печь", typeOper.getOborud());
        assertEquals("Тигель", typeOper.getOstnasInstr());
        assertEquals("Пайка", typeOper.getName());
        assertEquals("ПК-1", typeOper.getShifr());
        assertEquals("15", typeOper.getNumZech());
        assertSame(funcs, typeOper.getTypeOperFuncs());
    }

    @Test
    @DisplayName("equals and hashCode: equal by id")
    void equalsAndHashCode_sameId_areEqual() {
        TypeOper t1 = TypeOper.builder().id(100L).numOper("001").build();
        TypeOper t2 = TypeOper.builder().id(100L).numOper("002").build();
        TypeOper t3 = TypeOper.builder().id(200L).numOper("001").build();

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertNotEquals(t1, t3);
    }

    @Test
    @DisplayName("toString: excludes typeOperFuncs to prevent recursion")
    void toString_excludesFuncs() {
        TypeOper t = TypeOper.builder().id(100L).name("Пайка").build();
        String str = t.toString();

        assertTrue(str.contains("name=Пайка"));
        assertFalse(str.contains("typeOperFuncs="));
    }
}
