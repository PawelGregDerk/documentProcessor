package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import com.spire.doc.*;
import com.spire.doc.documents.Paragraph;
import com.spire.doc.fields.TextRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class FmeaDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 25;

    public FmeaDocEditor(@Value("${out.fmea.path}") String src) {
        super(src, src);
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        Document doc = loadCopyByBookmark(dto.path(), dto.fmeaName(), savedDto.fmeaName(), 0, sourceBookmark(dto));
        Table table = doc.getSections().get(0).getTables().get(0);

        Set<Long> processedFuncIds = new HashSet<>();
        Set<Long> processedOperIds = new HashSet<>();

        for (int oi = 0; oi < savedDto.opers().size(); oi++) {
            OperDto oper = savedDto.opers().get(oi);
            OperDto origOper = oi < dto.opers().size() ? dto.opers().get(oi) : oper;

            boolean operExists = findCellByBookmark(table, "oper_" + oper.id() + "_numOper") != null;
            processedOperIds.add(oper.id());

            int firstFuncRowIdx = -1;

            for (int fi = 0; fi < oper.funcs().size(); fi++) {
                FuncDto func = oper.funcs().get(fi);
                processedFuncIds.add(func.id());

                boolean exists = findCellByBookmark(table, "func_" + func.id() + "_col_7") != null;

                if (exists) {
                    updateCell(table, "func_" + func.id() + "_col_7", func.name());
                    updateCell(table, "func_" + func.id() + "_col_17", func.specCharakt());

                    TableCell specCell = findCellByBookmark(table, "func_" + func.id() + "_col_17");
                    applySmallFont(specCell);

                    if (fi == 0) {
                        TableRow funcRow = findRowByBookmark(table, "func_" + func.id() + "_col_7");
                        firstFuncRowIdx = rowIndexOf(table, funcRow);
                    }
                } else {
                    TableRow newRow = insertRowAfterLastBookmark(table, "oper_" + oper.id(), COLUMN_COUNT);

                    if (fi == 0) {
                        firstFuncRowIdx = rowIndexOf(table, newRow);

                        if (!operExists) {
                            setShadedBookmarkedText(newRow.getCells().get(3), "oper_" + oper.id() + "_numOper", oper.numOper());
                            newRow.getCells().get(3).getParagraphs().get(0).appendText(" ");
                            appendShadedBookmark(newRow.getCells().get(3).getParagraphs().get(0), "oper_" + oper.id() + "_name", oper.name());
                            newRow.getCells().get(3).getParagraphs().get(0).appendText(" ");
                            appendShadedBookmark(newRow.getCells().get(3).getParagraphs().get(0), "oper_" + oper.id() + "_numZech", oper.numZech());
                        }
                    }

                    setShadedText(newRow.getCells().get(7), func.name());
                    setShadedText(newRow.getCells().get(17), func.specCharakt());

                    applySmallFont(newRow.getCells().get(17));
                }
            }

            if (firstFuncRowIdx >= 0 && oper.funcs().size() > 1) {
                int endRow = firstFuncRowIdx + oper.funcs().size() - 1;
                for (int col = 0; col <= 6; col++) {
                    table.applyVerticalMerge(col, firstFuncRowIdx, endRow);
                }
            }
        }

        removeDeletedRows(table, processedFuncIds, "func_", "_col_7");

        String article = savedDto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(savedDto.sborEds());
        String origArticle = dto.sborEds().getFirst().nazv() + " " + dto.sborEds().getFirst().oboznach();

        Map<String, String> oldHeader = resolveHeaderOldData(
                Map.of("d", origArticle, "n", dto.fmeaName(), "p", dto.packageName())
        );

        updateHeader(doc, oldHeader, Map.of(
                "d", article,
                "n", savedDto.fmeaName(),
                "p", savedDto.packageName()
        ));

        save(doc, savedDto.path(), savedDto.fmeaName());
    }

    private void applySmallFont(TableCell cell) {
        if (cell == null) return;

        // ParagraphCollection не Iterable → идём по индексам
        for (int i = 0; i < cell.getParagraphs().getCount(); i++) {
            Paragraph p = cell.getParagraphs().get(i);

            // DocumentObjectCollection тоже не Iterable
            for (int j = 0; j < p.getChildObjects().getCount(); j++) {
                DocumentObject obj = p.getChildObjects().get(j);
                if (obj instanceof TextRange) {
                    TextRange tr = (TextRange) obj;
                    tr.getCharacterFormat().setFontSize(8);
                }
            }
        }
    }

    private String sourceBookmark(DockPackageDto dto) {
        if (dto.opers().isEmpty() || dto.opers().getFirst().funcs().isEmpty()) {
            return null;
        }
        var func = dto.opers().getFirst().funcs().getFirst();
        return func.id() == null ? null : "func_" + func.id() + "_col_17";
    }

    private int rowIndexOf(Table table, TableRow row) {
        for (int i = 0; i < table.getRows().getCount(); i++) {
            if (table.getRows().get(i) == row) {
                return i;
            }
        }
        return -1;
    }
}
