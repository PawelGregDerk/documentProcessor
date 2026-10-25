package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractDocEditorTest {

    static class TestDocEditor extends AbstractDocEditor {
        public TestDocEditor(String srcPath, String destPath) {
            super(srcPath, destPath);
        }

        @Override
        public void edit(DockPackageDto dto, DockPackageDto savedDto) {
            // no-op for testing base methods
        }
    }

    @Test
    @DisplayName("copyFolder: returns unchanged folder name with default prefix")
    void copyFolder_defaultPrefix_returnsSameFolder() {
        TestDocEditor editor = new TestDocEditor("src/{0}", "dest/{0}");
        assertEquals("myFolder", editor.copyFolder("myFolder"));
    }

    @Test
    @DisplayName("sourceFolderOverride: getter and setter work correctly")
    void sourceFolderOverride_setAndGet_returnsValue() {
        TestDocEditor editor = new TestDocEditor("src/{0}", "dest/{0}");
        assertEquals("", editor.getSourceFolderOverride());

        editor.setSourceFolderOverride("overrideFolder");
        assertEquals("overrideFolder", editor.getSourceFolderOverride());
    }

    @Test
    @DisplayName("designationsAssemblyUnit: 1 sborEd returns oboznach, multiple returns 'согласно таблице 1'")
    void designationsAssemblyUnit_variousSizes_returnsCorrectString() {
        TestDocEditor editor = new TestDocEditor("src/{0}", "dest/{0}");

        SborEdDto se1 = new SborEdDto(1L, 1L, "Деталь", "ОБОЗН-001");
        SborEdDto se2 = new SborEdDto(2L, 1L, "Узел", "ОБОЗН-002");

        assertEquals("ОБОЗН-001", editor.designationsAssemblyUnit(List.of(se1)));
        assertEquals("согласно таблице 1", editor.designationsAssemblyUnit(List.of(se1, se2)));
    }

    @Test
    @DisplayName("dseText: 1 sborEd returns singleValue, multiple returns 'Таблица 01'")
    void dseText_variousSizes_returnsCorrectString() {
        TestDocEditor editor = new TestDocEditor("src/{0}", "dest/{0}");

        SborEdDto se1 = new SborEdDto(1L, 1L, "Деталь", "ОБОЗН-001");
        SborEdDto se2 = new SborEdDto(2L, 1L, "Узел", "ОБОЗН-002");

        assertEquals("CustomName", editor.dseText(List.of(se1), "CustomName"));
        assertEquals("Таблица 01", editor.dseText(List.of(se1, se2), "CustomName"));
    }

    @Test
    @DisplayName("resolveHeaderOldData: returns fallback map if lastResolvedSource is null")
    void resolveHeaderOldData_noResolvedSource_returnsFallback() {
        TestDocEditor editor = new TestDocEditor("src/{0}", "dest/{0}");
        Map<String, String> fallback = Map.of("key1", "val1", "key2", "val2");

        Map<String, String> result = editor.resolveHeaderOldData(fallback);
        assertEquals(fallback, result);
    }

    @Test
    @DisplayName("resolveHeaderOldData: reads from .docproc-meta properties file when available")
    void resolveHeaderOldData_withMetaProperties_mergesValues(@TempDir Path tempDir) throws Exception {
        Path docPath = tempDir.resolve("sample.docx");
        Files.createFile(docPath);

        Path metaDir = tempDir.resolve(".docproc-meta");
        Files.createDirectories(metaDir);
        Path metaProp = metaDir.resolve("sample.docx.hdr.properties");

        Properties props = new Properties();
        props.setProperty("author", "Engineer A");
        try (BufferedWriter writer = Files.newBufferedWriter(metaProp)) {
            props.store(writer, null);
        }

        TestDocEditor editor = new TestDocEditor("src/{0}", "dest/{0}");
        ReflectionTestUtils.setField(editor, "lastResolvedSource", docPath);

        Map<String, String> fallback = Map.of("author", "Default Author", "version", "1.0");
        Map<String, String> result = editor.resolveHeaderOldData(fallback);

        assertEquals("Engineer A", result.get("author"));
        assertEquals("1.0", result.get("version"));
    }
}
