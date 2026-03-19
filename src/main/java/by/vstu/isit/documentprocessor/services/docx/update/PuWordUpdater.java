package by.vstu.isit.documentprocessor.services.docx.update;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxUpdater;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class PuWordUpdater extends AbstractDocxUpdater {
    public PuWordUpdater(
            @Value("${inp.pu.path}") Resource inp,
            @Value("${tmp.out.pu.path}") String tmp,
            @Value("${out.pu.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void update(DockPackageDto dto) throws Exception {
        path = dto.path();
        var source = resolveOutPath(path, dto.puName());
        ensureExisting(source);
        var target = source; // Сохраняем в ту же папку
        updateDocument(dto, source, target);
    }
    
    @Override
    public void updateWithRename(DockPackageDto dto, DockPackageDto originalDto, Path targetPath) throws Exception {
        path = dto.path();
        var source = resolveOutPath(path, originalDto.puName());
        ensureExisting(source);
        updateDocument(dto, source, targetPath);
    }
    
    private void updateDocument(DockPackageDto dto, Path source, Path target) throws Exception {
        try (var writer = new Docx4jBookmarkWriter(inpPath, source, target)) {
            writer.updateBookmarkText("d", article(dto));
            writer.updateBookmarkText("p", dto.packageName());
            writer.updateBookmarkText("n", dto.puName());

            writer.setHeaderCellSegments(0, 3, 1, List.of(
                    Docx4jBookmarkWriter.CellSegment.text(article(dto), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 3, 2, List.of(
                    Docx4jBookmarkWriter.CellSegment.text(dto.packageName(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 3, 3, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("ПУ", false),
                    Docx4jBookmarkWriter.CellSegment.text(dto.puName(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);

            var table = writer.getTable(0);
            var rowIndices = writer.listBookmarkIndices("pu_r", "_numOper");
            for (int ri = 0; ri < Math.min(dto.opers().size(), rowIndices.size()); ri++) {
                var oper = dto.opers().get(ri);
                int rowIndex = rowIndices.get(ri);
                for (int funcIndex = 0; funcIndex < oper.funcs().size(); funcIndex++) {
                    var func = oper.funcs().get(funcIndex);
                    int curRow = rowIndex + funcIndex;
                    var row = table.getRows().get(curRow);
                    writer.updateBookmarkText("pu_r" + rowIndex + "_numOper", oper.numOper());
                    writer.updateBookmarkText("pu_r" + rowIndex + "_operName", oper.name());
                    writer.updateBookmarkText("pu_r" + rowIndex + "_oborud", oper.oborud());
                    writer.updateBookmarkText("pu_r" + rowIndex + "_ostnas", oper.ostnasInstr());

                    boolean prodUpdated = writer.updateBookmarkText("pu_r" + curRow + "_funcName_prod", func.isProd() ? func.name() : "");
                    boolean procUpdated = writer.updateBookmarkText("pu_r" + curRow + "_funcName_proc", func.isProd() ? "" : func.name());
                    if (!prodUpdated || !procUpdated) {
                        List<Docx4jBookmarkWriter.CellSegment> prodSeg = new ArrayList<>();
                        List<Docx4jBookmarkWriter.CellSegment> procSeg = new ArrayList<>();
                        if (func.isProd()) {
                            prodSeg.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                    "pu_r" + curRow + "_funcName_prod", func.name(), false, false));
                        } else {
                            procSeg.add(Docx4jBookmarkWriter.CellSegment.bookmark(
                                    "pu_r" + curRow + "_funcName_proc", func.name(), false, false));
                        }
                        writer.setCellSegments(row.getCells().get(4), prodSeg);
                        writer.setCellSegments(row.getCells().get(5), procSeg);
                    }

                    writer.updateBookmarkText("pu_r" + curRow + "_funcSpec", func.specCharakt());
                    writer.updateBookmarkText("pu_r" + curRow + "_funcParam", func.param());
                }
            }
            writer.save();
        }
    }
}

