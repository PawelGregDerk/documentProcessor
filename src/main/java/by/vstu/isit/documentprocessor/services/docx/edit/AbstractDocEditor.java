package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import com.spire.doc.*;
import com.spire.doc.BookmarkStart;
import com.spire.doc.documents.Paragraph;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.FileReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;
@Slf4j
public abstract class AbstractDocEditor {

    protected final String srcPath;
    protected final String destPath;
    private Path lastResolvedSource;

    protected AbstractDocEditor(String srcPath, String destPath) {
        this.srcPath = srcPath;
        // Destination path template is derived from originals output template.
        // This guarantees that "copy_*" updates are generated under the same root as originals
        // configured via application.properties (out.*.path), even if legacy copy.out.* properties exist.
        this.destPath = srcPath;
    }

    public abstract void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception;

    protected String copyFolder(String folder) {
        return "копия_" + folder;
    }

    protected Document loadCopy(String folder, String srcName, String destName) throws IOException {
        Path src = Path.of(format(srcPath, folder, srcName));
        Path altSrc = Path.of(format(srcPath, folder, destName));
        Path dest = Path.of(format(destPath, copyFolder(folder), destName));
        Path resolvedSrc = resolveSource(src, altSrc);

        if (resolvedSrc == null) {
            throw new NoSuchFileException("No source document found in originals. Tried: " + src + ", " + altSrc);
        }

        Files.createDirectories(dest.getParent());
        Files.copy(resolvedSrc, dest, StandardCopyOption.REPLACE_EXISTING);
        lastResolvedSource = resolvedSrc;

        Document doc = new Document();
        doc.loadFromFile(dest.toString());
        return doc;
    }

    protected Document loadCopy(String folder, String name) throws IOException {
        return loadCopy(folder, name, name);
    }

    protected Document loadCopySub(String folder, String sub, String srcName, String destName) throws IOException {
        Path src = Path.of(format(srcPath, folder, sub, srcName));
        Path altSrc = Path.of(format(srcPath, folder, sub, destName));
        Path dest = Path.of(format(destPath, copyFolder(folder), sub, destName));
        Path resolvedSrc = resolveSource(src, altSrc);

        if (resolvedSrc == null) {
            throw new NoSuchFileException("No source document found in originals. Tried: " + src + ", " + altSrc);
        }

        Files.createDirectories(dest.getParent());
        Files.copy(resolvedSrc, dest, StandardCopyOption.REPLACE_EXISTING);
        lastResolvedSource = resolvedSrc;

        Document doc = new Document();
        doc.loadFromFile(dest.toString());
        return doc;
    }

