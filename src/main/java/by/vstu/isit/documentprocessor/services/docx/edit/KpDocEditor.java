package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.FuncService;
import com.spire.doc.Table;
import com.spire.doc.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KpDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 11;
    private final DocpackageService docpackageService;
    private final FuncService funcService;

    public KpDocEditor(
            @Value("${out.kp.path}") String src,
            @Value("${copy.out.kp.path}") String dest,
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
        Document doc = loadCopy(dto.path(), original.getKpName(), dto.kpName());
        Table table = doc.getSections().get(0).getTables().get(0);

        Set<Long> processedOperIds = new HashSet<>();

        for (OperDto oper : dto.opers()) {
            processedOperIds.add(oper.id());
            boolean exists = findCellByBookmark(table, "oper_" + oper.id() + "_col_0") != null;

            if (exists) {
                updateCell(table, "oper_" + oper.id() + "_col_0", oper.numOper());
                updateCell(table, "oper_" + oper.id() + "_name", oper.name());
                updateCell(table, "oper_" + oper.id() + "_numZech", oper.numZech());
                updateCell(table, "oper_" + oper.id() + "_oborud", oper.oborud());
                updateCell(table, "oper_" + oper.id() + "_col_8", buildSpecChars(oper.funcs()));
                for (FuncDto func : oper.funcs()) {
                    boolean originalIsProd = func.id() != null
                            ? funcService.getRepository().findById(func.id())
                                    .map(f -> f.getIsProd()).orElse(func.isProd())
                            : func.isProd();
                    if (originalIsProd != func.isProd()) {
                        clearBookmarkText(table, "func_" + func.id() + "_name");
                        clearBookmarkText(table, "func_" + func.id() + "_param");
                        TableRow row = findRowByBookmark(table, "func_" + func.id() + "_name");
                        if (row != null) {
                            setShadedText(row.getCells().get(func.isProd() ? 9 : 10), func.name() + "\n" + func.param());
                            setShadedText(row.getCells().get(func.isProd() ? 10 : 9), "");
                        }
                    } else {
                        updateCell(table, "func_" + func.id() + "_name", func.name());
                        updateCell(table, "func_" + func.id() + "_param", func.param());
                    }
                }
            } else {
                TableRow newRow = addRowWithShading(table, COLUMN_COUNT);
                setShadedText(newRow.getCells().get(0), oper.numOper());
                setShadedText(newRow.getCells().get(7), oper.name() + "\n" + oper.numZech() + "\n" + oper.oborud());
                setShadedText(newRow.getCells().get(8), buildSpecChars(oper.funcs()));
                for (FuncDto func : oper.funcs()) {
                    if (func.isProd()) {
                        setShadedText(newRow.getCells().get(9), func.name() + "\n" + func.param() + "\n");
                    } else {
                        setShadedText(newRow.getCells().get(10), func.name() + "\n" + func.param() + "\n");
                    }
                }
            }
        }

        removeDeletedRows(table, processedOperIds, "oper_", "_col_0");

        String article = dto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(dto.sborEds());
        String origArticle = original.getSborEds().getFirst().getNazv() + " "
                + original.getSborEds().getFirst().getOboznach() + "\u2014"
                + original.getSborEds().getLast().getOboznach();
        updateHeader(doc,
                Map.of("d", origArticle, "n", original.getKpName(), "p", original.getPackageName()),
                Map.of("d", article, "n", dto.kpName(), "p", dto.packageName())
        );

        save(doc, dto.path(), dto.kpName());
    }

    private String buildSpecChars(List<FuncDto> funcs) {
        return funcs.stream()
                .map(FuncDto::specCharakt)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }
}
