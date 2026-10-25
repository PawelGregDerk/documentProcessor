package by.vstu.isit.documentprocessor.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocpackageTest {

    @Test
    @DisplayName("builder and getters/setters: construct and retrieve fields")
    void builder_validData_setsAllFields() {
        List<Oper> opers = new ArrayList<>();
        List<SborEd> sborEds = new ArrayList<>();

        Docpackage docpackage = Docpackage.builder()
                .id(1L)
                .packageName("PKG-01")
                .path("C:/docs/pkg1")
                .puName("ПУ 001")
                .spuName("СХПУ 001")
                .kpName("КП 001")
                .fmeaName("FMEA 001")
                .vedIName("15-001")
                .opers(opers)
                .sborEds(sborEds)
                .build();

        assertEquals(1L, docpackage.getId());
        assertEquals("PKG-01", docpackage.getPackageName());
        assertEquals("C:/docs/pkg1", docpackage.getPath());
        assertEquals("ПУ 001", docpackage.getPuName());
        assertEquals("СХПУ 001", docpackage.getSpuName());
        assertEquals("КП 001", docpackage.getKpName());
        assertEquals("FMEA 001", docpackage.getFmeaName());
        assertEquals("15-001", docpackage.getVedIName());
        assertSame(opers, docpackage.getOpers());
        assertSame(sborEds, docpackage.getSborEds());
    }

    @Test
    @DisplayName("equals and hashCode: equal by id from superclass")
    void equalsAndHashCode_sameId_areEqual() {
        Docpackage p1 = Docpackage.builder().id(10L).packageName("Name1").build();
        Docpackage p2 = Docpackage.builder().id(10L).packageName("Name2").build();
        Docpackage p3 = Docpackage.builder().id(20L).packageName("Name1").build();

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test
    @DisplayName("toString: excludes opers and sborEds to prevent infinite recursion")
    void toString_excludesCollections() {
        Docpackage pkg = Docpackage.builder().id(5L).packageName("PKG").build();
        String str = pkg.toString();

        assertTrue(str.contains("packageName=PKG"));
        assertFalse(str.contains("opers="));
        assertFalse(str.contains("sborEds="));
    }
}
