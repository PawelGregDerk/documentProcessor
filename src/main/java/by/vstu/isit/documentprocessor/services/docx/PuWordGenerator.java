//first
package by.vstu.isit.documentprocessor.services.docx;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.text.MessageFormat.format;
import static org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.CONTINUE;
import static org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.RESTART;

@Service
public class PuWordGenerator {
    private static final int COLUMN_COUNT = 14;
    private static final int DATA_START_ROW = 2;
    @Value("${inp.pu.path}")
    private Resource inpPuPath;
    @Value("${tmp.out.pu.path}")
    private String tmpOutPuPath;
    @Value("${out.pu.path}")
    private String outPuPath;

    public void generate(DockPackageDto dto) throws Exception {
        try (var inp = inpPuPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            fillHeaderFromSecondPage(doc, dto.puName());
            var table = doc.getTables().getFirst();
            for (var oper : dto.opers()) {
                int startRow = Math.max(table.getNumberOfRows(), DATA_START_ROW);
                // ===== если функций нет — одна строка =====
                if (oper.funcs().isEmpty()) {
                    var row = table.createRow();
                    ensureCells(row);
                    row.getCell(0).setText(oper.numOper());
                    row.getCell(1).setText(oper.name());
                    fillOperCell(row.getCell(2), oper);
                    mergeHorizontal(row, 2);
                    mergeHorizontal(row, 9);
                    continue;
                }

                // ===== если функции есть =====
                for (var func : oper.funcs()) {
                    var row = table.createRow();
                    ensureCells(row);
                    row.getCell(0).setText(oper.numOper());
                    row.getCell(1).setText(oper.name());
                    fillOperCell(row.getCell(2), oper);
                    mergeHorizontal(row, 2);
                    mergeHorizontal(row, 9);
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

            try (var out = new FileOutputStream(tmpOutPuPath)) {
                doc.write(out);
            }
        }

        try (var in = new FileInputStream(tmpOutPuPath);
             var template = XWPFTemplate.compile(in).render(Map.of(
                     "d", dto.getFirst().extra(),
                     "n", dto.puName()
             ));
             var out = new FileOutputStream(format(outPuPath, dto.puName()))) {
            template.write(out);
        }
        Files.deleteIfExists(Path.of(tmpOutPuPath));
    }

    private void fillOperCell(XWPFTableCell cell, OperDto oper) {
        cell.removeParagraph(0);
        var p = cell.addParagraph();
        var r = p.createRun();
        r.setText(oper.oborud() + " " + oper.ostnasInstr());
    }

    private void mergeHorizontal(XWPFTableRow row, int col) {
        var cell = row.getCell(col);
        var tcPr = cell.getCTTc().isSetTcPr()
                ? cell.getCTTc().getTcPr()
                : cell.getCTTc().addNewTcPr();
        if (tcPr.isSetGridSpan()) {
            tcPr.getGridSpan().setVal(BigInteger.valueOf(2));
        } else {
            tcPr.addNewGridSpan().setVal(BigInteger.valueOf(2));
        }
    }

    private void mergeVertical(XWPFTable table, int start, int end, int col) {
        for (int r = start; r <= end; r++) {
            var cell = table.getRow(r).getCell(col);
            var tcPr = cell.getCTTc().isSetTcPr()
                    ? cell.getCTTc().getTcPr()
                    : cell.getCTTc().addNewTcPr();
            var merge = tcPr.isSetVMerge() ? tcPr.getVMerge() : tcPr.addNewVMerge();
            merge.setVal(r == start ? RESTART : CONTINUE);
        }
    }

    private void ensureCells(XWPFTableRow row) {
        while (row.getTableCells().size() < COLUMN_COUNT) {
            row.createCell();
        }
    }

    private void fillHeaderFromSecondPage(XWPFDocument doc, String value) {
        if (doc.getHeaderList().size() > 1) {
            var header = doc.getHeaderList().get(1);
            for (var p : header.getParagraphs()) {
                p.getRuns().forEach(r -> {
                    if (r.text() != null && r.text().contains("${PU}")) {
                        r.setText(value, 0);
                    }
                });
            }
        }
    }
}
