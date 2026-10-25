package by.vstu.isit.documentprocessor.services.docx.write.impl;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

final class DocxTestSupport {

    private DocxTestSupport() {
    }

    static XWPFDocument open(Path path) throws Exception {
        return new XWPFDocument(Files.newInputStream(path));
    }

    static int templateRows(Resource resource, int tableIndex) throws Exception {
        try (InputStream in = resource.getInputStream(); XWPFDocument doc = new XWPFDocument(in)) {
            return doc.getTables().get(tableIndex).getNumberOfRows();
        }
    }

    static Properties headerMeta(Path out) throws Exception {
        Path meta = out.getParent().resolve(".docproc-meta").resolve(out.getFileName() + ".hdr.properties");
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(meta, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }

    static List<String> bookmarkNames(XWPFDocument doc) {
        List<String> names = new ArrayList<>();
        collect(doc.getParagraphs(), names);
        for (XWPFTable table : doc.getTables()) {
            collect(table, names);
        }
        for (XWPFHeader header : doc.getHeaderList()) {
            collect(header.getParagraphs(), names);
            for (XWPFTable table : header.getTables()) {
                collect(table, names);
            }
        }
        return names;
    }

    static XWPFTableCell cellByBookmark(XWPFTable table, String bookmark) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                if (hasBookmark(cell, bookmark)) {
                    return cell;
                }
            }
        }
        throw new AssertionError("Закладка не найдена: " + bookmark);
    }

    static boolean hasBookmark(XWPFTableCell cell, String bookmark) {
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
            for (CTBookmark start : paragraph.getCTP().getBookmarkStartList()) {
                if (bookmark.equals(start.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    static String headerText(XWPFDocument doc) {
        StringBuilder text = new StringBuilder();
        for (XWPFHeader header : doc.getHeaderList()) {
            text.append(header.getText()).append('\n');
        }
        return text.toString();
    }

    static STMerge.Enum verticalMerge(XWPFTableCell cell) {
        var tcPr = cell.getCTTc().getTcPr();
        if (tcPr == null || !tcPr.isSetVMerge()) {
            return null;
        }
        var vMerge = tcPr.getVMerge();
        // В OOXML <w:vMerge/> без атрибута val означает "continue".
        return vMerge.isSetVal() ? vMerge.getVal() : STMerge.CONTINUE;
    }

    static BigInteger gridSpan(XWPFTableCell cell) {
        var tcPr = cell.getCTTc().getTcPr();
        if (tcPr == null || !tcPr.isSetGridSpan()) {
            return null;
        }
        return tcPr.getGridSpan().getVal();
    }

    static boolean hasFontSize(XWPFTableCell cell, int size) {
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (run.getFontSize() == size && text != null && !text.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean hasFontFamily(XWPFTableCell cell, String family) {
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (family.equals(run.getFontFamily()) && text != null && !text.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void collect(XWPFTable table, List<String> names) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                collect(cell.getParagraphs(), names);
            }
        }
    }

    private static void collect(List<XWPFParagraph> paragraphs, List<String> names) {
        for (XWPFParagraph paragraph : paragraphs) {
            for (CTBookmark start : paragraph.getCTP().getBookmarkStartList()) {
                names.add(start.getName());
            }
        }
    }
}
