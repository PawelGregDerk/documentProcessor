package by.vstu.isit.documentprocessor.services.docx.generate;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.excepts.NoFunctionException;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxGenerator;
import by.vstu.isit.documentprocessor.services.docx.common.merge.HorizontMerger;
import by.vstu.isit.documentprocessor.services.docx.common.merge.VerticalMerger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PU generator: fills process table and merges cells,
 * then adds bookmarks for later updates.
 */
@Service
public class PuWordGenerator extends AbstractDocxGenerator implements HorizontMerger, VerticalMerger {
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
        path = dto.path();
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().getFirst();
            for (var oper : dto.opers()) {
                int startRow = Math.max(table.getNumberOfRows(), DATA_START_ROW);
                if (oper.funcs().isEmpty()) {
                    throw new NoFunctionException(oper.numOper(), oper.name());
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

            postProcess(doc, dto.puName(), Map.of(
                    "d", article(dto),
                    "n", dto.puName(),
                    "p", dto.packageName()
            ));
        }

        var out = resolveOutPath(dto.puName());
        try (var writer = new Docx4jBookmarkWriter(inpPath, out)) {
            writer.setHeaderCellSegments(0, 3, 1, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark("d", article(dto), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 3, 2, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark("p", dto.packageName(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 3, 3, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("ПУ", false),
                    Docx4jBookmarkWriter.CellSegment.bookmark("n", dto.puName(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);

            var table = writer.getTable(0);
            int totalRows = dto.opers().stream().mapToInt(o -> o.funcs().size()).sum();
            int rowIndex = table.getRows().getCount() - totalRows;

            for (var oper : dto.opers()) {
                for (int funcIndex = 0; funcIndex < oper.funcs().size(); funcIndex++) {
                    var func = oper.funcs().get(funcIndex);
                    var row = table.getRows().get(rowIndex);

                    if (funcIndex == 0) {
                        writer.setCellSegments(row.getCells().get(0), List.of(
                                Docx4jBookmarkWriter.CellSegment.bookmark(
                                        "pu_r" + rowIndex + "_numOper", oper.numOper(), false, false)
                        ));
                        writer.setCellSegments(row.getCells().get(1), List.of(
                                Docx4jBookmarkWriter.CellSegment.bookmark(
                                        "pu_r" + rowIndex + "_operName", oper.name(), false, false)
                        ));

                        List<Docx4jBookmarkWriter.CellSegment> equipSegments = new ArrayList<>();
                        equipSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                "pu_r" + rowIndex + "_oborud", oper.oborud(), false, false));
                        equipSegments.add(Docx4jBookmarkWriter.CellSegment.text(" ", false));
                        equipSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                "pu_r" + rowIndex + "_ostnas", oper.ostnasInstr(), false, false));
                        writer.setCellSegments(row.getCells().get(2), equipSegments);
                    } else {
                        writer.setCellSegments(row.getCells().get(0), List.of());
                        writer.setCellSegments(row.getCells().get(1), List.of());
                        writer.setCellSegments(row.getCells().get(2), List.of());
                    }

                    if (func.isProd()) {
                        writer.setCellSegments(row.getCells().get(4), List.of(
                                Docx4jBookmarkWriter.CellSegment.bookmark(
                                        "pu_r" + rowIndex + "_funcName_prod", func.name(), false, false)
                        ));
                        writer.setCellSegments(row.getCells().get(5), List.of());
                    } else {
                        writer.setCellSegments(row.getCells().get(4), List.of());
                        writer.setCellSegments(row.getCells().get(5), List.of(
                                Docx4jBookmarkWriter.CellSegment.bookmark(
                                        "pu_r" + rowIndex + "_funcName_proc", func.name(), false, false)
                        ));
                    }

                    writer.setCellSegments(row.getCells().get(6), List.of(
                            Docx4jBookmarkWriter.CellSegment.bookmark(
                                    "pu_r" + rowIndex + "_funcSpec", func.specCharakt(), false, false)
                    ));
                    writer.setCellSegments(row.getCells().get(7), List.of(
                            Docx4jBookmarkWriter.CellSegment.bookmark(
                                    "pu_r" + rowIndex + "_funcParam", func.param(), false, false)
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
        return java.nio.file.Files.exists(resolveOutPath(dto.puName()));
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