    private Path resolveSource(Path... candidates) {
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    protected Document loadCopySub(String folder, String sub, String name) throws IOException {
        return loadCopySub(folder, sub, name, name);
    }

    protected Document loadCopyByBookmark(String folder, String srcName, String destName, int tableIndex, String bookmarkName) throws Exception {
        try {
            return loadCopy(folder, srcName, destName);
        } catch (NoSuchFileException ex) {
            if (bookmarkName == null || bookmarkName.isBlank()) {
                throw ex;
            }
            Path sourceDir = Path.of(format(srcPath, folder, srcName)).getParent();
            Path found = findSourceByBookmark(sourceDir, tableIndex, bookmarkName);
            if (found == null) {
                throw ex;
            }
            return loadCopyFromExplicitSource(found, Path.of(format(destPath, copyFolder(folder), destName)));
        }
    }

    protected Document loadCopySubByBookmark(String folder, String sub, String srcName, String destName, int tableIndex, String bookmarkName) throws Exception {
        try {
            return loadCopySub(folder, sub, srcName, destName);
        } catch (NoSuchFileException ex) {
            if (bookmarkName == null || bookmarkName.isBlank()) {
                throw ex;
            }
            Path sourceDir = Path.of(format(srcPath, folder, sub, srcName)).getParent();
            Path found = findSourceByBookmark(sourceDir, tableIndex, bookmarkName);
            if (found == null) {
                throw ex;
            }
            return loadCopyFromExplicitSource(found, Path.of(format(destPath, copyFolder(folder), sub, destName)));
        }
    }

    private Document loadCopyFromExplicitSource(Path source, Path dest) throws IOException {
        Files.createDirectories(dest.getParent());
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        lastResolvedSource = source;
        Document doc = new Document();
        doc.loadFromFile(dest.toString());
        return doc;
    }

    protected Map<String, String> resolveHeaderOldData(Map<String, String> fallback) {
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

    private Path metaPathFor(Path sourceDoc) {
        Path metaDir = sourceDoc.getParent().resolve(".docproc-meta");
        return metaDir.resolve(sourceDoc.getFileName().toString() + ".hdr.properties");
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
        doc.saveToFile(Path.of(format(destPath, copyFolder(folder), name)).toString(), FileFormat.Docx);
        doc.close();
    }

    protected void saveSub(Document doc, String folder, String sub, String name) {
        doc.saveToFile(Path.of(format(destPath, copyFolder(folder), sub, name)).toString(), FileFormat.Docx);
        doc.close();
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
            boolean inBookmark = false;
            int bookmarkStartIdx = -1;
            for (int m = 0; m < para.getChildObjects().getCount(); m++) {
                var obj = para.getChildObjects().get(m);
                if (obj instanceof BookmarkStart bm && bookmarkName.equals(bm.getName())) {
                    inBookmark = true;
                    bookmarkStartIdx = m;
                    log.info("Found bookmark start: {} at index {}", bookmarkName, m);
                    continue;
                }
                if (obj instanceof com.spire.doc.BookmarkEnd && inBookmark) {
                    // TextRange не найден между start и end — добавляем новый
                    log.info("Bookmark end found without TextRange, appending text: '{}'", newText);
                    com.spire.doc.fields.TextRange tr = para.appendText(newText);
                    tr.getCharacterFormat().isShadow(true);
                    return;
                }
                if (inBookmark && obj instanceof com.spire.doc.fields.TextRange tr) {
                    boolean changed = !newText.equals(tr.getText().trim());
                    log.info("Updating TextRange for bookmark '{}': '{}' -> '{}'", bookmarkName, tr.getText(), newText);
                    tr.setText(newText);
                    if (changed) {
                        tr.getCharacterFormat().isShadow(true);
                    }
                    return;
                }
            }
        }
        // Если закладка не найдена, логируем это
        log.warn("Bookmark '{}' not found in cell", bookmarkName);
    }

    protected void updateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
        for (var key : oldData.keySet()) {
            String oldVal = oldData.get(key);
            String newVal = newData.get(key);
            if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
                replaceAllOccurrences(doc, oldVal, newVal);
            }
        }
    }

    private void replaceAllOccurrences(Document doc, String oldValue, String newValue) {
        if (oldValue == null || oldValue.isEmpty()) {
            return;
        }
        doc.replace(oldValue, newValue, true, false);
    }

    protected TableRow addRowWithShading(Table table, int columnCount) {
        TableRow row = table.addRow();
        while (row.getCells().getCount() < columnCount) {
            row.addCell();
        }
        return row;
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
            table.getRows().insert(insertIdx + 1, newRow);
        } else {
            newRow = table.addRow();
        }
        while (newRow.getCells().getCount() < columnCount) {
            newRow.addCell();
        }
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
        Paragraph para = cell.getParagraphs().getCount() > 0
                ? cell.getParagraphs().get(0)
                : cell.addParagraph();
        para.setText("");
        com.spire.doc.fields.TextRange tr = para.appendText(text);
        tr.getCharacterFormat().isShadow(true);
    }

    protected void clearCell(TableCell cell) {
        Paragraph para = cell.getParagraphs().getCount() > 0
                ? cell.getParagraphs().get(0)
                : cell.addParagraph();
        para.setText("");
    }

    protected void setShadedBookmarkedText(TableCell cell, String bookmarkName, String text) {
        Paragraph para = cell.getParagraphs().getCount() > 0
                ? cell.getParagraphs().get(0)
                : cell.addParagraph();
        para.setText("");
        appendShadedBookmark(para, bookmarkName, text);
    }

    protected void appendShadedBookmark(Paragraph para, String bookmarkName, String text) {
        para.appendBookmarkStart(bookmarkName);
        com.spire.doc.fields.TextRange tr = para.appendText(text == null ? "" : text);
        tr.getCharacterFormat().isShadow(true);
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

    protected String designationsAssemblyUnit(List<SborEdDto> list) {
        return list.size() > 1 ? "согласно таблице 1" : list.getFirst().oboznach();
    }
}
