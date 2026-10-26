package by.vstu.isit.documentprocessor.services.docx.edit;

import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.text.MessageFormat.format;

@Slf4j
public class DocEditorFileManager {

    private final String srcPath;
    private final String destPath;
    private Path lastResolvedSource;
    private String copyFolderPrefix = "";
    private String sourceFolderOverride = "";

    public DocEditorFileManager(String srcPath) {
        this.srcPath = srcPath;
        this.destPath = srcPath;
    }

    public void setSourceFolderOverride(String sourceFolderOverride) {
        this.sourceFolderOverride = sourceFolderOverride;
    }

    public String getSourceFolderOverride() {
        return sourceFolderOverride;
    }

    public String copyFolder(String folder) {
        return copyFolderPrefix + folder;
    }

    public Document loadCopy(String folder, String srcName, String destName) throws IOException {
        String srcFolder = !sourceFolderOverride.isEmpty() ? sourceFolderOverride : folder;
        Path src = Path.of(format(srcPath, srcFolder, srcName));
        Path altSrc = Path.of(format(srcPath, srcFolder, destName));
        Path dest = Path.of(format(destPath, copyFolder(folder), destName));
        Path resolvedSrc = resolveSource(src, altSrc);

        if (resolvedSrc == null) {
            throw new NoSuchFileException("No source document found in originals. Tried: " + src + ", " + altSrc);
        }

        Files.createDirectories(dest.getParent());
        if (!resolvedSrc.equals(dest)) {
            Files.copy(resolvedSrc, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        lastResolvedSource = resolvedSrc;

        Document doc = new Document();
        doc.loadFromFile(dest.toString());
        return doc;
    }

    public Document loadCopy(String folder, String name) throws IOException {
        return loadCopy(folder, name, name);
    }

    public Document loadCopySub(String folder, String sub, String srcName, String destName) throws IOException {
        String srcFolder = !sourceFolderOverride.isEmpty() ? sourceFolderOverride : folder;
        Path src = Path.of(format(srcPath, srcFolder, sub, srcName));
        Path altSrc = Path.of(format(srcPath, srcFolder, sub, destName));
        Path dest = Path.of(format(destPath, copyFolder(folder), sub, destName));
        Path resolvedSrc = resolveSource(src, altSrc);

        if (resolvedSrc == null) {
            throw new NoSuchFileException("No source document found in originals. Tried: " + src + ", " + altSrc);
        }

        Files.createDirectories(dest.getParent());
        if (!resolvedSrc.equals(dest)) {
            Files.copy(resolvedSrc, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        lastResolvedSource = resolvedSrc;

        Document doc = new Document();
        doc.loadFromFile(dest.toString());
        return doc;
    }

    public Document loadCopySub(String folder, String sub, String name) throws IOException {
        return loadCopySub(folder, sub, name, name);
    }

    public Document loadFromExplicitSource(Path source, Path dest) throws IOException {
        Files.createDirectories(dest.getParent());
        if (!source.equals(dest)) {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        lastResolvedSource = source;
        Document doc = new Document();
        doc.loadFromFile(dest.toString());
        return doc;
    }

    public Map<String, String> resolveHeaderOldData(Map<String, String> fallback) {
        if (lastResolvedSource == null) {
            return fallback;
        }

        Path meta = metaPathFor(lastResolvedSource);
        if (!Files.exists(meta)) {
            return fallback;
        }

        Properties props = new Properties();
        try (FileReader reader = new FileReader(meta.toFile())) {
            props.load(reader);
        } catch (Exception e) {
            return fallback;
        }

        Map<String, String> resolved = new HashMap<>();
        for (Map.Entry<String, String> entry : fallback.entrySet()) {
            String key = entry.getKey();
            String value = props.getProperty(key, entry.getValue());
            resolved.put(key, value);
        }
        return resolved;
    }

    public void save(Document doc, String folder, String name, Map<String, String> headerData) {
        Path outPath = Path.of(format(destPath, copyFolder(folder), name));
        doc.saveToFile(outPath.toString(), FileFormat.Docx);
        doc.close();
        if (headerData != null) {
            saveHeaderMeta(outPath, headerData);
        }
    }

    public void saveSub(Document doc, String folder, String sub, String name, Map<String, String> headerData) {
        Path outPath = Path.of(format(destPath, copyFolder(folder), sub, name));
        doc.saveToFile(outPath.toString(), FileFormat.Docx);
        doc.close();
        if (headerData != null) {
            saveHeaderMeta(outPath, headerData);
        }
    }

    private Path resolveSource(Path... candidates) {
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private Path metaPathFor(Path sourceDoc) {
        Path metaDir = sourceDoc.getParent().resolve(".docproc-meta");
        return metaDir.resolve(sourceDoc.getFileName().toString() + ".hdr.properties");
    }

    private void saveHeaderMeta(Path outDoc, Map<String, String> headerData) {
        try {
            Path metaDir = outDoc.getParent().resolve(".docproc-meta");
            Files.createDirectories(metaDir);
            setHidden(metaDir);
            Path meta = metaDir.resolve(outDoc.getFileName() + ".hdr.properties");
            Properties props = new Properties();
            headerData.forEach((key, value) -> {
                if (value != null) {
                    props.setProperty(key, value);
                }
            });
            try (var writer = Files.newBufferedWriter(meta, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                props.store(writer, "Header source values");
            }
            setHidden(meta);
        } catch (Exception e) {
            log.warn("Failed to save header metadata for {}: {}", outDoc, e.getMessage());
        }
    }

    private void setHidden(Path path) {
        try {
            Files.setAttribute(path, "dos:hidden", true);
        } catch (Exception ignored) {
        }
    }
}
