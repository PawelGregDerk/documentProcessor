package by.vstu.isit.documentprocessor.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DockPackageDtoTest {

    @Test
    @DisplayName("constructor: null opers and sborEds are replaced with empty ArrayLists")
    void constructor_nullLists_initializesEmptyLists() {
        DockPackageDto dto = new DockPackageDto(1L, "pkg", "path", "pu", "spu", "kp", "fmea", "ved", null, null);

        assertNotNull(dto.opers());
        assertTrue(dto.opers().isEmpty());
        assertNotNull(dto.sborEds());
        assertTrue(dto.sborEds().isEmpty());
        // Verify mutability of defensive copy
        assertDoesNotThrow(() -> dto.opers().add(new OperDto(1L, 1L, "01", "wi", "ob", "os", "op", "sh", "01", null)));
        assertDoesNotThrow(() -> dto.sborEds().add(new SborEdDto(1L, 1L, "nazv", "oboz")));
    }

    @Test
    @DisplayName("constructor: non-null lists are defensively copied")
    void constructor_validLists_defensivelyCopiesLists() {
        List<OperDto> opers = new ArrayList<>();
        opers.add(new OperDto(1L, 1L, "01", "wi", "ob", "os", "op", "sh", "01", null));

        List<SborEdDto> sborEds = new ArrayList<>();
        sborEds.add(new SborEdDto(1L, 1L, "nazv", "oboz"));

        DockPackageDto dto = new DockPackageDto(1L, "pkg", "path", "pu", "spu", "kp", "fmea", "ved", opers, sborEds);

        assertEquals(1, dto.opers().size());
        assertEquals(1, dto.sborEds().size());

        // Modifying original input list does not affect DTO's list
        opers.clear();
        assertEquals(1, dto.opers().size());

        sborEds.clear();
        assertEquals(1, dto.sborEds().size());
    }

    @Test
    @DisplayName("record getters: return correct values")
    void getters_validData_returnsMatchingValues() {
        DockPackageDto dto = new DockPackageDto(10L, "Package", "/tmp/docs", "PU-1", "SPU-1", "KP-1", "FMEA-1", "VED-1", List.of(), List.of());

        assertEquals(10L, dto.id());
        assertEquals("Package", dto.packageName());
        assertEquals("/tmp/docs", dto.path());
        assertEquals("PU-1", dto.puName());
        assertEquals("SPU-1", dto.spuName());
        assertEquals("KP-1", dto.kpName());
        assertEquals("FMEA-1", dto.fmeaName());
        assertEquals("VED-1", dto.vedIName());
    }
}
