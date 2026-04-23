package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import com.spire.doc.Table;
import com.spire.doc.*;
import com.spire.doc.documents.Paragraph;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class KpDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 11;

    public KpDocEditor(
            @Value("${out.kp.path}") String src,
            @Value("${copy.out.kp.path}") String dest
    ) {
        super(src, dest);
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        Document doc = loadCopyByBookmark(dto.path(), dto.kpName(), savedDto.kpName(), 0, sourceBookmark(dto));
        Table table = doc.getSections().get(0).getTables().get(0);

        Set<Long> processedOperIds = new HashSet<>();

        for (int oi = 0; oi < savedDto.opers().size(); oi++) {
            OperDto oper = savedDto.opers().get(oi);
            processedOperIds.add(oper.id());
            boolean exists = findCellByBookmark(table, "oper_" + oper.id() + "_col_0") != null;

            TableRow row;
            if (exists) {
                row = findRowByBookmark(table, "oper_" + oper.id() + "_col_0");
                updateCell(table, "oper_" + oper.id() + "_col_0", oper.numOper());
                updateCell(table, "oper_" + oper.id() + "_name", oper.name());
                updateCell(table, "oper_" + oper.id() + "_numZech", oper.numZech());
                updateCell(table, "oper_" + oper.id() + "_oborud", oper.oborud());
            } else {
                row = addRowWithShading(table, COLUMN_COUNT);
                setShadedText(row.getCells().get(0), oper.numOper());
                setShadedText(row.getCells().get(7), oper.name() + "\n" + oper.numZech() + "\n" + oper.oborud());
            }
            rebuildSpecCell(row, oper);
            rebuildFunctionGroups(row, oper);
        }

        removeDeletedRows(table, processedOperIds, "oper_", "_col_0");

        String article = savedDto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(savedDto.sborEds());
        String origArticle = dto.sborEds().getFirst().nazv() + " "
                + dto.sborEds().getFirst().oboznach() + "\u2014"
                + dto.sborEds().getLast().oboznach();
        Map<String, String> oldHeader = resolveHeaderOldData(
                Map.of("d", origArticle, "n", dto.kpName(), "p", dto.packageName())
        );
        updateHeader(doc, oldHeader, Map.of("d", article, "n", savedDto.kpName(), "p", savedDto.packageName()));

        save(doc, savedDto.path(), savedDto.kpName());
    }

    private String sourceBookmark(DockPackageDto dto) {
        if (dto.opers().isEmpty() || dto.opers().getFirst().funcs().isEmpty()) {
            return null;
        }
        var func = dto.opers().getFirst().funcs().getFirst();
        return func.id() == null ? null : "func_" + func.id() + "_name";
    }

    private void rebuildSpecCell(TableRow row, OperDto oper) {
        TableCell cell = row.getCells().get(8);
        clearCell(cell);
        Paragraph para = cell.getParagraphs().getCount() > 0 ? cell.getParagraphs().get(0) : cell.addParagraph();

        boolean first = true;
        for (FuncDto func : oper.funcs()) {
            if (StringUtils.isBlank(func.specCharakt())) {
                continue;
            }
            if (!first) {
                para.appendText("\n");
            }
            appendShadedBookmark(para, "func_" + func.id() + "_col_8", func.specCharakt());
            first = false;
        }
    }

    private void rebuildFunctionGroups(TableRow row, OperDto oper) {
        TableCell prodCell = row.getCells().get(9);
        TableCell procCell = row.getCells().get(10);
        clearCell(prodCell);
        clearCell(procCell);
        Paragraph prodPara = prodCell.getParagraphs().getCount() > 0 ? prodCell.getParagraphs().get(0) : prodCell.addParagraph();
        Paragraph procPara = procCell.getParagraphs().getCount() > 0 ? procCell.getParagraphs().get(0) : procCell.addParagraph();

        boolean firstProd = true;
        boolean firstProc = true;
        for (FuncDto func : oper.funcs()) {
            if (Boolean.TRUE.equals(func.isProd())) {
                if (!firstProd) {
                    prodPara.appendText("\n");
                }
                appendShadedBookmark(prodPara, "func_" + func.id() + "_name", func.name());
                prodPara.appendText("\n");
                appendShadedBookmark(prodPara, "func_" + func.id() + "_param", func.param());
                firstProd = false;
            } else {
                if (!firstProc) {
                    procPara.appendText("\n");
                }
                appendShadedBookmark(procPara, "func_" + func.id() + "_name", func.name());
                procPara.appendText("\n");
                appendShadedBookmark(procPara, "func_" + func.id() + "_param", func.param());
                firstProc = false;
            }
        }
    }
}
