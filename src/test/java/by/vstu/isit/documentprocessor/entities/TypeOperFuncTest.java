package by.vstu.isit.documentprocessor.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeOperFuncTest {

    @Test
    @DisplayName("builder and getters/setters: construct and retrieve fields")
    void builder_validData_setsAllFields() {
        TypeOper typeOper = TypeOper.builder().id(50L).build();

        TypeOperFunc func = TypeOperFunc.builder()
                .id(1L)
                .name("Очистка флюса")
                .param("Время 5 мин")
                .isProd(false)
                .specCharakt("Без остатков")
                .typeOper(typeOper)
                .build();

        assertEquals(1L, func.getId());
        assertEquals("Очистка флюса", func.getName());
        assertEquals("Время 5 мин", func.getParam());
        assertFalse(func.getIsProd());
        assertEquals("Без остатков", func.getSpecCharakt());
        assertSame(typeOper, func.getTypeOper());
    }

    @Test
    @DisplayName("equals and hashCode: equal by id")
    void equalsAndHashCode_sameId_areEqual() {
        TypeOperFunc f1 = TypeOperFunc.builder().id(1L).name("F1").build();
        TypeOperFunc f2 = TypeOperFunc.builder().id(1L).name("F2").build();
        TypeOperFunc f3 = TypeOperFunc.builder().id(2L).name("F1").build();

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, f3);
    }

    @Test
    @DisplayName("toString: includes fields")
    void toString_includesFields() {
        TypeOperFunc f = TypeOperFunc.builder().id(1L).name("Очистка").param("Парам").build();
        String str = f.toString();

        assertTrue(str.contains("Очистка"));
        assertTrue(str.contains("Парам"));
    }
}
