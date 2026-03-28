package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
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
    private final DocpackageService docpackageService;

    public PuDocEditor(
            @Value("${out.pu.path}") String src,
            @Value("${copy.out.pu.path}") String dest,
            DocpackageService docpackageService
    ) {
        super(src, dest);
        this.docpackageService = docpackageService;
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        Document doc = loadCopy(dto.path(), dto.puName(), savedDto.puName());
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
                    setShadedText(newRow.getCells().get(func.isProd() ? 4 : 5), func.name());
                    setShadedText(newRow.getCells().get(6), func.specCharakt());
                    setShadedText(newRow.getCells().get(7), func.param());
                }
            }
        }

        removeDeletedRows(table, processedFuncIds, "func_", "_col_6");

        String article = savedDto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(savedDto.sborEds());
        String origArticle = dto.sborEds().getFirst().nazv() + " "
                + dto.sborEds().getFirst().oboznach() + "\u2014"
                + dto.sborEds().getLast().oboznach();
        updateHeader(doc,
                Map.of("d", origArticle, "n", dto.puName(), "p", dto.packageName()),
                Map.of("d", article, "n", savedDto.puName(), "p", savedDto.packageName())
        );

        save(doc, savedDto.path(), savedDto.puName());
    }
}
