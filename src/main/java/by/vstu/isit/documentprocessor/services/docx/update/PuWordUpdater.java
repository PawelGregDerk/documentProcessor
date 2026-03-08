package by.vstu.isit.documentprocessor.services.docx.update;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxUpdater;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
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
        var target = resolveOutPath(copyPath(path), dto.puName());
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
            int totalRows = dto.opers().stream().mapToInt(o -> o.funcs().size()).sum();
            int rowIndex = table.getRows().getCount() - totalRows;

            for (var oper : dto.opers()) {
                for (int funcIndex = 0; funcIndex < oper.funcs().size(); funcIndex++) {
                    var func = oper.funcs().get(funcIndex);
                    writer.updateBookmarkText("pu_r" + rowIndex + "_numOper", oper.numOper());
                    writer.updateBookmarkText("pu_r" + rowIndex + "_operName", oper.name());
                    writer.updateBookmarkText("pu_r" + rowIndex + "_oborud", oper.oborud());
                    writer.updateBookmarkText("pu_r" + rowIndex + "_ostnas", oper.ostnasInstr());

                    if (func.isProd()) {
                        writer.updateBookmarkText("pu_r" + rowIndex + "_funcName_prod", func.name());
                    } else {
                        writer.updateBookmarkText("pu_r" + rowIndex + "_funcName_proc", func.name());
                    }

                    writer.updateBookmarkText("pu_r" + rowIndex + "_funcSpec", func.specCharakt());
                    writer.updateBookmarkText("pu_r" + rowIndex + "_funcParam", func.param());

                    rowIndex++;
                }
            }
            writer.save();
        }
    }
}

