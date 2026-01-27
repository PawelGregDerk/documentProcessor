package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
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
            @Value("${tmp.out.kp.path}") String tmp,
            @Value("${out.kp.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().getFirst();
            for (var oper : dto.opers()) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                var funcs = oper.funcs();
                row.getCell(0).setText(oper.numOper());
                fillOpInfo(row.getCell(7), oper);
                fillFuncSpecChars(row.getCell(8), funcs);
                fillFuncNameParam(row.getCell(9), funcs, true);
                fillFuncNameParam(row.getCell(10), funcs, false);
            }

            postProcess(doc, dto.kpName(), Map.of(
                    "d", dto.vedIName(),
                    "n", dto.kpName(),
                    "p", dto.packageName()
            ));
        }
    }

    private void fillOpInfo(XWPFTableCell cell, OperDto op) {
        var p = cell.getParagraphs().getFirst();
        var r = p.createRun();
        r.setText(op.name());
        r.addBreak();
        r.setText("Цех " + op.numZech());
        r.addBreak();
        r.setText(op.oborud());
    }

    private void fillFuncSpecChars(XWPFTableCell cell, List<FuncDto> funcs) {
        var p = cell.getParagraphs().getFirst();
        funcs.stream()
                .map(FuncDto::specCharakt)
                .filter(StringUtils::isNotBlank)
                .forEach(s -> {
                    var r = p.createRun();
                    r.setText(s);
                    r.addBreak();
                });
    }

    private void fillFuncNameParam(XWPFTableCell cell, List<FuncDto> funcs, boolean isProd) {
        var p = cell.getParagraphs().getFirst();
        funcs.stream()
                .filter(f -> f.isProd() == isProd)
                .forEach(f -> {
                    var r = p.createRun();
                    r.setText(f.name());
                    r.addBreak();
                    r.setText(f.param());
                    r.addBreak();
                });
    }
}
