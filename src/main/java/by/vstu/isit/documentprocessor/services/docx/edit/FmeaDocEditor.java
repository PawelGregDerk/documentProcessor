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
public class FmeaDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 25;

    public FmeaDocEditor(
            @Value("${out.fmea.path}") String src
    ) {
        super(src, src);
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        Document doc = loadCopyByBookmark(dto.path(), dto.fmeaName(), savedDto.fmeaName(), 0, sourceBookmark(dto));
        Table table = doc.getSections().get(0).getTables().get(0);

        Set<Long> processedFuncIds = new HashSet<>();

        for (int oi = 0; oi < savedDto.opers().size(); oi++) {
            OperDto oper = savedDto.opers().get(oi);
            OperDto origOper = oi < dto.opers().size() ? dto.opers().get(oi) : oper;
         //   updateCell(table, "oper_" + oper.id() + "_col_2", savedDto.vedIName());
            updateCell(table, "oper_" + oper.id() + "_numOper", oper.numOper());
            updateCell(table, "oper_" + oper.id() + "_name", oper.name());
            updateCell(table, "oper_" + oper.id() + "_numZech", oper.numZech());

            for (FuncDto func : oper.funcs()) {
                processedFuncIds.add(func.id());
                boolean exists = findCellByBookmark(table, "func_" + func.id() + "_col_7") != null;

                if (exists) {
                    updateCell(table, "func_" + func.id() + "_col_7", func.name());
                    updateCell(table, "func_" + func.id() + "_col_17", func.specCharakt());
                } else {
                    TableRow newRow = insertRowAfterLastBookmark(table, "oper_" + oper.id(), COLUMN_COUNT);
                    setShadedText(newRow.getCells().get(7), func.name());
                    setShadedText(newRow.getCells().get(17), func.specCharakt());
                }
            }
        }

        removeDeletedRows(table, processedFuncIds, "func_", "_col_7");

        String article = savedDto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(savedDto.sborEds());
        String origArticle = dto.sborEds().getFirst().nazv() + " "
                + dto.sborEds().getFirst().oboznach() + "\u2014"
                + dto.sborEds().getLast().oboznach();
        Map<String, String> oldHeader = resolveHeaderOldData(
                Map.of("d", origArticle, "n", dto.fmeaName(), "p", dto.packageName())
        );
        updateHeader(doc, oldHeader, Map.of("d", article, "n", savedDto.fmeaName(), "p", savedDto.packageName()));

        save(doc, savedDto.path(), savedDto.fmeaName());
    }

    private String sourceBookmark(DockPackageDto dto) {
        if (dto.opers().isEmpty() || dto.opers().getFirst().funcs().isEmpty()) {
            return null;
        }
        var func = dto.opers().getFirst().funcs().getFirst();
        return func.id() == null ? null : "func_" + func.id() + "_col_17";
    }
}
