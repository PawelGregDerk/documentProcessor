package by.vstu.isit.documentprocessor.services.docx.bookmarks;

import org.docx4j.XmlUtils;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Р¤Р°СЃР°Рґ РґР»СЏ Р·Р°РїРёСЃРё РґР°РЅРЅС‹С… РІ docx С‡РµСЂРµР· docx4j СЃ РёСЃРїРѕР»СЊР·РѕРІР°РЅРёРµРј Р·Р°РєР»Р°РґРѕРє.
 */
public final class Docx4jBookmarkWriter implements AutoCloseable {
    private static final ObjectFactory WML_FACTORY = new ObjectFactory();

    private final Docx4jDocumentAccessor accessor;
    private final WordprocessingMLPackage doc;
    private final Docx4jBookmarkHelper bookmarkHelper;
    private BigInteger nextBookmarkId;

    public Docx4jBookmarkWriter(Resource template, Path outPath) throws IOException {
        this.accessor = new Docx4jDocumentAccessor(template, outPath);
        this.doc = accessor.load();
        this.bookmarkHelper = new Docx4jBookmarkHelper(doc);
        this.nextBookmarkId = bookmarkHelper.maxBookmarkId().add(BigInteger.ONE);
    }

    public Docx4jBookmarkWriter(Resource template, Path inPath, Path outPath) throws IOException {
        this.accessor = new Docx4jDocumentAccessor(template, inPath, outPath);
        this.doc = accessor.load();
        this.bookmarkHelper = new Docx4jBookmarkHelper(doc);
        this.nextBookmarkId = bookmarkHelper.maxBookmarkId().add(BigInteger.ONE);
    }

    public DocxTable getTable(int index) {
        List<Tbl> tables = getBodyTables(doc.getMainDocumentPart());
        if (index < 0 || index >= tables.size()) {
            throw new IndexOutOfBoundsException("Table index " + index + " is out of range: " + tables.size());
        }
        return new DocxTable(tables.get(index));
    }

