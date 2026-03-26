package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.FuncService;
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
    private final FuncService funcService;

    public PuDocEditor(
            @Value("${out.pu.path}") String src,
            @Value("${copy.out.pu.path}") String dest,
            DocpackageService docpackageService,
            FuncService funcService
    ) {
        super(src, dest);
        this.docpackageService = docpackageService;
        this.funcService = funcService;
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto) throws Exception {
        var original = docpackageService.getRepository().findById(dto.id()).orElseThrow();
        Document doc = loadCopy(dto.path(), original.getPuName(), dto.puName());
        Table table = doc.getSections().get(0).getTables().get(0);

        Set<Long> processedFuncIds = new HashSet<>();

        for (OperDto oper : dto.opers()) {
            updateCell(table, "oper_" + oper.id() + "_col_0", oper.numOper());
            updateCell(table, "oper_" + oper.id() + "_col_1", oper.name());
            updateCell(table, "oper_" + oper.id() + "_oborud", oper.oborud());
            updateCell(table, "oper_" + oper.id() + "_ostnasInstr", oper.ostnasInstr());

            for (FuncDto func : oper.funcs()) {
                processedFuncIds.add(func.id());
                boolean exists = findCellByBookmark(table, "func_" + func.id() + "_col_6") != null;

                if (exists) {
                    boolean originalIsProd = func.id() != null
                            ? funcService.getRepository().findById(func.id())
                                    .map(f -> f.getIsProd()).orElse(func.isProd())
                            : func.isProd();
                    if (originalIsProd != func.isProd()) {
                        clearBookmarkText(table, "func_" + func.id() + "_col_4");
                        clearBookmarkText(table, "func_" + func.id() + "_col_5");
                    }
                    updateCell(table, "func_" + func.id() + "_col_4", func.isProd() ? func.name() : "");
                    updateCell(table, "func_" + func.id() + "_col_5", func.isProd() ? "" : func.name());
                    updateCell(table, "func_" + func.id() + "_col_6", func.specCharakt());
                    updateCell(table, "func_" + func.id() + "_col_7", func.param());
                } else {
                    TableRow newRow = insertRowAfterLastBookmark(table, "func_" + func.id(), COLUMN_COUNT);
                    setShadedText(newRow.getCells().get(func.isProd() ? 4 : 5), func.name());
                    setShadedText(newRow.getCells().get(6), func.specCharakt());
                    setShadedText(newRow.getCells().get(7), func.param());
                }
            }
        }

        removeDeletedRows(table, processedFuncIds, "func_", "_col_6");

        String article = dto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(dto.sborEds());
        String origArticle = original.getSborEds().getFirst().getNazv() + " "
                + original.getSborEds().getFirst().getOboznach() + "\u2014"
                + original.getSborEds().getLast().getOboznach();
        updateHeader(doc,
                Map.of("d", origArticle, "n", original.getPuName(), "p", original.getPackageName()),
                Map.of("d", article, "n", dto.puName(), "p", dto.packageName())
        );

        save(doc, dto.path(), dto.puName());
    }
}
