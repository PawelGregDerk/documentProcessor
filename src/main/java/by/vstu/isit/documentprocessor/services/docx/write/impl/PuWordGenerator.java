package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.excepts.NoFunctionException;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.HorizontMerger;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.VerticalMerger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PuWordGenerator extends AbstractWordGenerator implements HorizontMerger, VerticalMerger {
    private static final int COLUMN_COUNT = 14;
    private static final int DATA_START_ROW = 2;

    public PuWordGenerator(
            @Value("${inp.pu.path}") Resource inp,
            @Value("${out.pu.path}") String out
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
                    var row = createRow(table, oper, fi == 0);
                    addBookmark(row.getCell(4), "func_" + func.id() + "_col_4", func.isProd() ? func.name() : "");
                    addBookmark(row.getCell(5), "func_" + func.id() + "_col_5", func.isProd() ? "" : func.name());
                    addBookmark(row.getCell(6), "func_" + func.id() + "_col_6", func.specCharakt());
                    addBookmark(row.getCell(7), "func_" + func.id() + "_col_7", func.param());
                }

                int endRow = table.getNumberOfRows() - 1;
                mergeVertical(table, startRow, endRow, 0);
                mergeVertical(table, startRow, endRow, 1);
                mergeVertical(table, startRow, endRow, 2);
                mergeVertical(table, startRow, endRow, 12);
                mergeVertical(table, startRow, endRow, 13);
            }

            String article = dto.sborEds().getFirst().nazv() + " " + designationsAssemblyUnit(dto.sborEds());
            postProcess(doc, dto.path(), dto.puName(), Map.of(
                    "d", article,
                    "n", dto.puName(),
                    "p", dseText(dto.sborEds(), dto.packageName())
            ));
        }
    }

    private XWPFTableRow createRow(XWPFTable table, OperDto oper, boolean first) {
        var row = table.createRow();
        ensureCells(row, COLUMN_COUNT);
        if (first) {
            addBookmark(row.getCell(0), "oper_" + oper.id() + "_col_0", oper.numOper());
            addBookmark(row.getCell(1), "oper_" + oper.id() + "_col_1", oper.name());
            appendBookmark(row.getCell(1), "oper_" + oper.id() + "_numZech", oper.numZech());
            addBookmark(row.getCell(2), "oper_" + oper.id() + "_oborud", oper.oborud());
            appendBookmark(row.getCell(2), "oper_" + oper.id() + "_ostnasInstr", oper.ostnasInstr());
        }
        mergeHorizontal(row, 2, 2);
        mergeHorizontal(row, 9, 2);
        return row;
    }

    private void addFuncBookmarks(XWPFTableRow row, Long operId, Long funcId) {
        // ячейки 4 и 5 уже заполнены до вызова — перезаписываем через addBookmark
    }
}
