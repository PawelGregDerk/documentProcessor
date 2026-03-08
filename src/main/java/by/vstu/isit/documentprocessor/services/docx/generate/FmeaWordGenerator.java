package by.vstu.isit.documentprocessor.services.docx.generate;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.excepts.NoFunctionException;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxGenerator;
import by.vstu.isit.documentprocessor.services.docx.common.merge.VerticalMerger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Генератор FMEA: заполняет таблицу, выполняет вертикальные объединения,
 * затем создает закладки в итоговом документе.
 */
@Service
public class FmeaWordGenerator extends AbstractDocxGenerator implements VerticalMerger {
    /**
     * Количество колонок в таблице FMEA.
     */
    private static final int COLUMN_COUNT = 25;
    /**
     * Индекс первой строки данных в таблице FMEA.
     */
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
        path = dto.path();
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().getFirst();
            for (var oper : dto.opers()) {
                int startRow = Math.max(table.getNumberOfRows(), DATA_START_ROW);
                if (oper.funcs().isEmpty()) {
                    throw new NoFunctionException(oper.numOper(), oper.name());
                }

                for (var func : oper.funcs()) {
                    var row = createRow(table, oper, dto.vedIName());
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
                    "d", article(dto),
                    "n", dto.fmeaName(),
                    "p", dto.packageName()
            ));
        }

        var out = resolveOutPath(dto.fmeaName());
        try (var writer = new Docx4jBookmarkWriter(inpPath, out)) {
            writer.setHeaderCellSegments(0, 1, 1, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Изделие: ", false),
                    Docx4jBookmarkWriter.CellSegment.bookmark("p", dto.packageName(), false, false)
            ));
            writer.setHeaderCellSegments(0, 1, 3, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark("d", article(dto), false, false)
            ));
            writer.setHeaderCellSegments(0, 1, 4, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Номер FMEA: ", false),
                    Docx4jBookmarkWriter.CellSegment.bookmark("n", dto.fmeaName(), false, false)
            ));

            var table = writer.getTable(0);
            int totalRows = dto.opers().stream().mapToInt(o -> o.funcs().size()).sum();
            int rowIndex = table.getRows().getCount() - totalRows;
            for (var oper : dto.opers()) {
                for (int funcIndex = 0; funcIndex < oper.funcs().size(); funcIndex++) {
                    var func = oper.funcs().get(funcIndex);
                    var row = table.getRows().get(rowIndex);

                    if (funcIndex == 0) {
                        writer.setCellSegments(row.getCells().get(2), List.of(
                                Docx4jBookmarkWriter.CellSegment.bookmark("fmea_r" + rowIndex + "_vedI", dto.vedIName(), false, false)
                        ));

                        List<Docx4jBookmarkWriter.CellSegment> opSegments = new ArrayList<>();
                        opSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                "fmea_r" + rowIndex + "_numOper", oper.numOper(), true, true));
                        opSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                "fmea_r" + rowIndex + "_operName", oper.name(), false, true));
                        opSegments.add(Docx4jBookmarkWriter.CellSegment.text("Цех ", false));
                        opSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                "fmea_r" + rowIndex + "_numZech", oper.numZech(), false, false));
                        writer.setCellSegments(row.getCells().get(3), opSegments);
                    } else {
                        writer.setCellSegments(row.getCells().get(2), List.of());
                        writer.setCellSegments(row.getCells().get(3), List.of());
                    }

                    writer.setCellSegments(row.getCells().get(7), List.of(
                            Docx4jBookmarkWriter.CellSegment.bookmark(
                                    "fmea_r" + rowIndex + "_funcName", func.name(), false, false)
                    ));
                    writer.setCellSegments(row.getCells().get(17), List.of(
                            Docx4jBookmarkWriter.CellSegment.bookmark(
                                    "fmea_r" + rowIndex + "_funcSpec", func.specCharakt(), false, false)
                    ));

                    rowIndex++;
                }
            }
            writer.save();
        }
    }

    @Override
    public boolean hasGeneratedFiles(DockPackageDto dto) {
        path = dto.path();
        return java.nio.file.Files.exists(resolveOutPath(dto.fmeaName()));
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

