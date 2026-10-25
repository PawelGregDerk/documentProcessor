package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class DocxTestSupport {

    private DocxTestSupport() {
    }

    public static XWPFDocument open(Path path) throws Exception {
        return new XWPFDocument(Files.newInputStream(path));
    }

    public static int templateRows(Resource resource, int tableIndex) throws Exception {
        try (InputStream in = resource.getInputStream(); XWPFDocument doc = new XWPFDocument(in)) {
            return doc.getTables().get(tableIndex).getNumberOfRows();
        }
    }

    public static void generateKp(Resource template, String pattern, DockPackageDto dto) throws Exception {
        new KpWordGenerator(template, pattern).generate(dto);
    }

    public static Properties headerMeta(Path out) throws Exception {
        Path meta = out.getParent().resolve(".docproc-meta").resolve(out.getFileName() + ".hdr.properties");
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(meta, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }

    public static List<String> bookmarkNames(XWPFDocument doc) {
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

    public static XWPFTableCell cellByBookmark(XWPFTable table, String bookmark) {
        XWPFTableRow row = rowByBookmark(table, bookmark);
        return row.getCell(cellIndexByBookmark(row, bookmark));
    }

    public static XWPFTableRow rowByBookmark(XWPFTable table, String bookmark) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                if (hasBookmark(cell, bookmark)) {
                    return row;
                }
            }
        }
        throw new AssertionError("Закладка не найдена: " + bookmark);
    }

    public static boolean hasBookmark(XWPFTableCell cell, String bookmark) {
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
            for (CTBookmark start : paragraph.getCTP().getBookmarkStartList()) {
                if (bookmark.equals(start.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String headerText(XWPFDocument doc) {
        StringBuilder text = new StringBuilder();
        for (XWPFHeader header : doc.getHeaderList()) {
            text.append(header.getText()).append('\n');
        }
        return text.toString();
    }

    public static STMerge.Enum verticalMerge(XWPFTableCell cell) {
        var tcPr = cell.getCTTc().getTcPr();
        if (tcPr == null || !tcPr.isSetVMerge()) {
            return null;
        }
        var vMerge = tcPr.getVMerge();
        // В OOXML <w:vMerge/> без атрибута val означает "continue".
        return vMerge.isSetVal() ? vMerge.getVal() : STMerge.CONTINUE;
    }

    public static BigInteger gridSpan(XWPFTableCell cell) {
        var tcPr = cell.getCTTc().getTcPr();
        if (tcPr == null || !tcPr.isSetGridSpan()) {
            return null;
        }
        return tcPr.getGridSpan().getVal();
    }

    public static boolean hasFontSize(XWPFTableCell cell, int size) {
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

    public static boolean hasFontFamily(XWPFTableCell cell, String family) {
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

    public static void putManualText(Path docx, String bookmark, int cellIndex, String text) throws Exception {
        byte[] bytes = Files.readAllBytes(docx);
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            XWPFTableRow row = rowByBookmark(tableByBookmark(doc, bookmark), bookmark);
            XWPFTableCell cell = row.getCell(cellIndex);
            XWPFParagraph paragraph = cell.getParagraphs().get(0);
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            try (OutputStream out = Files.newOutputStream(docx)) {
                doc.write(out);
            }
        }
    }

    public static String manualText(Path docx, String bookmark, int cellIndex) throws Exception {
        try (XWPFDocument doc = open(docx)) {
            XWPFTableRow row = rowByBookmark(tableByBookmark(doc, bookmark), bookmark);
            return row.getCell(cellIndex).getText();
        }
    }

    private static XWPFTable tableByBookmark(XWPFDocument doc, String bookmark) {
        for (XWPFTable table : doc.getTables()) {
            try {
                rowByBookmark(table, bookmark);
                return table;
            } catch (AssertionError ignored) {
                // ищем в следующей таблице
            }
        }
        throw new AssertionError("Закладка не найдена ни в одной таблице: " + bookmark);
    }

    private static int cellIndexByBookmark(XWPFTableRow row, String bookmark) {
        for (int i = 0; i < row.getTableCells().size(); i++) {
            if (hasBookmark(row.getCell(i), bookmark)) {
                return i;
            }
        }
        throw new AssertionError("Закладка не найдена в строке: " + bookmark);
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