    public List<Integer> listBookmarkIndices(String prefix, String suffix) {
        TreeSet<Integer> indices = new TreeSet<>();
        collectBookmarkIndices(doc.getMainDocumentPart().getContent(), prefix, suffix, indices);
        if (indices.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(indices);
    }

    public boolean updateBookmarkText(String name, String text) {
        String safeText = Objects.toString(text, "");
        boolean updated = updateBookmarkInContent(doc.getMainDocumentPart().getContent(), name, safeText);
        for (SectionWrapper section : doc.getDocumentModel().getSections()) {
            if (section.getHeaderFooterPolicy() == null) {
                continue;
            }
            updated |= updateBookmarkInContent(section.getHeaderFooterPolicy().getDefaultHeader(), name, safeText);
            updated |= updateBookmarkInContent(section.getHeaderFooterPolicy().getDefaultFooter(), name, safeText);
            updated |= updateBookmarkInContent(section.getHeaderFooterPolicy().getEvenHeader(), name, safeText);
            updated |= updateBookmarkInContent(section.getHeaderFooterPolicy().getEvenFooter(), name, safeText);
            updated |= updateBookmarkInContent(section.getHeaderFooterPolicy().getFirstHeader(), name, safeText);
            updated |= updateBookmarkInContent(section.getHeaderFooterPolicy().getFirstFooter(), name, safeText);
        }
        for (Part part : doc.getParts().getParts().values()) {
            if (part instanceof HeaderPart header) {
                updated |= updateBookmarkInContent(header, name, safeText);
            } else if (part instanceof FooterPart footer) {
                updated |= updateBookmarkInContent(footer, name, safeText);
            }
        }
        return updated;
    }

    public void setHeaderCellSegments(int tableIndex, int row, int col, List<CellSegment> segments) {
        setHeaderCellSegments(tableIndex, row, col, segments, HeaderFooterScope.ALL);
    }

    public void setHeaderCellSegments(int tableIndex, int row, int col, List<CellSegment> segments, HeaderFooterScope scope) {
        for (SectionWrapper section : doc.getDocumentModel().getSections()) {
            if (section.getHeaderFooterPolicy() == null) {
                continue;
            }
            if (scope == HeaderFooterScope.ALL || scope == HeaderFooterScope.DEFAULT_EVEN_ONLY) {
                applyHeaderFooterSegments(section.getHeaderFooterPolicy().getDefaultHeader(), tableIndex, row, col, segments);
                applyHeaderFooterSegments(section.getHeaderFooterPolicy().getDefaultFooter(), tableIndex, row, col, segments);
                applyHeaderFooterSegments(section.getHeaderFooterPolicy().getEvenHeader(), tableIndex, row, col, segments);
                applyHeaderFooterSegments(section.getHeaderFooterPolicy().getEvenFooter(), tableIndex, row, col, segments);
            }
            if (scope == HeaderFooterScope.ALL || scope == HeaderFooterScope.FIRST_ONLY) {
                applyHeaderFooterSegments(section.getHeaderFooterPolicy().getFirstHeader(), tableIndex, row, col, segments);
                applyHeaderFooterSegments(section.getHeaderFooterPolicy().getFirstFooter(), tableIndex, row, col, segments);
            }
        }
    }

    public void setCellSegments(DocxTableCell cell, List<CellSegment> segments) {
        clearCellParagraphs(cell.tc);
        P paragraph = WML_FACTORY.createP();
        cell.tc.getContent().add(paragraph);

        for (CellSegment segment : segments) {
            String text = Objects.toString(segment.text(), "");
            if (segment.bookmark() != null) {
                bookmarkHelper.removeBookmarkIfExists(segment.bookmark());
                BigInteger id = nextBookmarkId;
                nextBookmarkId = nextBookmarkId.add(BigInteger.ONE);
                CTBookmark start = WML_FACTORY.createCTBookmark();
                start.setName(segment.bookmark());
                start.setId(id);
                paragraph.getContent().add(createRun(" ", false, false, false));
                paragraph.getContent().add(start);
                paragraph.getContent().add(createRun(text, segment.bold(), false, true));
                CTMarkupRange end = WML_FACTORY.createCTMarkupRange();
                end.setId(id);
                paragraph.getContent().add(end);
                paragraph.getContent().add(createRun(" ", false, false, false));
            } else {
                paragraph.getContent().add(createRun(text, segment.bold(), false, false));
            }

            if (segment.lineBreakAfter()) {
                R breakRun = WML_FACTORY.createR();
                breakRun.getContent().add(WML_FACTORY.createBr());
                paragraph.getContent().add(breakRun);
            }
        }
    }

    public void save() throws IOException {
        accessor.save(doc);
    }

    @Override
    public void close() {
        // no-op
    }

    public record CellSegment(String bookmark, String text, boolean bold, boolean lineBreakAfter) {
        public static CellSegment text(String text, boolean lineBreakAfter) {
            return new CellSegment(null, text, false, lineBreakAfter);
        }

        public static CellSegment bookmark(String name, String text, boolean bold, boolean lineBreakAfter) {
            return new CellSegment(name, text, bold, lineBreakAfter);
        }
    }

    public enum HeaderFooterScope {
        ALL,
        DEFAULT_EVEN_ONLY,
        FIRST_ONLY
    }

    public static final class DocxTable {
        private final Tbl tbl;

        private DocxTable(Tbl tbl) {
            this.tbl = tbl;
        }

        public DocxTableRows getRows() {
            return new DocxTableRows(tbl);
        }
    }

    public static final class DocxTableRows {
        private final List<Tr> rows;

        private DocxTableRows(Tbl tbl) {
            this.rows = getRowsFromTable(tbl);
        }

        public int getCount() {
            return rows.size();
        }

        public DocxTableRow get(int index) {
            return new DocxTableRow(rows.get(index));
        }
    }

    public static final class DocxTableRow {
        private final Tr tr;

        private DocxTableRow(Tr tr) {
            this.tr = tr;
        }

        public DocxTableCells getCells() {
            return new DocxTableCells(tr);
        }
    }

    public static final class DocxTableCells {
        private final List<Tc> cells;

        private DocxTableCells(Tr tr) {
            this.cells = getCellsFromRow(tr);
        }

        public int getCount() {
            return cells.size();
        }

        public DocxTableCell get(int index) {
            return new DocxTableCell(cells.get(index));
        }
    }

    public static final class DocxTableCell {
        private final Tc tc;

        private DocxTableCell(Tc tc) {
            this.tc = tc;
        }
    }

    private void applyHeaderFooterSegments(HeaderPart header, int tableIndex, int row, int col, List<CellSegment> segments) {
        if (header == null) {
            return;
        }
        Hdr hdr = header.getJaxbElement();
        applySegmentsToHeaderFooter(hdr, tableIndex, row, col, segments);
    }

    private void applyHeaderFooterSegments(FooterPart footer, int tableIndex, int row, int col, List<CellSegment> segments) {
        if (footer == null) {
            return;
        }
        Ftr ftr = footer.getJaxbElement();
        applySegmentsToHeaderFooter(ftr, tableIndex, row, col, segments);
    }

    private void applySegmentsToHeaderFooter(ContentAccessor accessor, int tableIndex, int row, int col, List<CellSegment> segments) {
        List<Tbl> tables = getTablesFromContent(accessor.getContent());
        if (tableIndex < 0 || tableIndex >= tables.size()) {
            return;
        }
        Tbl tbl = tables.get(tableIndex);
        List<Tr> rows = getRowsFromTable(tbl);
        if (row < 0 || row >= rows.size()) {
            return;
        }
        List<Tc> cells = getCellsFromRow(rows.get(row));
        if (col < 0 || col >= cells.size()) {
            return;
        }
        setCellSegments(new DocxTableCell(cells.get(col)), segments);
    }

    private static List<Tbl> getBodyTables(MainDocumentPart main) {
        return getTablesFromContent(main.getContent());
    }

    private static List<Tbl> getTablesFromContent(List<Object> content) {
        List<Tbl> tables = new ArrayList<>();
        for (Object o : content) {
            Object unwrapped = XmlUtils.unwrap(o);
            if (unwrapped instanceof Tbl tbl) {
                tables.add(tbl);
                continue;
            }
            if (unwrapped instanceof ContentAccessor accessor) {
                tables.addAll(getTablesFromContent(accessor.getContent()));
            }
        }
        return tables;
    }

    private static List<Tr> getRowsFromTable(Tbl tbl) {
        List<Tr> rows = new ArrayList<>();
        for (Object o : tbl.getContent()) {
            Object unwrapped = XmlUtils.unwrap(o);
            if (unwrapped instanceof Tr tr) {
                rows.add(tr);
            }
        }
        return rows;
    }

    private static List<Tc> getCellsFromRow(Tr tr) {
        List<Tc> cells = new ArrayList<>();
        for (Object o : tr.getContent()) {
            Object unwrapped = XmlUtils.unwrap(o);
            if (unwrapped instanceof Tc tc) {
                cells.add(tc);
            }
        }
        return cells;
    }

    private static void clearCellParagraphs(Tc cell) {
        TcPr tcPr = null;
        for (Object o : cell.getContent()) {
            Object unwrapped = XmlUtils.unwrap(o);
            if (unwrapped instanceof TcPr pr) {
                tcPr = pr;
                break;
            }
        }
        cell.getContent().clear();
        if (tcPr != null) {
            cell.getContent().add(tcPr);
        }
    }

    private static R createRun(String text, boolean bold, boolean padWithSpaces, boolean shadow) {
        R run = WML_FACTORY.createR();
        Text t = WML_FACTORY.createText();
        String paddedText = padWithSpaces ? padWithSpaces(text) : text;
        t.setValue(paddedText);
        if (paddedText.startsWith(" ") || paddedText.endsWith(" ")) {
            t.setSpace("preserve");
        }
        run.getContent().add(t);
        if (bold || shadow) {
            RPr rPr = WML_FACTORY.createRPr();
            if (shadow) {
                BooleanDefaultTrue shadowFlag = new BooleanDefaultTrue();
                rPr.setShadow(shadowFlag);
            }
            if (bold) {
                BooleanDefaultTrue b = new BooleanDefaultTrue();
                rPr.setB(b);
            }
            run.setRPr(rPr);
        }
        return run;
    }

    private static String padWithSpaces(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length() + 2);
        if (!text.startsWith(" ")) {
            sb.append(' ');
        }
        sb.append(text);
        if (!text.endsWith(" ")) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static R createRunWithProps(String text, RPr baseProps) {
        R run = WML_FACTORY.createR();
        Text t = WML_FACTORY.createText();
        t.setValue(text);
        if (text.startsWith(" ") || text.endsWith(" ")) {
            t.setSpace("preserve");
        }
        run.getContent().add(t);

        RPr rPr = baseProps;
        if (rPr == null) {
            rPr = WML_FACTORY.createRPr();
        }
        BooleanDefaultTrue shadow = new BooleanDefaultTrue();
        rPr.setShadow(shadow);
        run.setRPr(rPr);
        return run;
    }

    private boolean updateBookmarkInContent(HeaderPart header, String name, String text) {
        if (header == null) {
            return false;
        }
        return updateBookmarkInContent(header.getJaxbElement().getContent(), name, text);
    }

    private boolean updateBookmarkInContent(FooterPart footer, String name, String text) {
        if (footer == null) {
            return false;
        }
        return updateBookmarkInContent(footer.getJaxbElement().getContent(), name, text);
    }

    private boolean updateBookmarkInContent(List<Object> content, String name, String text) {
        boolean updated = false;
        for (int i = 0; i < content.size(); i++) {
            Object unwrapped = XmlUtils.unwrap(content.get(i));
            if (unwrapped instanceof CTBookmark bookmark && name.equals(bookmark.getName())) {
                BigInteger id = bookmark.getId();
                int endIndex = findBookmarkEndIndex(content, i + 1, id);
                if (endIndex != -1) {
                    RPr baseProps = findFirstRunProps(content, i + 1, endIndex);
                    for (int j = endIndex - 1; j > i; j--) {
                        content.remove(j);
                    }
                    content.add(i + 1, createRunWithProps(text, baseProps));
                    updated = true;
                }
            }
        }

        for (Object o : content) {
            Object unwrapped = XmlUtils.unwrap(o);
            if (unwrapped instanceof ContentAccessor accessor) {
                updated |= updateBookmarkInContent(accessor.getContent(), name, text);
            }
        }
        return updated;
    }

    private static int findBookmarkEndIndex(List<Object> content, int startIndex, BigInteger id) {
        for (int i = startIndex; i < content.size(); i++) {
            Object unwrapped = XmlUtils.unwrap(content.get(i));
            if (unwrapped instanceof CTMarkupRange range && id.equals(range.getId())) {
                return i;
            }
        }
        return -1;
    }

    private static void collectBookmarkIndices(List<Object> content, String prefix, String suffix, TreeSet<Integer> out) {
        for (Object o : content) {
            Object unwrapped = XmlUtils.unwrap(o);
            if (unwrapped instanceof CTBookmark bookmark) {
                String name = bookmark.getName();
                if (name != null && name.startsWith(prefix) && name.endsWith(suffix)) {
                    String middle = name.substring(prefix.length(), name.length() - suffix.length());
                    try {
                        out.add(Integer.parseInt(middle));
                    } catch (NumberFormatException ignored) {
                        // ignore non-numeric bookmark names
                    }
                }
            } else if (unwrapped instanceof ContentAccessor accessor) {
                collectBookmarkIndices(accessor.getContent(), prefix, suffix, out);
            }
        }
    }

    private static RPr findFirstRunProps(List<Object> content, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            Object unwrapped = XmlUtils.unwrap(content.get(i));
            if (unwrapped instanceof R run) {
                if (run.getRPr() != null) {
                    return run.getRPr();
                }
            }
        }
        return null;
    }
}

