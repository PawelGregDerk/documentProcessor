package by.vstu.isit.documentprocessor.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperTest {

    @Test
    @DisplayName("builder and getters/setters: construct and retrieve fields")
    void builder_validData_setsAllFields() {
        Docpackage pkg = Docpackage.builder().id(1L).build();
        List<Func> funcs = new ArrayList<>();

        Oper oper = Oper.builder()
                .id(10L)
                .numOper("005")
                .nomInstr("15-132/005")
                .oborud("Станок ЧПУ")
                .ostnasInstr("Резец R1")
                .name("Токарная")
                .shifr("ТOK-1")
                .numZech("15")
                .docpackage(pkg)
                .funcs(funcs)
                .build();

        assertEquals(10L, oper.getId());
        assertEquals("005", oper.getNumOper());
        assertEquals("15-132/005", oper.getNomInstr());
        assertEquals("Станок ЧПУ", oper.getOborud());
        assertEquals("Резец R1", oper.getOstnasInstr());
        assertEquals("Токарная", oper.getName());
        assertEquals("ТOK-1", oper.getShifr());
        assertEquals("15", oper.getNumZech());
        assertSame(pkg, oper.getDocpackage());
        assertSame(funcs, oper.getFuncs());
    }

    @Test
    @DisplayName("equals and hashCode: equal by id")
    void equalsAndHashCode_sameId_areEqual() {
        Oper o1 = Oper.builder().id(5L).numOper("001").build();
        Oper o2 = Oper.builder().id(5L).numOper("002").build();
        Oper o3 = Oper.builder().id(6L).numOper("001").build();

        assertEquals(o1, o2);
        assertEquals(o1.hashCode(), o2.hashCode());
        assertNotEquals(o1, o3);
    }

    @Test
    @DisplayName("toString: excludes funcs to prevent infinite recursion")
    void toString_excludesFuncs() {
        Oper oper = Oper.builder().id(5L).name("Опрессовка").build();
        String str = oper.toString();

        assertTrue(str.contains("name=Опрессовка"));
        assertFalse(str.contains("funcs="));
    }
}
