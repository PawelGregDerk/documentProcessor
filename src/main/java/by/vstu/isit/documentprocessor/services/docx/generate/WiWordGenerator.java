package by.vstu.isit.documentprocessor.services.docx.generate;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxGenerator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Генератор WI (ведомость инструкций): заполняет таблицу операций и
 * проставляет закладки в колонтитулах и таблице.
 */
@Service
public class WiWordGenerator extends AbstractDocxGenerator {
    /**
     * Количество колонок таблицы WI.
     */
    private static final int COLUMN_COUNT = 5;

    public WiWordGenerator(
            @Value("${inp.wi.path}") Resource inp,
            @Value("${tmp.out.wi.path}") String tmp,
            @Value("${out.wi.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        path = dto.path();
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().get(1);
            table.getRow(0).setRepeatHeader(true);

            for (var oper : dto.opers()) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                row.getCell(0).setText(oper.numOper());
                row.getCell(1).setText(oper.shifr() + " " + oper.name());
                row.getCell(2).setText(oper.numZech() + "-" + oper.nomInstr());
            }

            postProcess(doc, dto.vedIName(), Map.of(
                    "d", designationsAssemblyUnit(dto.sborEds()),
                    "d1", dto.sborEds().getFirst().nazv(),
                    "p", dto.packageName()));
        }

        var out = resolveOutPath(dto.vedIName());
        try (var writer = new Docx4jBookmarkWriter(inpPath, out)) {
            writer.setHeaderCellSegments(0, 1, 2, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "d", designationsAssemblyUnit(dto.sborEds()), false, false)
            ));
            writer.setHeaderCellSegments(0, 1, 3, List.of(
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "d1", dto.sborEds().getFirst().nazv(), false, false)
            ));
            writer.setHeaderCellSegments(0, 1, 4, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Изделие: ", false),
                    Docx4jBookmarkWriter.CellSegment.bookmark(
                            "p", dto.packageName(), false, false)
            ));

            var table = writer.getTable(1);
            int totalRows = dto.opers().size();
            int rowIndex = table.getRows().getCount() - totalRows;
            for (var oper : dto.opers()) {
                var row = table.getRows().get(rowIndex);
                writer.setCellSegments(row.getCells().get(0), List.of(
                        Docx4jBookmarkWriter.CellSegment.bookmark(
                                "wi_r" + rowIndex + "_numOper", oper.numOper(), false, false)
                ));

                List<Docx4jBookmarkWriter.CellSegment> nameSegments = new ArrayList<>();
                nameSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                        "wi_r" + rowIndex + "_shifr", oper.shifr(), false, false));
                nameSegments.add(Docx4jBookmarkWriter.CellSegment.text(" ", false));
                nameSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                        "wi_r" + rowIndex + "_operName", oper.name(), false, false));
                writer.setCellSegments(row.getCells().get(1), nameSegments);

                List<Docx4jBookmarkWriter.CellSegment> instrSegments = new ArrayList<>();
                instrSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                        "wi_r" + rowIndex + "_numZech", oper.numZech(), false, false));
                instrSegments.add(Docx4jBookmarkWriter.CellSegment.text("-", false));
                instrSegments.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                        "wi_r" + rowIndex + "_nomInstr", oper.nomInstr(), false, false));
                writer.setCellSegments(row.getCells().get(2), instrSegments);

                rowIndex++;
            }
            writer.save();
        }
    }

    @Override
    public boolean hasGeneratedFiles(DockPackageDto dto) {
        path = dto.path();
        return java.nio.file.Files.exists(resolveOutPath(dto.vedIName()));
    }
}

