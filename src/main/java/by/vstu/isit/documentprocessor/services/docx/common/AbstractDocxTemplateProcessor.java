package by.vstu.isit.documentprocessor.services.docx.common;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import com.deepoove.poi.XWPFTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;

public abstract class AbstractDocxTemplateProcessor {
    /**
     * Шаблон документа (docx), из которого строится итоговый файл.
     */
    protected final Resource inpPath;
    /**
     * Путь к временному файлу, в который сохраняется промежуточный результат.
     */
    protected final String tmpOut;
    /**
     * Путь к итоговому файлу (может содержать плейсхолдер имени).
     */
    protected final String outPath;
    protected String path;

    protected AbstractDocxTemplateProcessor(Resource inpPath, String tmpOut, String outPath) {
        this.inpPath = inpPath;
        this.tmpOut = tmpOut;
        this.outPath = outPath;
    }

    protected void ensureCells(XWPFTableRow row, int columnCount) {
        while (row.getTableCells().size() < columnCount) {
            row.createCell();
        }
    }

    protected void postProcess(XWPFDocument doc, String name, Map<String, String> colData) throws Exception {
        Path tmp = resolveTmpPath(name);
        var tFile = tmp.toFile();
        FileUtils.forceMkdirParent(tFile);

        Path out = resolveOutPath(name);
        var file = out.toFile();
        FileUtils.forceMkdirParent(file);

        try (var outTmp = new FileOutputStream(tFile)) {
            doc.write(outTmp);
        }

        try (var in = new FileInputStream(tFile);
             var template = XWPFTemplate.compile(in).render(colData);
             var outFinal = new FileOutputStream(file)) {
            template.write(outFinal);
        } finally {
            Files.deleteIfExists(tFile.toPath());
        }
    }

    protected void ensureExisting(Path out) throws Exception {
        if (!Files.exists(out)) {
            throw new Exception("Документ не найден: " + out);
        }
    }

    protected String designationsAssemblyUnit(List<SborEdDto> sborEdList) {
        String first = sborEdList.getFirst().oboznach();
        String last = sborEdList.getLast().oboznach();
        return first + "\u2014" + last;
    }

    protected String article(DockPackageDto dto) {
        return dto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(dto.sborEds());
    }

    protected Path resolveOutPath(String name) {
        if (!outPath.contains("{2}")) {
            return Path.of(format(outPath, path, name));
        }
        return Path.of(format(outPath, path, "ri", name));
    }

    protected Path resolveOutPath(String basePath, String name) {
        if (!outPath.contains("{2}")) {
            return Path.of(format(outPath, basePath, name));
        }
        return Path.of(format(outPath, basePath, "ri", name));
    }

    protected Path resolveTmpPath(String name) {
        if (!tmpOut.contains("{2}")) {
            return Path.of(format(tmpOut, path));
        }
        return Path.of(format(tmpOut, path, "ri", name));
    }

    protected String copyPath(String basePath) {
        return "копия" + basePath;
    }
}

