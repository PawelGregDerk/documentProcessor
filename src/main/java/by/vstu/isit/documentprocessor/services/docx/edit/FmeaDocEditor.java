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
public class FmeaDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 25;
    private final DocpackageService docpackageService;

    public FmeaDocEditor(
            @Value("${out.fmea.path}") String src,
            @Value("${copy.out.fmea.path}") String dest,
            DocpackageService docpackageService
    ) {
        super(src, dest);
        this.docpackageService = docpackageService;
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto) throws Exception {
        var original = docpackageService.getRepository().findById(dto.id()).orElseThrow();
        Document doc = loadCopy(dto.path(), original.getFmeaName(), dto.fmeaName());
        Table table = doc.getSections().get(0).getTables().get(0);

        Set<Long> processedFuncIds = new HashSet<>();

        for (OperDto oper : dto.opers()) {
            updateCell(table, "oper_" + oper.id() + "_col_2", dto.vedIName());
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
                    TableRow newRow = insertRowAfterLastBookmark(table, "func_" + func.id(), COLUMN_COUNT);
                    setShadedText(newRow.getCells().get(7), func.name());
                    setShadedText(newRow.getCells().get(17), func.specCharakt());
                }
            }
        }

        removeDeletedRows(table, processedFuncIds, "func_", "_col_7");

        String article = dto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(dto.sborEds());
        String origArticle = original.getSborEds().getFirst().getNazv() + " "
                + original.getSborEds().getFirst().getOboznach() + "\u2014"
                + original.getSborEds().getLast().getOboznach();
        updateHeader(doc,
                Map.of("d", origArticle, "n", original.getFmeaName(), "p", original.getPackageName()),
                Map.of("d", article, "n", dto.fmeaName(), "p", dto.packageName())
        );

        save(doc, dto.path(), dto.fmeaName());
    }
}
