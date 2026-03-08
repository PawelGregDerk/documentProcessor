package by.vstu.isit.documentprocessor.services.docx.bookmarks;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * РЈС‚РёР»РёС‚Р° Р·Р°РіСЂСѓР·РєРё Рё СЃРѕС…СЂР°РЅРµРЅРёСЏ РґРѕРєСѓРјРµРЅС‚РѕРІ docx4j РґР»СЏ Р·Р°РґР°РЅРЅРѕРіРѕ С€Р°Р±Р»РѕРЅР° Рё РїСѓС‚Рё.
 */
final class Docx4jDocumentAccessor {
    private final Resource template;
    private final Path inputPath;
    private final Path outputPath;

    Docx4jDocumentAccessor(Resource template, Path outPath) {
        this(template, outPath, outPath);
    }

    Docx4jDocumentAccessor(Resource template, Path inputPath, Path outputPath) {
        this.template = template;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    WordprocessingMLPackage load() throws IOException {
        try {
            if (Files.exists(inputPath)) {
                return WordprocessingMLPackage.load(inputPath.toFile());
            }
            try (InputStream in = template.getInputStream()) {
                return WordprocessingMLPackage.load(in);
            }
        } catch (Docx4JException e) {
            throw new IOException("Failed to load docx", e);
        }
    }

    void save(WordprocessingMLPackage doc) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try {
            doc.save(outputPath.toFile());
        } catch (Docx4JException e) {
            throw new IOException("Failed to save docx", e);
        }
    }
}

