package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.OperService;
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
    private final OperService operService;

    public RiDocEditor(
            @Value("${out.ri.path}") String src,
            @Value("${copy.out.ri.path}") String dest,
            OperService operService
    ) {
        super(src, dest);
        this.operService = operService;
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto) throws Exception {
        for (OperDto oper : dto.opers()) {
            var originalOper = operService.getRepository().findById(oper.id()).orElseThrow();
            String originalName = originalOper.getName();

            Document doc = loadCopySub(dto.path(), "ri", originalName, oper.name());
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

            updateHeader(doc,
                    Map.of("d", originalOper.getDocpackage().getSborEds().getFirst().getOboznach() + "\u2014"
                                    + originalOper.getDocpackage().getSborEds().getLast().getOboznach(),
                            "d1", originalOper.getDocpackage().getSborEds().getFirst().getNazv(),
                            "p", originalOper.getDocpackage().getPackageName(),
                            "shop", originalOper.getShifr(),
                            "namOp", originalOper.getName(),
                            "numOp", originalOper.getNumOper(),
                            "oObr", originalOper.getOborud(),
                            "oOst", originalOper.getOstnasInstr()),
                    Map.of("d", designationsAssemblyUnit(dto.sborEds()),
                            "d1", dto.sborEds().getFirst().nazv(),
                            "p", dto.packageName(),
                            "shop", oper.shifr(),
                            "namOp", oper.name(),
                            "numOp", oper.numOper(),
                            "oObr", oper.oborud(),
                            "oOst", oper.ostnasInstr())
            );

            saveSub(doc, dto.path(), "ri", oper.name());

            if (!originalName.equals(oper.name())) {
                Path oldFile = Path.of(format(destPath, dto.path(), "ri", originalName));
                Files.deleteIfExists(oldFile);
            }
        }
    }
}
