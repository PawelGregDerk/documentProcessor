package by.vstu.isit.documentprocessor.services.docx;

import by.vstu.isit.documentprocessor.dto.*;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FmeaWordGenerator {

    private static final int COLUMN_COUNT = 25;
    private static final int DATA_START_ROW = 2;

    public void generate(FmeaDto dto, String templatePath, String outPath) throws Exception {

        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(templatePath))) {

            fillHeaderFromSecondPage(doc, dto.fmeaName());

            XWPFTable table = doc.getTables().get(0);

            for (FmeaOperDto oper : dto.operations()) {

                int startRow = Math.max(table.getNumberOfRows(), DATA_START_ROW);

                // ===== если функций нет — одна строка =====
                if (oper.functions().isEmpty()) {
                    XWPFTableRow row = table.createRow();
                    ensureCells(row);
                    fillOperCell(row.getCell(3), oper);
                    continue;
                }

                // ===== если функции есть =====
                for (FmeaFuncDto func : oper.functions()) {
                    XWPFTableRow row = table.createRow();
                    ensureCells(row);

                    fillOperCell(row.getCell(3), oper);
                    row.getCell(7).setText(func.name());
                    row.getCell(17).setText(func.spec());
                }

                int endRow = table.getNumberOfRows() - 1;

                // ⚠ первые 2 столбца НЕ объединяем
                mergeVertical(table, startRow, endRow, 3);
                mergeVertical(table, startRow, endRow, 4);
                mergeVertical(table, startRow, endRow, 5);
                mergeVertical(table, startRow, endRow, 6);
            }

            try (FileOutputStream out = new FileOutputStream(outPath)) {
                doc.write(out);
            }
        }
    }

    /* =========================================================
       Заполнение 4-й ячейки (операция)
       ========================================================= */
    private void fillOperCell(XWPFTableCell cell, FmeaOperDto oper) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();

        XWPFRun r1 = p.createRun();
        r1.setBold(true);
        r1.setText(oper.numOper());

        XWPFRun r2 = p.createRun();
        r2.setText(" " + oper.name() + " Цех " + oper.zech());
    }

    /* =========================================================
       Вертикальное объединение (без текста)
       ========================================================= */
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

    /* =========================================================
       Гарантия 25 ячеек
       ========================================================= */
    private void ensureCells(XWPFTableRow row) {
        while (row.getTableCells().size() < COLUMN_COUNT) {
            row.createCell();
        }
    }

    /* =========================================================
       Заполнение колонтитула со 2-й страницы
       ========================================================= */
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
