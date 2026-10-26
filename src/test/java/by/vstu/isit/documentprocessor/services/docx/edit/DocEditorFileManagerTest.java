package by.vstu.isit.documentprocessor.services.docx.edit;

import com.spire.doc.Document;
import com.spire.doc.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class DocEditorFileManagerTest {

    @TempDir
    Path tempDir;

    private DocEditorFileManager manager;

    @BeforeEach
    void setUp() {
        String pattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/{1}";
        manager = new DocEditorFileManager(pattern);
    }

    @Test
    @DisplayName("loadCopy: копирует документ и загружает его")
    void loadCopy_copiesAndLoadsDocument() throws Exception {
        Path source = copyTemplate("pkg", "source.docx");

        Document doc = manager.loadCopy("pkg", "source.docx", "copy.docx");

        assertThat(source).exists();
        assertThat(tempDir.resolve("pkg/copy.docx")).exists();
        assertThat(doc.getSections().getCount()).isPositive();
        doc.close();
    }

    @Test
    @DisplayName("loadCopy: читает старую шапку из метаданных источника")
    void loadCopy_resolvesOldHeaderFromMeta() throws Exception {
        Path source = copyTemplate("pkg", "source.docx");
        writeMeta(source, Map.of("d", "из метаданных"));

        manager.loadCopy("pkg", "source.docx", "copy.docx");
        Map<String, String> resolved = manager.resolveHeaderOldData(
                Map.of("d", "запасное", "n", "нет в метаданных"));

        assertThat(resolved.get("d")).isEqualTo("из метаданных");
        assertThat(resolved.get("n")).isEqualTo("нет в метаданных");
    }

    @Test
    @DisplayName("loadCopy: учитывает sourceFolderOverride")
    void loadCopy_usesSourceFolderOverride() throws Exception {
        copyTemplate("archive", "source.docx");
        manager.setSourceFolderOverride("archive");

        manager.loadCopy("pkg", "source.docx", "copy.docx");

        assertThat(tempDir.resolve("pkg/copy.docx")).exists();
    }

    @Test
    @DisplayName("save: сохраняет документ и метаданные шапки")
    void save_writesDocumentAndHeaderMeta() throws Exception {
        Document doc = new Document();
        Section section = doc.addSection();
        section.addParagraph().appendText("Документ");

        manager.save(doc, "pkg", "saved.docx", Map.of("d", "значение"));

        Path saved = tempDir.resolve("pkg/saved.docx");
        Path meta = tempDir.resolve("pkg/.docproc-meta/saved.docx.hdr.properties");
        assertThat(saved).exists();
        assertThat(meta).exists();

        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(meta, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        assertThat(props.getProperty("d")).isEqualTo("значение");
    }

    private Path copyTemplate(String folder, String name) throws Exception {
        Path target = tempDir.resolve(folder).resolve(name);
        Files.createDirectories(target.getParent());
        try (var in = new ClassPathResource("templates/RI.docx").getInputStream()) {
            Files.copy(in, target);
        }
        return target;
    }

    private void writeMeta(Path source, Map<String, String> values) throws Exception {
        Path meta = source.getParent().resolve(".docproc-meta")
                .resolve(source.getFileName() + ".hdr.properties");
        Files.createDirectories(meta.getParent());
        Properties props = new Properties();
        props.putAll(values);
        try (var writer = Files.newBufferedWriter(meta, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(writer, null);
        }
    }
}
