package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import com.spire.doc.BookmarkStart;
import com.spire.doc.Table;
import com.spire.doc.*;
import com.spire.doc.documents.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class PuDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 14;

    public PuDocEditor(
            @Value("${out.pu.path}") String src
    ) {
        super(src, src);
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        Document doc = loadCopyByBookmark(dto.path(), dto.puName(), savedDto.puName(), 0, sourceBookmark(dto));
        Table table = doc.getSections().get(0).getTables().get(0);

        Set<Long> processedOperIds = new HashSet<>();

        for (OperDto oper : savedDto.opers()) {
            processedOperIds.add(oper.id());
            String bookmarkName = "oper_" + oper.id() + "_col_0";
            boolean exists = findCellByBookmark(table, bookmarkName) != null;

            log.info("Processing operation: id={}, name={}, exists={}", oper.id(), oper.name(), exists);

            if (exists) {
                log.info("Updating existing operation: {}", bookmarkName);
                updateCell(table, "oper_" + oper.id() + "_col_0", oper.numOper());
                updateCell(table, "oper_" + oper.id() + "_col_1", oper.name());
                updateCell(table, "oper_" + oper.id() + "_numZech", oper.numZech());
                updateCell(table, "oper_" + oper.id() + "_oborud", oper.oborud());
                updateCell(table, "oper_" + oper.id() + "_ostnasInstr", oper.ostnasInstr());
            } else {
                log.info("Adding new operation: {}", bookmarkName);
                TableRow row = insertRowAfterLastBookmark(table, "oper_", COLUMN_COUNT);
                setShadedBookmarkedText(row.getCells().get(0), "oper_" + oper.id() + "_col_0", oper.numOper());
                setShadedBookmarkedText(row.getCells().get(1), "oper_" + oper.id() + "_col_1", oper.name());
                appendShadedBookmark(row.getCells().get(1).getParagraphs().get(0), "oper_" + oper.id() + "_numZech", oper.numZech());
                setShadedBookmarkedText(row.getCells().get(2), "oper_" + oper.id() + "_oborud", oper.oborud());
                appendShadedBookmark(row.getCells().get(2).getParagraphs().get(0), "oper_" + oper.id() + "_ostnasInstr", oper.ostnasInstr());
            }
            rebuildOperationFunctionRows(table, oper);
        }

        removeDeletedRows(table, processedOperIds, "oper_", "_col_0");
        rebuildOperMerges(table, savedDto);

        String article = savedDto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(savedDto.sborEds());
        String origArticle = dto.sborEds().getFirst().nazv() + " "
                + dto.sborEds().getFirst().oboznach();
        Map<String, String> oldHeader = resolveHeaderOldData(
                Map.of("d", origArticle, "n", dto.puName(), "p", dseText(dto.sborEds(), dto.packageName()))
        );
        updateHeader(doc, oldHeader, Map.of("d", article, "n", savedDto.puName(), "p", dseText(savedDto.sborEds(), savedDto.packageName())));

        save(doc, savedDto.path(), savedDto.puName());
    }

    private String sourceBookmark(DockPackageDto dto) {
        if (dto.opers().isEmpty() || dto.opers().getFirst().funcs().isEmpty()) {
            return null;
        }
        var func = dto.opers().getFirst().funcs().getFirst();
        return func.id() == null ? null : "func_" + func.id() + "_col_6";
    }

    private void rebuildOperMerges(Table table, DockPackageDto dto) {
        for (OperDto oper : dto.opers()) {
            TableRow startRow = findRowByBookmark(table, "oper_" + oper.id() + "_col_0");
            if (startRow == null) {
                continue;
            }
            int start = -1;
            for (int i = 0; i < table.getRows().getCount(); i++) {
                if (table.getRows().get(i) == startRow) {
                    start = i;
                    break;
                }
            }
            if (start < 0) {
                continue;
            }
            int rowSpan = Math.max(oper.funcs().size(), 1);
            int end = Math.min(table.getRows().getCount() - 1, start + rowSpan - 1);
            if (end <= start) {
                continue;
            }
            table.applyVerticalMerge(0, start, end);
            table.applyVerticalMerge(1, start, end);
            table.applyVerticalMerge(2, start, end);
            table.applyVerticalMerge(12, start, end);
            table.applyVerticalMerge(13, start, end);
        }
    }

    private void rebuildOperationFunctionRows(Table table, OperDto oper) {
        TableRow startRow = findRowByBookmark(table, "oper_" + oper.id() + "_col_0");
        if (startRow == null) {
            log.warn("Start row not found for operation {}", oper.id());
            return;
        }
        int start = rowIndexOf(table, startRow);
        if (start < 0) {
            log.warn("Row index not found for operation {}", oper.id());
            return;
        }

        int blockEnd = findOperationBlockEnd(table, start);
        int targetRows = Math.max(oper.funcs().size(), 1);
        int currentRows = blockEnd - start + 1;

        log.info("Rebuilding function rows for operation {}: start={}, blockEnd={}, targetRows={}, currentRows={}",
                oper.id(), start, blockEnd, targetRows, currentRows);

        while (currentRows < targetRows) {
            TableRow template = table.getRows().get(blockEnd);
            TableRow newRow = (TableRow) template.deepClone();
            clearRowTexts(newRow);
            table.getRows().insert(blockEnd + 1, newRow);
            blockEnd++;
            currentRows++;
            log.info("Added row for operation {}, currentRows={}", oper.id(), currentRows);
        }

        while (currentRows > targetRows) {
            table.getRows().remove(table.getRows().get(blockEnd));
            blockEnd--;
            currentRows--;
            log.info("Removed row for operation {}, currentRows={}", oper.id(), currentRows);
        }

        for (int i = 0; i < oper.funcs().size(); i++) {
            FuncDto func = oper.funcs().get(i);
            TableRow row = table.getRows().get(start + i);
            setShadedBookmarkedText(row.getCells().get(4), "func_" + func.id() + "_col_4",
                    Boolean.TRUE.equals(func.isProd()) ? func.name() : "");
            setShadedBookmarkedText(row.getCells().get(5), "func_" + func.id() + "_col_5",
                    Boolean.TRUE.equals(func.isProd()) ? "" : func.name());
            setShadedBookmarkedText(row.getCells().get(6), "func_" + func.id() + "_col_6", func.specCharakt());
            setShadedBookmarkedText(row.getCells().get(7), "func_" + func.id() + "_col_7", func.param());
            log.info("Set function bookmarks for func {} at row {}", func.id(), start + i);
        }
    }

    private int rowIndexOf(Table table, TableRow row) {
        for (int i = 0; i < table.getRows().getCount(); i++) {
            if (table.getRows().get(i) == row) {
                return i;
            }
        }
        return -1;
    }

    private int findOperationBlockEnd(Table table, int startIndex) {
        for (int i = startIndex + 1; i < table.getRows().getCount(); i++) {
            if (containsOperStartBookmark(table.getRows().get(i), "oper_")) {
                return i - 1;
            }
        }
        return table.getRows().getCount() - 1;
    }

    private void clearRowTexts(TableRow row) {
        for (int j = 0; j < row.getCells().getCount(); j++) {
            TableCell cell = row.getCells().get(j);
            for (int k = 0; k < cell.getParagraphs().getCount(); k++) {
                cell.getParagraphs().get(k).setText("");
            }
        }
    }
}
