package by.vstu.isit.documentprocessor.services.docx.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.excepts.NoFunctionException;
import by.vstu.isit.documentprocessor.services.docx.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.abstracts.VerticalMerger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FmeaWordGenerator extends AbstractWordGenerator implements VerticalMerger {
    private static final int COLUMN_COUNT = 25;
    private static final int DATA_START_ROW = 2;

    public FmeaWordGenerator(
            @Value("${inp.fmea.path}") Resource inp,
            @Value("${tmp.out.fmea.path}") String tmp,
            @Value("${out.fmea.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().getFirst();
            for (var oper : dto.opers()) {
                int startRow = Math.max(table.getNumberOfRows(), DATA_START_ROW);
                if (oper.funcs().isEmpty()) {
                    throw new NoFunctionException(oper.numOper(), oper.name());
                }

                for (var func : oper.funcs()) {
                    var row = createRow(table, oper, dto.numInstr());
                    row.getCell(7).setText(func.name());
                    row.getCell(17).setText(func.specCharakt());
                }

                int endRow = table.getNumberOfRows() - 1;
                mergeVertical(table, startRow, endRow, 0);
                mergeVertical(table, startRow, endRow, 1);
                mergeVertical(table, startRow, endRow, 2);
                mergeVertical(table, startRow, endRow, 3);
                mergeVertical(table, startRow, endRow, 4);
                mergeVertical(table, startRow, endRow, 5);
                mergeVertical(table, startRow, endRow, 6);
            }

            postProcess(doc, dto.fmeaName(), Map.of(
                    "d", dto.numInstr(),
                    "n", dto.fmeaName(),
                    "p", dto.packageName()
            ));
        }
    }

    private XWPFTableRow createRow(XWPFTable table, OperDto oper, String content2) {
        var row = table.createRow();
        ensureCells(row, COLUMN_COUNT);
        row.getCell(2).setText(content2);
        fillOperCell(row.getCell(3), oper);
        return row;
    }

    private void fillOperCell(XWPFTableCell cell, OperDto oper) {
        var p = cell.getParagraphs().getFirst();
        var r1 = p.createRun();
        r1.setBold(true);
        r1.setText(oper.numOper());
        r1.addBreak();
        var r2 = p.createRun();
        r2.setText(oper.name());
        r2.addBreak();
        r2.setText("Цех " + oper.numZech());
    }
}
