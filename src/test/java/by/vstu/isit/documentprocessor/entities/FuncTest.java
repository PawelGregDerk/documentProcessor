package by.vstu.isit.documentprocessor.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuncTest {

    @Test
    @DisplayName("builder and getters/setters: construct and retrieve fields")
    void builder_validData_setsAllFields() {
        Oper oper = Oper.builder().id(100L).build();

        Func func = Func.builder()
                .id(1L)
                .name("Промывка деталей")
                .param("Температура 60C")
                .isProd(true)
                .specCharakt("Класс чистоты 5")
                .oper(oper)
                .build();

        assertEquals(1L, func.getId());
        assertEquals("Промывка деталей", func.getName());
        assertEquals("Температура 60C", func.getParam());
        assertTrue(func.getIsProd());
        assertEquals("Класс чистоты 5", func.getSpecCharakt());
        assertSame(oper, func.getOper());
    }

    @Test
    @DisplayName("equals and hashCode: equal by id")
    void equalsAndHashCode_sameId_areEqual() {
        Func f1 = Func.builder().id(1L).name("F1").build();
        Func f2 = Func.builder().id(1L).name("F2").build();
        Func f3 = Func.builder().id(2L).name("F1").build();

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, f3);
    }

    @Test
    @DisplayName("toString: includes class and fields")
    void toString_includesFields() {
        Func func = Func.builder().id(1L).name("TestFunc").isProd(false).build();
        String str = func.toString();

        assertTrue(str.contains("TestFunc"));
        assertTrue(str.contains("isProd=false"));
    }
}
