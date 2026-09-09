package by.vstu.isit.documentprocessor.services.docx.write.abstracts;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import lombok.Getter;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;

public abstract class AbstractWordGenerator {
    private static final Logger log = LoggerFactory.getLogger(AbstractWordGenerator.class);
    @Getter
    protected final Resource inpPath;
    @Getter
    protected final String outPath;

    protected AbstractWordGenerator(Resource inpPath, String outPath) {
        this.inpPath = inpPath;
        this.outPath = outPath;
    }

    public abstract void generate(DockPackageDto dto) throws Exception;

    protected void ensureCells(XWPFTableRow row, int columnCount) {
        while (row.getTableCells().size() < columnCount) {
            row.createCell();
        }
        for (var cell : row.getTableCells()) {
            setCellDefaultFont(cell);
            setCellMargins(cell);
        }
    }

    protected void setCellDefaultFont(XWPFTableCell cell) {
        for (var para : cell.getParagraphs()) {
            var ctp = para.getCTP();
            var pPr = ctp.isSetPPr() ? ctp.getPPr() : ctp.addNewPPr();
            var rPr = pPr.addNewRPr();
            var fonts = rPr.addNewRFonts();
            fonts.setAscii("Arial Narrow");
            fonts.setHAnsi("Arial Narrow");
        }
    }

    protected void setCellMargins(XWPFTableCell cell) {
        var tcPr = cell.getCTTc().isSetTcPr()
                ? cell.getCTTc().getTcPr()
                : cell.getCTTc().addNewTcPr();
        var tcMar = tcPr.addNewTcMar();
        var margin = BigInteger.valueOf(23);
        var type = org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.Enum.forString("dxa");
        tcMar.addNewTop().setW(margin);
        tcMar.getTop().setType(type);
        tcMar.addNewBottom().setW(margin);
        tcMar.getBottom().setType(type);
        tcMar.addNewLeft().setW(margin);
        tcMar.getLeft().setType(type);
        tcMar.addNewRight().setW(margin);
        tcMar.getRight().setType(type);
    }

    protected void copyCellWidths(XWPFTableRow template, XWPFTableRow target) {
        for (int i = 0; i < template.getTableCells().size()
                && i < target.getTableCells().size(); i++) {
            var srcPr = template.getCell(i).getCTTc().getTcPr();
            if (srcPr != null && srcPr.isSetTcW()) {
                var tgtCell = target.getCell(i);
                var tgtPr = tgtCell.getCTTc().isSetTcPr()
                        ? tgtCell.getCTTc().getTcPr()
                        : tgtCell.getCTTc().addNewTcPr();
                if (!tgtPr.isSetTcW()) {
                    tgtPr.addNewTcW().setW(srcPr.getTcW().getW());
                }
            }
        }
    }

    protected void addBookmark(XWPFTableCell cell, String bookmarkName, String text) {
        var para = cell.getParagraphs().getFirst();
        para.getRuns().forEach(r -> r.setText("", 0));
        appendBookmark(para, bookmarkName, text);
    }

    protected void appendBookmark(XWPFTableCell cell, String bookmarkName, String text) {
        var para = cell.getParagraphs().getFirst();
        var spaceRun = para.createRun();
        spaceRun.setText(" ");
        spaceRun.setFontFamily("Arial Narrow");
        appendBookmark(para, bookmarkName, text);
    }

    private void appendBookmark(XWPFParagraph para, String bookmarkName, String text) {
        var p = para.getCTP();
        BigInteger id = BigInteger.valueOf(Math.abs(bookmarkName.hashCode()));

        CTBookmark start = p.addNewBookmarkStart();
        start.setName(bookmarkName);
        start.setId(id);

        var run = para.createRun();
        run.setText(text);
        run.setFontFamily("Arial Narrow");

        CTMarkupRange end = p.addNewBookmarkEnd();
        end.setId(id);
    }

    protected void postProcess(XWPFDocument doc, String folder, String name, Map<String, String> headerData) throws Exception {
        writeDocument(doc, Path.of(format(outPath, folder, name)), headerData);
    }

    protected void writeDocument(XWPFDocument doc, Path out, Map<String, String> headerData) throws Exception {
        Path tmp = Files.createTempFile("docproc_", ".docx");
        Files.createDirectories(out.getParent());

        try {
            try (var outTmp = new FileOutputStream(tmp.toFile())) {
                doc.write(outTmp);
            }
            fillHeadersWithSpire(tmp, out, headerData);
            try {
                saveHeaderMeta(out, headerData);
            } catch (Exception metaEx) {
                log.warn("Failed to save header metadata for {}: {}", out, metaEx.getMessage());
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private void saveHeaderMeta(Path outDoc, Map<String, String> headerData) throws Exception {
        Path metaDir = outDoc.getParent().resolve(".docproc-meta");
        Files.createDirectories(metaDir);
        markHidden(metaDir);

        Path meta = metaDir.resolve(outDoc.getFileName().toString() + ".hdr.properties");
        Properties props = new Properties();
        headerData.forEach((k, v) -> {
            if (v != null) {
                props.setProperty(k, v);
            }
        });
        try (var writer = Files.newBufferedWriter(
                meta,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {
            props.store(writer, "Header source values");
        }
        markHidden(meta);
    }

    private void markHidden(Path path) {
        try {
            Files.setAttribute(path, "dos:hidden", true);
        } catch (Exception ignored) {
        }
    }

    private void fillHeadersWithSpire(Path src, Path out, Map<String, String> data) {
        Document spireDoc = new Document();
        spireDoc.loadFromFile(src.toString());
        for (var entry : data.entrySet()) {
            spireDoc.replace("{{" + entry.getKey() + "}}", entry.getValue(), false, false);
        }
        spireDoc.saveToFile(out.toString(), FileFormat.Docx);
        spireDoc.close();
    }

    protected String designationsAssemblyUnit(List<SborEdDto> sborEdList) {
        return sborEdList.size() > 1 ? "согласно таблице 1" : sborEdList.getFirst().oboznach();
    }

    protected String dseText(List<SborEdDto> sborEds, String singleValue) {
        return sborEds.size() > 1 ? "Таблица 01" : singleValue;
    }
}
