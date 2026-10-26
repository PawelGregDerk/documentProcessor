package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import com.spire.doc.*;
import com.spire.doc.BookmarkStart;
import com.spire.doc.documents.CellMerge;
import com.spire.doc.documents.Paragraph;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;
@Slf4j
public abstract class AbstractDocEditor {

    protected final String srcPath;
    protected final String destPath;
    private final DocEditorFileManager fileManager;
    private Map<String, String> lastHeaderData;

    public void setSourceFolderOverride(String sourceFolderOverride) {
        fileManager.setSourceFolderOverride(sourceFolderOverride);
    }

    protected String getSourceFolderOverride() {
        return fileManager.getSourceFolderOverride();
    }

    protected AbstractDocEditor(String srcPath, String destPath) {
        this.srcPath = srcPath;
        // Destination path template is derived from originals output template.
        // This guarantees that "copy_*" updates are generated under the same root as originals
        // configured via application.properties (out.*.path), even if legacy copy.out.* properties exist.
        this.destPath = srcPath;
        this.fileManager = new DocEditorFileManager(srcPath);
    }

    public abstract void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception;

    protected String copyFolder(String folder) {
        return fileManager.copyFolder(folder);
    }

    protected Document loadCopy(String folder, String srcName, String destName) throws IOException {
        return fileManager.loadCopy(folder, srcName, destName);
    }

    protected Document loadCopy(String folder, String name) throws IOException {
        return fileManager.loadCopy(folder, name);
    }

    protected Document loadCopySub(String folder, String sub, String srcName, String destName) throws IOException {
        return fileManager.loadCopySub(folder, sub, srcName, destName);
    }

    protected Document loadCopySub(String folder, String sub, String name) throws IOException {
        return fileManager.loadCopySub(folder, sub, name);
    }

    protected Document loadCopyByBookmark(String folder, String srcName, String destName, int tableIndex, String bookmarkName) throws Exception {
        try {
            return loadCopy(folder, srcName, destName);
        } catch (NoSuchFileException ex) {
            if (bookmarkName == null || bookmarkName.isBlank()) {
                throw ex;
            }
            String srcFolder = !getSourceFolderOverride().isEmpty() ? getSourceFolderOverride() : folder;
            Path sourceDir = Path.of(format(srcPath, srcFolder, srcName)).getParent();
            Path found = findSourceByBookmark(sourceDir, tableIndex, bookmarkName);
            if (found == null) {
                throw ex;
            }
            return fileManager.loadFromExplicitSource(found, Path.of(format(destPath, copyFolder(folder), destName)));
        }
    }

    protected Document loadCopySubByBookmark(String folder, String sub, String srcName, String destName, int tableIndex, String bookmarkName) throws Exception {
        try {
            return loadCopySub(folder, sub, srcName, destName);
        } catch (NoSuchFileException ex) {
            if (bookmarkName == null || bookmarkName.isBlank()) {
                throw ex;
            }
            String srcFolder = !getSourceFolderOverride().isEmpty() ? getSourceFolderOverride() : folder;
            Path sourceDir = Path.of(format(srcPath, srcFolder, sub, srcName)).getParent();
            Path found = findSourceByBookmark(sourceDir, tableIndex, bookmarkName);
            if (found == null) {
                throw ex;
            }
            return fileManager.loadFromExplicitSource(found, Path.of(format(destPath, copyFolder(folder), sub, destName)));
        }
    }

    protected Map<String, String> resolveHeaderOldData(Map<String, String> fallback) {
        return fileManager.resolveHeaderOldData(fallback);
    }

