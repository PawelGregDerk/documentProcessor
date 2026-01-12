package by.vstu.isit.documentprocessor.services.docx;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;

@Service
public class PuWordGenerator extends AbstractWordGenerator {
    private static final int COLUMN_COUNT = 14;
    private static final int DATA_START_ROW = 2;

    public PuWordGenerator(
            @Value("${inp.pu.path}") Resource inp,
            @Value("${tmp.out.pu.path}") String tmp,
            @Value("${out.pu.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            fillHeaderFromSecondPage(doc, "${PU}", dto.puName());
            var table = doc.getTables().getFirst();
            for (var oper : dto.opers()) {
                int startRow = Math.max(table.getNumberOfRows(), DATA_START_ROW);
                if (oper.funcs().isEmpty()) {
                    createRow(table, oper);
                    continue;
                }

                for (var func : oper.funcs()) {
                    var row = createRow(table, oper);
                    if (func.isProd()) {
                        row.getCell(4).setText(func.name());
                    } else {
                        row.getCell(5).setText(func.name());
                    }

                    row.getCell(6).setText(func.specCharakt());
                    row.getCell(7).setText(func.param());
                }

                int endRow = table.getNumberOfRows() - 1;
                mergeVertical(table, startRow, endRow, 0);
                mergeVertical(table, startRow, endRow, 1);
                mergeVertical(table, startRow, endRow, 2);
                mergeVertical(table, startRow, endRow, 12);
                mergeVertical(table, startRow, endRow, 13);
            }

            try (var out = new FileOutputStream(tmpOut)) {
                doc.write(out);
            }
        }

        postProcess(dto, dto.puName());
    }

    private XWPFTableRow createRow(XWPFTable table, OperDto oper) {
        var row = table.createRow();
        ensureCells(row, COLUMN_COUNT);
        row.getCell(0).setText(oper.numOper());
        row.getCell(1).setText(oper.name());
        row.getCell(2).setText(oper.oborud() + " " + oper.ostnasInstr());
        mergeHorizontal(row, 2, 2);
        mergeHorizontal(row, 9, 2);
        return row;
    }
}
