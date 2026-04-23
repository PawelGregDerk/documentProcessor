package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KpWordGenerator extends AbstractWordGenerator {
    private static final int COLUMN_COUNT = 11;

    protected KpWordGenerator(
            @Value("${inp.kp.path}") Resource inp,
            @Value("${out.kp.path}") String out
    ) {
        super(inp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().getFirst();
            for (var oper : dto.opers()) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                var funcs = oper.funcs();
                addBookmark(row.getCell(0), "oper_" + oper.id() + "_col_0", oper.numOper());
                addBookmark(row.getCell(7), "oper_" + oper.id() + "_name", oper.name());
                appendBookmark(row.getCell(7), "oper_" + oper.id() + "_numZech", oper.numZech());
                appendBookmark(row.getCell(7), "oper_" + oper.id() + "_oborud", oper.oborud());
                addSpecBookmarks(row.getCell(8), funcs, oper.id());
                boolean firstProd = true, firstProc = true;
                for (FuncDto func : funcs) {
                    if (func.isProd()) {
                        if (firstProd) { addBookmark(row.getCell(9), "func_" + func.id() + "_name", func.name()); firstProd = false; }
                        else appendBookmark(row.getCell(9), "func_" + func.id() + "_name", func.name());
                        appendBookmark(row.getCell(9), "func_" + func.id() + "_param", func.param());
                    } else {
                        if (firstProc) { addBookmark(row.getCell(10), "func_" + func.id() + "_name", func.name()); firstProc = false; }
                        else appendBookmark(row.getCell(10), "func_" + func.id() + "_name", func.name());
                        appendBookmark(row.getCell(10), "func_" + func.id() + "_param", func.param());
                    }
                }
            }

            String article = dto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(dto.sborEds());
            postProcess(doc, dto.path(), dto.kpName(), Map.of(
                    "d", article,
                    "n", dto.kpName(),
                    "p", dto.packageName()
            ));
        }
    }

    private void addSpecBookmarks(org.apache.poi.xwpf.usermodel.XWPFTableCell cell, List<FuncDto> funcs, Long operId) {
        boolean first = true;
        for (FuncDto func : funcs) {
            if (StringUtils.isBlank(func.specCharakt())) {
                continue;
            }
            if (first) {
                addBookmark(cell, "func_" + func.id() + "_col_8", func.specCharakt());
                first = false;
            } else {
                appendBookmark(cell, "func_" + func.id() + "_col_8", func.specCharakt());
            }
        }
        if (first) {
            addBookmark(cell, "oper_" + operId + "_col_8", "");
        }
    }
}
