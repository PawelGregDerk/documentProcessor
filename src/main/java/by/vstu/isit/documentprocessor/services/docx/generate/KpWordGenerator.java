package by.vstu.isit.documentprocessor.services.docx.generate;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.DocxBookmarkSegments;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Генератор КП: заполняет таблицу карты потока процесса и проставляет закладки.
 */
@Service
public class KpWordGenerator extends AbstractDocxGenerator {
    /**
     * Количество колонок таблицы КП.
     */
    private static final int COLUMN_COUNT = 11;

    public KpWordGenerator(
            @Value("${inp.kp.path}") Resource inp,
            @Value("${tmp.out.kp.path}") String tmp,
            @Value("${out.kp.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        path = dto.path();
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().getFirst();
            for (var oper : dto.opers()) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                var funcs = oper.funcs();
                row.getCell(0).setText(oper.numOper());
                fillOpInfo(row.getCell(7), oper);
                fillFuncSpecChars(row.getCell(8), funcs);
                fillFuncNameParam(row.getCell(9), funcs, true);
                fillFuncNameParam(row.getCell(10), funcs, false);
            }

            postProcess(doc, dto.kpName(), Map.of(
                    "d", article(dto),
                    "n", dto.kpName(),
                    "p", dto.packageName()
            ));
        }

        var out = resolveOutPath(dto.kpName());
        try (var writer = new Docx4jBookmarkWriter(inpPath, out)) {
            writer.setHeaderCellSegments(0, 3, 0, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Карта потока процесса КП", false),
                    Docx4jBookmarkWriter.CellSegment.bookmark("n", dto.kpName(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 3, 1, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark("p", dto.packageName(), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);
            writer.setHeaderCellSegments(0, 3, 2, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark("d", article(dto), false, false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.FIRST_ONLY);

            var table = writer.getTable(0);
            int totalRows = dto.opers().size();
            int rowIndex = table.getRows().getCount() - totalRows;
            for (var oper : dto.opers()) {
                var row = table.getRows().get(rowIndex);
                var funcs = oper.funcs();

                writer.setCellSegments(row.getCells().get(0), List.of(
                        Docx4jBookmarkWriter.CellSegment.bookmark(
                                "kp_r" + rowIndex + "_numOper", oper.numOper(), false, false)
                ));

                List<Docx4jBookmarkWriter.CellSegment> opSegments = new ArrayList<>();
                opSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                        "kp_r" + rowIndex + "_operName", oper.name(), false, true));
                opSegments.add(Docx4jBookmarkWriter.CellSegment.text("Цех ", false));
                opSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                        "kp_r" + rowIndex + "_numZech", oper.numZech(), false, true));
                opSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                        "kp_r" + rowIndex + "_oborud", oper.oborud(), false, false));
                writer.setCellSegments(row.getCells().get(7), opSegments);

                List<Docx4jBookmarkWriter.CellSegment> specSegments = new ArrayList<>();
                for (int i = 0; i < funcs.size(); i++) {
                    var func = funcs.get(i);
                    specSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                            "kp_r" + rowIndex + "_func" + i + "_spec", func.specCharakt(), false, true));
                }
                DocxBookmarkSegments.removeTrailingBreak(specSegments);
                writer.setCellSegments(row.getCells().get(8), specSegments);

                List<Docx4jBookmarkWriter.CellSegment> prodSegments = new ArrayList<>();
                List<Docx4jBookmarkWriter.CellSegment> procSegments = new ArrayList<>();
                for (int i = 0; i < funcs.size(); i++) {
                    var func = funcs.get(i);
                    if (func.isProd()) {
                        prodSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                "kp_r" + rowIndex + "_func" + i + "_name_prod", func.name(), false, true));
                        prodSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                "kp_r" + rowIndex + "_func" + i + "_param_prod", func.param(), false, true));
                    } else {
                        procSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                "kp_r" + rowIndex + "_func" + i + "_name_proc", func.name(), false, true));
                        procSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                "kp_r" + rowIndex + "_func" + i + "_param_proc", func.param(), false, true));
                    }
                }
                DocxBookmarkSegments.removeTrailingBreak(prodSegments);
                DocxBookmarkSegments.removeTrailingBreak(procSegments);
                writer.setCellSegments(row.getCells().get(9), prodSegments);
                writer.setCellSegments(row.getCells().get(10), procSegments);

                rowIndex++;
            }
            writer.save();
        }
    }

    @Override
    public boolean hasGeneratedFiles(DockPackageDto dto) {
        path = dto.path();
        return java.nio.file.Files.exists(resolveOutPath(dto.kpName()));
    }

    private void fillOpInfo(XWPFTableCell cell, OperDto op) {
        var p = cell.getParagraphs().getFirst();
        var r = p.createRun();
        r.setText(op.name());
        r.addBreak();
        r.setText("Цех " + op.numZech());
        r.addBreak();
        r.setText(op.oborud());
    }

    private void fillFuncSpecChars(XWPFTableCell cell, List<FuncDto> funcs) {
        var p = cell.getParagraphs().getFirst();
        funcs.stream()
                .map(FuncDto::specCharakt)
                .filter(StringUtils::isNotBlank)
                .forEach(s -> {
                    var r = p.createRun();
                    r.setText(s);
                    r.addBreak();
                });
    }

    private void fillFuncNameParam(XWPFTableCell cell, List<FuncDto> funcs, boolean isProd) {
        var p = cell.getParagraphs().getFirst();
        funcs.stream()
                .filter(f -> f.isProd() == isProd)
                .forEach(f -> {
                    var r = p.createRun();
                    r.setText(f.name());
                    r.addBreak();
                    r.setText(f.param());
                    r.addBreak();
                });
    }
}

