package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import com.spire.doc.Table;
import com.spire.doc.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.text.MessageFormat.format;

@Service
public class RiDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 4;

    public RiDocEditor(
            @Value("${out.ri.path}") String src,
            @Value("${copy.out.ri.path}") String dest
    ) {
        super(src, dest);
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        for (int oi = 0; oi < savedDto.opers().size(); oi++) {
            OperDto oper = savedDto.opers().get(oi);
            OperDto origOper = oi < dto.opers().size() ? dto.opers().get(oi) : oper;

            Document doc = loadCopySubByBookmark(
                    dto.path(),
                    "ri",
                    origOper.name(),
                    oper.name(),
                    2,
                    sourceBookmark(origOper)
            );
            Table table = doc.getSections().get(0).getTables().get(2);

            Set<Long> processedFuncIds = new HashSet<>();

            for (FuncDto func : oper.funcs()) {
                processedFuncIds.add(func.id());
                boolean exists = findCellByBookmark(table, "func_" + func.id() + "_name") != null;

                if (exists) {
                    updateCell(table, "func_" + func.id() + "_name", func.name());
                    updateCell(table, "func_" + func.id() + "_param", func.param());
                    updateCell(table, "func_" + func.id() + "_col_2", func.specCharakt());
                } else {
                    TableRow newRow = insertRowAfterLastBookmark(table, "func_", COLUMN_COUNT);
                    setShadedText(newRow.getCells().get(1), func.name() + "\n" + func.param());
                    setShadedText(newRow.getCells().get(2), func.specCharakt());
                }
            }

            removeDeletedRows(table, processedFuncIds, "func_", "_name");

            String origDesig = dto.sborEds().getFirst().oboznach() + "\u2014" + dto.sborEds().getLast().oboznach();
            Map<String, String> oldHeader = resolveHeaderOldData(Map.of(
                    "d", origDesig,
                    "d1", dto.sborEds().getFirst().nazv(),
                    "p", dto.packageName(),
                    "shop", origOper.shifr(),
                    "namOp", origOper.name(),
                    "numOp", origOper.numOper(),
                    "oObr", origOper.oborud(),
                    "oOst", origOper.ostnasInstr()
            ));
            updateHeader(doc, oldHeader, Map.of(
                    "d", designationsAssemblyUnit(savedDto.sborEds()),
                    "d1", savedDto.sborEds().getFirst().nazv(),
                    "p", savedDto.packageName(),
                    "shop", oper.shifr(),
                    "namOp", oper.name(),
                    "numOp", oper.numOper(),
                    "oObr", oper.oborud(),
                    "oOst", oper.ostnasInstr()
            ));

            saveSub(doc, savedDto.path(), "ri", oper.name());

            if (!origOper.name().equals(oper.name())) {
                Path oldFile = Path.of(format(destPath, savedDto.path(), "ri", origOper.name()));
                Files.deleteIfExists(oldFile);
            }
        }
    }

    private String sourceBookmark(OperDto oper) {
        if (oper.funcs() == null || oper.funcs().isEmpty()) {
            return null;
        }
        var func = oper.funcs().getFirst();
        return func.id() == null ? null : "func_" + func.id() + "_name";
    }
}
