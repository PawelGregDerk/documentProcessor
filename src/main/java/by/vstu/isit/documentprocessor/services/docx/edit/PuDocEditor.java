package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import com.spire.doc.Table;
import com.spire.doc.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class PuDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 14;

    public PuDocEditor(
            @Value("${out.pu.path}") String src,
            @Value("${copy.out.pu.path}") String dest
    ) {
        super(src, dest);
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        Document doc = loadCopyByBookmark(dto.path(), dto.puName(), savedDto.puName(), 0, sourceBookmark(dto));
        Table table = doc.getSections().get(0).getTables().get(0);

        Set<Long> processedFuncIds = new HashSet<>();

        for (int oi = 0; oi < savedDto.opers().size(); oi++) {
            OperDto oper = savedDto.opers().get(oi);
            OperDto origOper = oi < dto.opers().size() ? dto.opers().get(oi) : null;
            updateCell(table, "oper_" + oper.id() + "_col_0", oper.numOper());
            updateCell(table, "oper_" + oper.id() + "_col_1", oper.name());
            updateCell(table, "oper_" + oper.id() + "_oborud", oper.oborud());
            updateCell(table, "oper_" + oper.id() + "_ostnasInstr", oper.ostnasInstr());

            for (FuncDto func : oper.funcs()) {
                processedFuncIds.add(func.id());
                boolean exists = findCellByBookmark(table, "func_" + func.id() + "_col_6") != null;

                if (exists) {
                    boolean originalIsProd = func.isProd();
                    if (origOper != null) {
                        originalIsProd = origOper.funcs().stream()
                                .filter(f -> f.id() != null && f.id().equals(func.id()))
                                .map(FuncDto::isProd)
                                .findFirst().orElse(func.isProd());
                    }
                    if (originalIsProd != func.isProd()) {
                        clearBookmarkText(table, "func_" + func.id() + "_col_4");
                        clearBookmarkText(table, "func_" + func.id() + "_col_5");
                    }
                    updateCell(table, "func_" + func.id() + "_col_4", func.isProd() ? func.name() : "");
                    updateCell(table, "func_" + func.id() + "_col_5", func.isProd() ? "" : func.name());
                    updateCell(table, "func_" + func.id() + "_col_6", func.specCharakt());
                    updateCell(table, "func_" + func.id() + "_col_7", func.param());
                } else {
                    TableRow newRow = insertRowAfterLastBookmark(table, "oper_" + oper.id(), COLUMN_COUNT);
                    setShadedBookmarkedText(newRow.getCells().get(func.isProd() ? 4 : 5),
                            "func_" + func.id() + "_col_" + (func.isProd() ? "4" : "5"),
                            func.name());
                    setShadedBookmarkedText(newRow.getCells().get(6), "func_" + func.id() + "_col_6", func.specCharakt());
                    setShadedBookmarkedText(newRow.getCells().get(7), "func_" + func.id() + "_col_7", func.param());
                }
            }
        }

        removeDeletedRows(table, processedFuncIds, "func_", "_col_6");
        rebuildOperMerges(table, savedDto);

        String article = savedDto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(savedDto.sborEds());
        String origArticle = dto.sborEds().getFirst().nazv() + " "
                + dto.sborEds().getFirst().oboznach() + "\u2014"
                + dto.sborEds().getLast().oboznach();
        Map<String, String> oldHeader = resolveHeaderOldData(
                Map.of("d", origArticle, "n", dto.puName(), "p", dto.packageName())
        );
        updateHeader(doc, oldHeader, Map.of("d", article, "n", savedDto.puName(), "p", savedDto.packageName()));

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
}
