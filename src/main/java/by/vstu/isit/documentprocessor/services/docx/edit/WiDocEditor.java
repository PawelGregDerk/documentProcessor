package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
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
public class WiDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 5;
    private final DocpackageService docpackageService;

    public WiDocEditor(
            @Value("${out.wi.path}") String src,
            @Value("${copy.out.wi.path}") String dest,
            DocpackageService docpackageService
    ) {
        super(src, dest);
        this.docpackageService = docpackageService;
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto) throws Exception {
        var original = docpackageService.getRepository().findById(dto.id()).orElseThrow();
        Document doc = loadCopy(dto.path(), original.getVedIName(), dto.vedIName());
        Table table = doc.getSections().get(0).getTables().get(1);

        Set<Long> processedOperIds = new HashSet<>();

        for (OperDto oper : dto.opers()) {
            processedOperIds.add(oper.id());
            boolean exists = findCellByBookmark(table, "oper_" + oper.id() + "_col_0") != null;

            if (exists) {
                updateCell(table, "oper_" + oper.id() + "_col_0", oper.numOper());
                updateCell(table, "oper_" + oper.id() + "_shifr", oper.shifr());
                updateCell(table, "oper_" + oper.id() + "_name", oper.name());
                updateCell(table, "oper_" + oper.id() + "_numZech", oper.numZech());
                updateCell(table, "oper_" + oper.id() + "_nomInstr", oper.nomInstr());
            } else {
                TableRow newRow = addRowWithShading(table, COLUMN_COUNT);
                setShadedText(newRow.getCells().get(0), oper.numOper());
                setShadedText(newRow.getCells().get(1), oper.shifr() + " " + oper.name());
                setShadedText(newRow.getCells().get(2), oper.numZech() + "-" + oper.nomInstr());
            }
        }

        removeDeletedRows(table, processedOperIds, "oper_", "_col_0");

        String origDesig = original.getSborEds().getFirst().getOboznach() + "\u2014"
                + original.getSborEds().getLast().getOboznach();
        updateHeader(doc,
                Map.of("d", origDesig, "d1", original.getSborEds().getFirst().getNazv(), "p", original.getPackageName()),
                Map.of("d", designationsAssemblyUnit(dto.sborEds()), "d1", dto.sborEds().getFirst().nazv(), "p", dto.packageName())
        );

        save(doc, dto.path(), dto.vedIName());
    }
}
