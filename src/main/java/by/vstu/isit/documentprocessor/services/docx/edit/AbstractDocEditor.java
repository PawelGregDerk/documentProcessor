package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import com.spire.doc.*;
import com.spire.doc.BookmarkStart;
import com.spire.doc.documents.Paragraph;
import com.spire.doc.documents.Paragraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;

public abstract class AbstractDocEditor {

    protected final String srcPath;
    protected final String destPath;

    protected AbstractDocEditor(String srcPath, String destPath) {
        this.srcPath = srcPath;
        this.destPath = destPath;
    }

    public abstract void edit(DockPackageDto dto) throws Exception;

    protected Document loadCopy(String folder, String srcName, String destName) throws IOException {
        Path src = Path.of(format(srcPath, folder, srcName));
        Path dest = Path.of(format(destPath, folder, destName));
        Files.createDirectories(dest.getParent());
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        Document doc = new Document();
        doc.loadFromFile(dest.toString());
        return doc;
    }

    protected Document loadCopy(String folder, String name) throws IOException {
        return loadCopy(folder, name, name);
    }

    protected Document loadCopySub(String folder, String sub, String srcName, String destName) throws IOException {
        Path src = Path.of(format(srcPath, folder, sub, srcName));
        Path dest = Path.of(format(destPath, folder, sub, destName));
        Files.createDirectories(dest.getParent());
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        Document doc = new Document();
        doc.loadFromFile(dest.toString());
        return doc;
    }

    protected Document loadCopySub(String folder, String sub, String name) throws IOException {
        return loadCopySub(folder, sub, name, name);
    }

    protected void save(Document doc, String folder, String name) {
        doc.saveToFile(Path.of(format(destPath, folder, name)).toString(), FileFormat.Docx);
        doc.close();
    }

    protected void saveSub(Document doc, String folder, String sub, String name) {
        doc.saveToFile(Path.of(format(destPath, folder, sub, name)).toString(), FileFormat.Docx);
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
            for (int m = 0; m < para.getChildObjects().getCount(); m++) {
                var obj = para.getChildObjects().get(m);
                if (obj instanceof BookmarkStart bm && bookmarkName.equals(bm.getName())) {
                    inBookmark = true;
                    continue;
                }
                if (obj instanceof com.spire.doc.BookmarkEnd && inBookmark) {
                    break;
                }
                if (inBookmark && obj instanceof com.spire.doc.fields.TextRange tr) {
                    boolean changed = !newText.equals(tr.getText().trim());
                    tr.setText(newText);
                    if (changed) {
                        tr.getCharacterFormat().isShadow(true);
                    }
                    return;
                }
            }
        }
    }

    protected void updateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
        for (var key : oldData.keySet()) {
            String oldVal = oldData.get(key);
            String newVal = newData.get(key);
            if (oldVal != null && newVal != null && !oldVal.equals(newVal)) {
                doc.replace(oldVal, newVal, false, false);
            }
        }
    }

    protected TableRow addRowWithShading(Table table, int columnCount) {
        TableRow row = table.addRow();
        while (row.getCells().getCount() < columnCount) {
            row.addCell();
        }
        return row;
    }

    protected TableRow insertRowAfterLastBookmark(Table table, String operBookmarkPrefix, int columnCount) {
        int lastIdx = -1;
        for (int i = 0; i < table.getRows().getCount(); i++) {
            TableRow row = table.getRows().get(i);
            for (int j = 0; j < row.getCells().getCount(); j++) {
                TableCell cell = row.getCells().get(j);
                for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
                    Paragraph para = cell.getParagraphs().get(k);
                    for (int m = 0; m < para.getChildObjects().getCount(); m++) {
                        var obj = para.getChildObjects().get(m);
                        if (obj instanceof BookmarkStart bm && bm.getName().startsWith(operBookmarkPrefix)) {
                            lastIdx = i;
                        }
                    }
                }
            }
        }
        TableRow newRow;
        if (lastIdx >= 0) {
            TableRow template = table.getRows().get(lastIdx);
            newRow = (TableRow) template.deepClone();
            for (int j = 0; j < newRow.getCells().getCount(); j++) {
                TableCell cell = newRow.getCells().get(j);
                for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
                    cell.getParagraphs().get(k).setText("");
                }
            }
            table.getRows().insert(lastIdx + 1, newRow);
        } else {
            newRow = table.addRow();
        }
        while (newRow.getCells().getCount() < columnCount) {
            newRow.addCell();
        }
        return newRow;
    }

    protected void setShadedText(TableCell cell, String text) {
        Paragraph para = cell.getParagraphs().getCount() > 0
                ? cell.getParagraphs().get(0)
                : cell.addParagraph();
        para.setText("");
        com.spire.doc.fields.TextRange tr = para.appendText(text);
        tr.getCharacterFormat().isShadow(true);
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

    protected String designationsAssemblyUnit(List<by.vstu.isit.documentprocessor.dto.SborEdDto> list) {
        return list.getFirst().oboznach() + "\u2014" + list.getLast().oboznach();
    }
}