    private Path findSourceByBookmark(Path sourceDir, int tableIndex, String bookmarkName) {
        if (sourceDir == null || !Files.isDirectory(sourceDir)) {
            return null;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir, "*.docx")) {
            for (Path file : stream) {
                if (containsBookmark(file, tableIndex, bookmarkName)) {
                    return file;
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private boolean containsBookmark(Path file, int tableIndex, String bookmarkName) {
        Document doc = new Document();
        try {
            doc.loadFromFile(file.toString());
            if (doc.getSections().getCount() == 0) {
                return false;
            }
            var tables = doc.getSections().get(0).getTables();
            if (tableIndex < 0 || tables.getCount() <= tableIndex) {
                return false;
            }
            return findCellByBookmark(tables.get(tableIndex), bookmarkName) != null;
        } catch (Exception e) {
            return false;
        } finally {
            doc.close();
        }
    }

    protected void save(Document doc, String folder, String name) {
        fileManager.save(doc, folder, name, lastHeaderData);
    }

    protected void saveSub(Document doc, String folder, String sub, String name) {
        fileManager.saveSub(doc, folder, sub, name, lastHeaderData);
    }

    protected TableRow findRowByBookmark(Table table, String bookmarkName) {
        for (int i = 0; i < table.getRows().getCount(); i++) {
            TableRow row = table.getRows().get(i);
            for (int j = 0; j < row.getCells().getCount(); j++) {
                TableCell cell = row.getCells().get(j);
                for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
                    Paragraph para = cell.getParagraphs().get(k);
                    for (int m = 0; m < para.getChildObjects().getCount(); m++) {
                        var obj = para.getChildObjects().get(m);
                        if (obj instanceof BookmarkStart bm && bookmarkName.equals(bm.getName())) {
                            return row;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected TableCell findCellByBookmark(Table table, String bookmarkName) {
        for (int i = 0; i < table.getRows().getCount(); i++) {
            TableRow row = table.getRows().get(i);
            for (int j = 0; j < row.getCells().getCount(); j++) {
                TableCell cell = row.getCells().get(j);
                for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
                    Paragraph para = cell.getParagraphs().get(k);
                    for (int m = 0; m < para.getChildObjects().getCount(); m++) {
                        var obj = para.getChildObjects().get(m);
                        if (obj instanceof BookmarkStart bm && bookmarkName.equals(bm.getName())) {
                            return cell;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected void updateCell(Table table, String bookmarkName, String newText) {
        TableCell cell = findCellByBookmark(table, bookmarkName);
        if (cell == null) return;
        updateBookmarkText(cell, bookmarkName, newText);
    }

    protected void clearBookmarkText(Table table, String bookmarkName) {
        TableCell cell = findCellByBookmark(table, bookmarkName);
        if (cell == null) return;
        updateBookmarkText(cell, bookmarkName, "");
    }

    private void updateBookmarkText(TableCell cell, String bookmarkName, String newText) {
        for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
            Paragraph para = cell.getParagraphs().get(k);
            int startIdx = -1;
            int endIdx = -1;

            for (int m = 0; m < para.getChildObjects().getCount(); m++) {
                var obj = para.getChildObjects().get(m);
                if (obj instanceof BookmarkStart bm && bookmarkName.equals(bm.getName())) {
                    startIdx = m;
                }
                if (startIdx >= 0 && obj instanceof com.spire.doc.BookmarkEnd && endIdx < 0) {
                    endIdx = m;
                    break;
                }
            }

            if (startIdx >= 0 && endIdx >= 0) {
                for (int m = endIdx - 1; m > startIdx; m--) {
                    para.getChildObjects().remove(para.getChildObjects().get(m));
                }
                com.spire.doc.fields.TextRange tr = para.appendText(newText);
                tr.getCharacterFormat().setFontName("Arial Narrow");
                para.getChildObjects().remove(tr);
                para.getChildObjects().insert(startIdx + 1, tr);
                log.info("Updated bookmark '{}' with text '{}'", bookmarkName, newText);
                return;
            }
        }
        log.warn("Bookmark '{}' not found in cell", bookmarkName);
    }

    protected void updateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
        for (var key : oldData.keySet()) {
            String oldVal = oldData.get(key);
            String newVal = newData.get(key);
            if (newVal != null) {
                log.info("updateHeader: replace placeholder [{}] with [{}]", "{{" + key + "}}", newVal);
                replaceAllOccurrences(doc, "{{" + key + "}}", newVal);
            }
            if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
                log.info("updateHeader: replace value [{}] with [{}] (key={})", oldVal, newVal, key);
                replaceAllOccurrences(doc, oldVal, newVal);
            }
        }
        this.lastHeaderData = newData;
    }

    private void replaceAllOccurrences(Document doc, String oldValue, String newValue) {
        if (oldValue == null || oldValue.isEmpty()) {
            return;
        }
        doc.replace(oldValue, newValue, true, false);
    }

    protected TableRow addRowWithShading(Table table, int columnCount) {
        TableRow row = table.addRow();
        clearVerticalMerge(row);
        while (row.getCells().getCount() < columnCount) {
            row.addCell();
        }
        setDefaultRowFont(row);
        setDefaultRowPaddings(row);
        return row;
    }

    protected void setDefaultRowFont(TableRow row) {
        com.spire.doc.documents.ParagraphStyle normalStyle =
                (com.spire.doc.documents.ParagraphStyle) row.getDocument().getStyles().findByName("Normal");
        if (normalStyle != null) {
            normalStyle.getCharacterFormat().setFontName("Arial Narrow");
            normalStyle.getCharacterFormat().setFontNameAscii("Arial Narrow");
            normalStyle.getCharacterFormat().setFontNameNonFarEast("Arial Narrow");
        }
    }

    protected void setDefaultRowPaddings(TableRow row) {
        for (int j = 0; j < row.getCells().getCount(); j++) {
            row.getCells().get(j).getCellFormat().getPaddings().setAll(1.1f);
        }
    }

    protected void clearVerticalMerge(TableRow row) {
        for (int i = 0; i < row.getCells().getCount(); i++) {
            row.getCells().get(i).getCellFormat().setVerticalMerge(CellMerge.None);
        }
    }

    protected void copySpireCellWidths(TableRow template, TableRow target) {
        for (int i = 0; i < template.getCells().getCount()
                && i < target.getCells().getCount(); i++) {
            float width = template.getCells().get(i).getWidth();
            if (width > 0) {
                target.getCells().get(i).setWidth(width);
            }
        }
    }

    protected TableRow insertRowAfterLastBookmark(Table table, String operBookmarkPrefix, int columnCount) {
        int lastOperStartIdx = -1;
        for (int i = 0; i < table.getRows().getCount(); i++) {
            TableRow row = table.getRows().get(i);
            for (int j = 0; j < row.getCells().getCount(); j++) {
                TableCell cell = row.getCells().get(j);
                for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
                    Paragraph para = cell.getParagraphs().get(k);
                    for (int m = 0; m < para.getChildObjects().getCount(); m++) {
                        var obj = para.getChildObjects().get(m);
                        if (obj instanceof BookmarkStart bm && bm.getName().startsWith(operBookmarkPrefix)) {
                            lastOperStartIdx = i;
                        }
                    }
                }
            }
        }
        
        // Находим конец блока последней операции
        int insertIdx = lastOperStartIdx;
        if (lastOperStartIdx >= 0) {
            for (int i = lastOperStartIdx + 1; i < table.getRows().getCount(); i++) {
                if (containsOperStartBookmark(table.getRows().get(i), operBookmarkPrefix)) {
                    break;
                }
                insertIdx = i;
            }
        }
        
        TableRow newRow;
        if (insertIdx >= 0) {
            newRow = table.addRow();
            clearVerticalMerge(newRow);
            table.getRows().insert(insertIdx + 1, newRow);
        } else {
            newRow = table.addRow();
            clearVerticalMerge(newRow);
        }
        while (newRow.getCells().getCount() < columnCount) {
            newRow.addCell();
        }
        setDefaultRowFont(newRow);
        setDefaultRowPaddings(newRow);
        return newRow;
    }

    protected boolean containsOperStartBookmark(TableRow row, String operBookmarkPrefix) {
        for (int j = 0; j < row.getCells().getCount(); j++) {
            TableCell cell = row.getCells().get(j);
            for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
                Paragraph para = cell.getParagraphs().get(k);
                for (int m = 0; m < para.getChildObjects().getCount(); m++) {
                    var obj = para.getChildObjects().get(m);
                    if (obj instanceof BookmarkStart bm && bm.getName().startsWith(operBookmarkPrefix)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void setShadedText(TableCell cell, String text) {
        clearCell(cell);
        Paragraph para = cell.getParagraphs().get(0);
        com.spire.doc.fields.TextRange tr = para.appendText(text);
        tr.getCharacterFormat().setFontName("Arial Narrow");
    }

    protected void clearCell(TableCell cell) {
        for (int k = cell.getParagraphs().getCount() - 1; k >= 0; k--) {
            Paragraph para = cell.getParagraphs().get(k);
            while (para.getChildObjects().getCount() > 0) {
                para.getChildObjects().remove(para.getChildObjects().get(0));
            }
            if (k > 0) {
                cell.getParagraphs().remove(para);
            }
        }
        if (cell.getParagraphs().getCount() == 0) {
            cell.addParagraph();
        }
    }

    protected void setShadedBookmarkedText(TableCell cell, String bookmarkName, String text) {
        clearCell(cell);
        Paragraph para = cell.getParagraphs().get(0);
        appendShadedBookmark(para, bookmarkName, text);
    }

    protected void appendShadedBookmark(Paragraph para, String bookmarkName, String text) {
        if (para.getChildObjects().getCount() == 1) {
            var first = para.getChildObjects().get(0);
            if (first instanceof com.spire.doc.fields.TextRange tr
                    && (tr.getText() == null || tr.getText().isEmpty())) {
                para.getChildObjects().remove(tr);
            }
        }
        if (para.getChildObjects().getCount() > 0) {
            com.spire.doc.fields.TextRange space = para.appendText(" ");
            space.getCharacterFormat().setFontName("Arial Narrow");
        }
        para.appendBookmarkStart(bookmarkName);
        com.spire.doc.fields.TextRange tr = para.appendText(text == null ? "" : text);
        tr.getCharacterFormat().setFontName("Arial Narrow");
        para.appendBookmarkEnd(bookmarkName);
    }

    protected Set<Long> getAllBookmarkIds(Table table, String prefix, String colSuffix) {
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < table.getRows().getCount(); i++) {
            TableRow row = table.getRows().get(i);
            for (int j = 0; j < row.getCells().getCount(); j++) {
                TableCell cell = row.getCells().get(j);
                for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
                    var para = cell.getParagraphs().get(k);
                    for (int m = 0; m < para.getChildObjects().getCount(); m++) {
                        var obj = para.getChildObjects().get(m);
                        if (obj instanceof BookmarkStart bm) {
                            String name = bm.getName();
                            if (name.startsWith(prefix) && name.endsWith(colSuffix)) {
                                String idStr = name.substring(prefix.length(), name.length() - colSuffix.length());
                                try { ids.add(Long.parseLong(idStr)); } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                }
            }
        }
        return ids;
    }

    protected void removeDeletedRows(Table table, Set<Long> keepIds, String prefix, String colSuffix) {
        Set<Long> existingIds = getAllBookmarkIds(table, prefix, colSuffix);
        for (Long id : existingIds) {
            if (!keepIds.contains(id)) {
                TableRow toRemove = findRowByBookmark(table, prefix + id + colSuffix);
                if (toRemove != null) {
                    table.getRows().remove(toRemove);
                }
            }
        }
    }

    protected int rowIndexOf(Table table, TableRow row) {
        for (int i = 0; i < table.getRows().getCount(); i++) {
            if (table.getRows().get(i) == row) {
                return i;
            }
        }
        return -1;
    }

    protected int findOperationBlockEnd(Table table, int startIndex) {
        for (int i = startIndex + 1; i < table.getRows().getCount(); i++) {
            if (containsOperStartBookmark(table.getRows().get(i), "oper_")) {
                return i - 1;
            }
        }
        return table.getRows().getCount() - 1;
    }

    protected void clearRowTexts(TableRow row) {
        for (int j = 0; j < row.getCells().getCount(); j++) {
            TableCell cell = row.getCells().get(j);
            for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
                cell.getParagraphs().get(k).setText("");
            }
        }
    }

    protected String designationsAssemblyUnit(List<SborEdDto> list) {
        return list.size() > 1 ? "согласно таблице 1" : list.getFirst().oboznach();
    }

    protected String dseText(List<SborEdDto> sborEds, String singleValue) {
        return sborEds.size() > 1 ? "Таблица 01" : singleValue;
    }
}
