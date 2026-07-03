package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
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
public class WiDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 5;

    public WiDocEditor(
            @Value("${out.wi.path}") String src
    ) {
        super(src, src);
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        Document doc = loadCopyByBookmark(dto.path(), dto.vedIName(), savedDto.vedIName(), 1, sourceBookmark(dto));
        Table table = doc.getSections().get(0).getTables().get(1);

        Set<Long> processedOperIds = new HashSet<>();

        for (OperDto oper : savedDto.opers()) {
            processedOperIds.add(oper.id());
            boolean exists = findCellByBookmark(table, "oper_" + oper.id() + "_col_0") != null;

            if (exists) {
                updateCell(table, "oper_" + oper.id() + "_col_0", oper.numOper());
                updateCell(table, "oper_" + oper.id() + "_shifr", oper.shifr());
                updateCell(table, "oper_" + oper.id() + "_name", oper.name());
                updateCell(table, "oper_" + oper.id() + "_nomInstr", oper.nomInstr());
            } else {
                TableRow newRow = addRowWithShading(table, COLUMN_COUNT);
                setShadedText(newRow.getCells().get(0), oper.numOper());
                setShadedText(newRow.getCells().get(1), oper.shifr() + " " + oper.name());
                setShadedText(newRow.getCells().get(2), oper.nomInstr());
            }
        }

        removeDeletedRows(table, processedOperIds, "oper_", "_col_0");

        String origDesig = dto.sborEds().getFirst().oboznach();
        Map<String, String> oldHeader = resolveHeaderOldData(
                Map.of("d", origDesig, "d1", dseText(dto.sborEds(), dto.sborEds().getFirst().nazv()), "p", dseText(dto.sborEds(), dto.packageName()))
        );
        updateHeader(doc, oldHeader, Map.of(
                "d", designationsAssemblyUnit(savedDto.sborEds()),
                "d1", dseText(savedDto.sborEds(), savedDto.sborEds().getFirst().nazv()),
                "p", dseText(savedDto.sborEds(), savedDto.packageName())
        ));

        save(doc, savedDto.path(), savedDto.vedIName());
    }

    private String sourceBookmark(DockPackageDto dto) {
        if (dto.opers().isEmpty()) {
            return null;
        }
        var oper = dto.opers().getFirst();
        return oper.id() == null ? null : "oper_" + oper.id() + "_shifr";
    }
}
