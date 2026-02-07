package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.VerticalMerger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RiWordGenerator extends AbstractWordGenerator implements VerticalMerger {
    private static final int COLUMN_COUNT = 4;
    private static final int DATA_START_ROW = 1;

    public RiWordGenerator(
            @Value("${inp.ri.path}") Resource inp,
            @Value("${tmp.out.ri.path}") String tmp,
            @Value("${out.ri.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        for (var oper : dto.opers()) {
            try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
                var table = doc.getTables().get(2);

                for (var fun : oper.funcs()) {
                    var row = createRow(table, fun);
                    row.getCell(2).setText(fun.specCharakt());
                }

                int endRow = table.getNumberOfRows() - 1;
                mergeVertical(table, DATA_START_ROW, endRow, 3);

                String article = designationsAssemblyUnit(dto.sborEds());
                postProcess(doc, oper.name(), Map.of(
                        "d", article,
                        "d1", dto.sborEds().getFirst().nazv(),
                        "p", dto.packageName(),
                        "shop", oper.shifr(),
                        "namOp", oper.name(),
                        "numOp", oper.numOper(),
                        "oObr", oper.oborud(),
                        "oOst", oper.ostnasInstr()
                ));
            }
        }
    }

    private XWPFTableRow createRow(XWPFTable table, FuncDto fun) {
        var row = table.createRow();
        ensureCells(row, COLUMN_COUNT);
        fillFuncCell(row.getCell(1), fun);
        return row;
    }

    private void fillFuncCell(XWPFTableCell cell, FuncDto fun) {
        var p = cell.getParagraphs().getFirst();
        var r1 = p.createRun();
        r1.setText(fun.name());
        r1.addBreak();
        var r2 = p.createRun();
        r2.setText(fun.param());
        r2.addBreak();
    }
}
