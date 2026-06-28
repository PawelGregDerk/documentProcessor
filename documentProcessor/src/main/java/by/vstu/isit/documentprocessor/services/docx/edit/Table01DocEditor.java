package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.Table01WordGenerator;
import com.spire.doc.*;
import com.spire.doc.documents.Paragraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

@Service
public class Table01DocEditor extends AbstractDocEditor {

    private final Table01WordGenerator generator;

    public Table01DocEditor(
            @Value("${out.tab01.path}") String outPath,
            Table01WordGenerator generator
    ) {
        super(outPath, outPath);
        this.generator = generator;
    }

    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        Path original = Path.of(MessageFormat.format(srcPath, savedDto.path()));
        String copyFolder = "копия_" + savedDto.path();
        Path copy = Path.of(MessageFormat.format(srcPath, copyFolder));

        int size = savedDto.sborEds().size();

        if (!Files.exists(original) && size <= 1) {
            return;
        }

        if (!Files.exists(original) && size > 1) {
            generator.generate(savedDto);
            Files.createDirectories(copy.getParent());
            Files.copy(original, copy, StandardCopyOption.REPLACE_EXISTING);
            editCopy(copy, savedDto);
            return;
        }

        if (Files.exists(original) && size > 1) {
            Files.createDirectories(copy.getParent());
            Files.copy(original, copy, StandardCopyOption.REPLACE_EXISTING);
            editCopy(copy, savedDto);
        }
    }

    // ---------------------------------------------------------
    // РЕДАКТИРОВАНИЕ КОПИИ
    // ---------------------------------------------------------

    private void editCopy(Path copy, DockPackageDto savedDto) {

        Document doc = new Document();
        doc.loadFromFile(copy.toString());

        Table table = doc.getSections().get(0).getTables().get(0);
        Set<Long> processed = new HashSet<>();

        for (SborEdDto se : savedDto.sborEds()) {

            processed.add(se.id());

            TableCell cellName = findCellByBookmark(table, "t01_name_" + se.id());

            if (cellName != null) {
                updateBookmark(cellName, "t01_name_" + se.id(),
                        se.nazv() + ", " + se.oboznach() + ", " + generator.fillFirstCell(savedDto.packageName(), se));
            } else {
                // Добавляем новую строку
                TableRow row = table.addRow();
                while (row.getCells().getCount() < 5) row.addCell();

                setBookmarked(row.getCells().get(0), "t01_name_" + se.id(),
                        se.nazv() + ", " + se.oboznach() + ", " + generator.fillFirstCell(savedDto.packageName(), se));
            }
        }

        removeDeletedRows(table, processed);

        doc.saveToFile(copy.toString(), FileFormat.Docx);
        doc.close();
    }

    // ---------------------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ---------------------------------------------------------

    private void removeDeletedRows(Table table, Set<Long> keepIds) {
        for (int r = 2; r < table.getRows().getCount(); ) {
            TableRow row = table.getRows().get(r);

            boolean keep = keepIds.stream()
                    .anyMatch(id -> findCellByBookmark(row, "t01_name_" + id) != null);

            if (!keep) {
                table.getRows().remove(row);
            } else {
                r++;
            }
        }
    }

    @Override
    protected TableCell findCellByBookmark(Table table, String name) {
        for (int r = 0; r < table.getRows().getCount(); r++) {
            TableCell cell = findCellByBookmark(table.getRows().get(r), name);
            if (cell != null) return cell;
        }
        return null;
    }

    private TableCell findCellByBookmark(TableRow row, String name) {
        for (int c = 0; c < row.getCells().getCount(); c++) {
            TableCell cell = row.getCells().get(c);
            for (Paragraph para : (Iterable<Paragraph>) cell.getParagraphs()) {
                for (int o = 0; o < para.getChildObjects().getCount(); o++) {
                    if (para.getChildObjects().get(o) instanceof BookmarkStart bm &&
                            bm.getName().equals(name)) {
                        return cell;
                    }
                }
            }
        }
        return null;
    }

    private void updateBookmark(TableCell cell, String name, String text) {
        for (Paragraph para : (Iterable<Paragraph>) cell.getParagraphs()) {
            boolean inside = false;

            for (int i = 0; i < para.getChildObjects().getCount(); i++) {
                var obj = para.getChildObjects().get(i);

                if (obj instanceof BookmarkStart bm && bm.getName().equals(name)) {
                    inside = true;
                    continue;
                }

                if (inside && obj instanceof com.spire.doc.fields.TextRange tr) {
                    tr.setText(text == null ? "" : text);
                    return;
                }
            }
        }
    }

    private void setBookmarked(TableCell cell, String name, String text) {
        Paragraph para = cell.addParagraph();
        para.appendBookmarkStart(name);
        para.appendText(text == null ? "" : text);
        para.appendBookmarkEnd(name);
    }
}
