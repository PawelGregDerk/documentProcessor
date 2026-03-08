package by.vstu.isit.documentprocessor.services.docx.generate;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
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

@Service
public class RiWordGenerator extends AbstractDocxGenerator implements VerticalMerger {
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
        path = dto.path();
        for (var oper : dto.opers()) {
            generateForOper(dto, oper);
        }
    }

    public void generateForOper(DockPackageDto dto, OperDto oper) throws Exception {
        generateForOper(dto, oper, dto.path());
    }

    public void generateForOper(DockPackageDto dto, OperDto oper, String basePath) throws Exception {
        path = basePath;
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().get(2);

            for (var fun : oper.funcs()) {
                var row = createRow(table, fun);
                row.getCell(2).setText(fun.specCharakt());
            }

            int endRow = table.getNumberOfRows() - 1;
            mergeVertical(table, DATA_START_ROW, endRow, 3);

            postProcess(doc, oper.name(), Map.of(
                    "d", designationsAssemblyUnit(dto.sborEds()),
                    "d1", dto.sborEds().getFirst().nazv(),
                    "p", dto.packageName(),
                    "shop", oper.shifr(),
                    "namOp", oper.name(),
                    "numOp", oper.numOper(),
                    "oObr", oper.oborud(),
                    "oOst", oper.ostnasInstr()
            ));
        }

        var out = resolveOutPath(basePath, oper.name());
        try (var writer = new Docx4jBookmarkWriter(inpPath, out)) {
            writer.setHeaderCellSegments(0, 1, 2, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "d", designationsAssemblyUnit(dto.sborEds()), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 1, 3, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "d1", dto.sborEds().getFirst().nazv(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 2, 1, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Наименование операции: ", false),
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "shop", oper.shifr(), false, false),
                    Docx4jBookmarkWriter.CellSegment.text(" ", false),
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "namOp", oper.name(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 2, 2, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Номер операции: ", false),
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "numOp", oper.numOper(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 2, 3, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Изделие: ", false),
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "p", dto.packageName(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 7, 0, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "oObr", oper.oborud(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 7, 1, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "oOst", oper.ostnasInstr(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);

            var table = writer.getTable(2);
            int totalRows = oper.funcs().size();
            int rowIndex = table.getRows().getCount() - totalRows;
            for (int i = 0; i < oper.funcs().size(); i++) {
                var func = oper.funcs().get(i);
                var row = table.getRows().get(rowIndex);

                List<Docx4jBookmarkWriter.CellSegment> funcSegments = new ArrayList<>();
                funcSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                        "ri_r" + rowIndex + "_funcName", func.name(), false, true));
                funcSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                        "ri_r" + rowIndex + "_funcParam", func.param(), false, false));
                writer.setCellSegments(row.getCells().get(1), funcSegments);

                writer.setCellSegments(row.getCells().get(2), List.of(
                        Docx4jBookmarkWriter.CellSegment.bookmark(
                                "ri_r" + rowIndex + "_funcSpec", func.specCharakt(), false, false)
                ));

                rowIndex++;
            }
            writer.save();
        }
    }

    @Override
    public boolean hasGeneratedFiles(DockPackageDto dto) {
        path = dto.path();
        return dto.opers().stream()
                .map(oper -> resolveOutPath(oper.name()))
                .allMatch(java.nio.file.Files::exists);
    }

    public boolean hasGeneratedFile(DockPackageDto dto, OperDto oper) {
        path = dto.path();
        return java.nio.file.Files.exists(resolveOutPath(oper.name()));
    }

    public boolean existsAt(String basePath, String operName) {
        return java.nio.file.Files.exists(resolveOutPath(basePath, operName));
    }

    public java.nio.file.Path outputDir(String basePath) {
        return resolveOutPath(basePath, "_").getParent();
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

