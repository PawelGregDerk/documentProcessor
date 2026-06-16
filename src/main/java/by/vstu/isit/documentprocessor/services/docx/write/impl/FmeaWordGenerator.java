package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.excepts.NoFunctionException;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.HorizontMerger;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.VerticalMerger;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class FmeaWordGenerator extends AbstractWordGenerator implements VerticalMerger {
    private static final int COLUMN_COUNT = 25;
    private static final int DATA_START_ROW = 2;

    public FmeaWordGenerator(
            @Value("${inp.fmea.path}") Resource inp,
            @Value("${out.fmea.path}") String out
    ) {
        super(inp, out);
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

                List<FuncDto> funcs = oper.funcs();
                for (int fi = 0; fi < funcs.size(); fi++) {
                    FuncDto func = funcs.get(fi);
                    var row = createRow(table, oper, dto.vedIName(), fi == 0);
                    addBookmark(row.getCell(7), "func_" + func.id() + "_col_7", func.name());
                    if (isNotBlank(func.specCharakt())) {
                        mergeHorizontalAndRemove(row, 17, 2);
                        addBookmark(row.getCell(17), "func_" + func.id() + "_col_17", func.specCharakt());
                    }
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

            String article = dto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(dto.sborEds());
            postProcess(doc, dto.path(), dto.fmeaName(), Map.of(
                    "d", article,
                    "n", dto.fmeaName(),
                    "p", dto.packageName()
            ));
        }
    }

    private XWPFTableRow createRow(XWPFTable table, OperDto oper, String vedIName, boolean first) {
        var row = table.createRow();
        ensureCells(row, COLUMN_COUNT);
        if (first) {
        //    addBookmark(row.getCell(2), "oper_" + oper.id() + "_col_2", vedIName);
            appendBookmark(row.getCell(3), "oper_" + oper.id() + "_numOper", oper.numOper());
            appendBookmark(row.getCell(3), "oper_" + oper.id() + "_name", oper.name());
            appendBookmark(row.getCell(3), "oper_" + oper.id() + "_numZech", oper.numZech());
        }
        return row;
    }

    private void mergeHorizontalAndRemove(XWPFTableRow row, int col, int span) {
        var cell = row.getCell(col);
        if (cell == null) return;

        var tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        if (tcPr.isSetGridSpan()) {
            tcPr.getGridSpan().setVal(BigInteger.valueOf(span));
        } else {
            tcPr.addNewGridSpan().setVal(BigInteger.valueOf(span));
        }

        // Удаляем последующие <w:tc> из CTRow (в обратном порядке)
        var ctRow = row.getCtRow();
        for (int i = col + span - 1; i > col; i--) {
            if (i < ctRow.sizeOfTcArray()) {
                ctRow.removeTc(i);
            }
        }
    }
}
