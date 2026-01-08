package by.vstu.isit.documentprocessor.services.docx;

import by.vstu.isit.documentprocessor.dto.*;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import com.deepoove.poi.XWPFTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.text.MessageFormat.format;

@Service
public class FmeaWordGenerator {
    private static final int COLUMN_COUNT = 25;
    private static final int DATA_START_ROW = 2;
    @Value("${inp.fmea.path}")
    private String inpFmeaPath;
    @Value("${tmp.out.fmea.path}")
    private String tmpOutFmeaPath;
    @Value("${out.fmea.path}")
    private String outFmeaPath;

    public void generate(DockPackageDto dto) throws Exception {

        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(inpFmeaPath))) {

            fillHeaderFromSecondPage(doc, dto.fmeaName());

            XWPFTable table = doc.getTables().getFirst();

            for (OperDto oper : dto.opers()) {

                int startRow = Math.max(table.getNumberOfRows(), DATA_START_ROW);

                // ===== если функций нет — одна строка =====
                if (oper.funcs().isEmpty()) {
                    XWPFTableRow row = table.createRow();
                    ensureCells(row);
                    fillExtraCell(row.getCell(2), oper.extra());
                    fillOperCell(row.getCell(3), oper);
                    continue;
                }

                // ===== если функции есть =====
                for (FuncDto func : oper.funcs()) {
                    XWPFTableRow row = table.createRow();
                    ensureCells(row);

                    fillOperCell(row.getCell(3), oper);
                    row.getCell(7).setText(func.name());
                    row.getCell(17).setText(func.specCharakt());
                }

                int endRow = table.getNumberOfRows() - 1;
                fillExtraCell(
                        table.getRow(startRow).getCell(2),
                        oper.extra()
                );
                // ⚠ первые 2 столбца НЕ объединяем
                mergeVertical(table, startRow, endRow, 2);
                mergeVertical(table, startRow, endRow, 3);
                mergeVertical(table, startRow, endRow, 4);
                mergeVertical(table, startRow, endRow, 5);
                mergeVertical(table, startRow, endRow, 6);
            }

            try (FileOutputStream out = new FileOutputStream(tmpOutFmeaPath)) {
                doc.write(out);
            }

            try (FileInputStream in = new FileInputStream(tmpOutFmeaPath);
                 XWPFTemplate template = XWPFTemplate.compile(in)
                         .render(Map.of(
                                 "d", dto.getFirst().extra(),
                                 "n", dto.fmeaName()
                         ));
                 FileOutputStream out = new FileOutputStream(format(outFmeaPath, dto.fmeaName()))) {
                template.write(out);
            }
            Files.deleteIfExists(Path.of(tmpOutFmeaPath));
        }
    }

    private void fillOperCell(XWPFTableCell cell, OperDto oper) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();

        XWPFRun r1 = p.createRun();
        r1.setBold(true);
        r1.setText(oper.numOper());

        XWPFRun r2 = p.createRun();
        r2.setText(" " + oper.name() + " Цех " + oper.numZech());
    }

    private void mergeVertical(XWPFTable table, int start, int end, int col) {
        for (int r = start; r <= end; r++) {
            XWPFTableCell cell = table.getRow(r).getCell(col);
            CTTcPr tcPr = cell.getCTTc().isSetTcPr()
                    ? cell.getCTTc().getTcPr()
                    : cell.getCTTc().addNewTcPr();

            CTVMerge merge = tcPr.isSetVMerge()
                    ? tcPr.getVMerge()
                    : tcPr.addNewVMerge();

            merge.setVal(r == start ? STMerge.RESTART : STMerge.CONTINUE);
        }
    }

    private void fillExtraCell(XWPFTableCell cell, String value) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        XWPFRun r = p.createRun();
        r.setText(value);
    }

    private void ensureCells(XWPFTableRow row) {
        while (row.getTableCells().size() < COLUMN_COUNT) {
            row.createCell();
        }
    }

    private void fillHeaderFromSecondPage(XWPFDocument doc, String value) {
        if (doc.getHeaderList().size() > 1) {
            XWPFHeader header = doc.getHeaderList().get(1);
            for (XWPFParagraph p : header.getParagraphs()) {
                p.getRuns().forEach(r -> {
                    if (r.text() != null && r.text().contains("${FMEA}")) {
                        r.setText(value, 0);
                    }
                });
            }
        }
    }
}
