package by.vstu.isit.documentprocessor.services.docx.update;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxUpdater;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class RiWordUpdater extends AbstractDocxUpdater {
    public RiWordUpdater(
            @Value("${inp.ri.path}") Resource inp,
            @Value("${tmp.out.ri.path}") String tmp,
            @Value("${out.ri.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void update(DockPackageDto dto) throws Exception {
        path = dto.path();
        for (var oper : dto.opers()) {
            updateForOper(dto, oper, oper.name(), dto.path(), dto.path());
        }
    }

    public void updateForOper(DockPackageDto dto, OperDto oper, String sourceOperName, String sourceBasePath, String targetBasePath) throws Exception {
        var source = resolveOutPath(sourceBasePath, sourceOperName);
        ensureExisting(source);
        var target = resolveOutPath(targetBasePath, oper.name());
        doUpdate(dto, oper, source, target);
    }

    private void doUpdate(DockPackageDto dto, OperDto oper, Path source, Path target) throws Exception {
        try (var writer = new Docx4jBookmarkWriter(inpPath, source, target)) {
            writer.updateBookmarkText("d", designationsAssemblyUnit(dto.sborEds()));
            writer.updateBookmarkText("d1", dto.sborEds().getFirst().nazv());
            writer.updateBookmarkText("shop", oper.shifr());
            writer.updateBookmarkText("namOp", oper.name());
            writer.updateBookmarkText("numOp", oper.numOper());
            writer.updateBookmarkText("p", dto.packageName());
            writer.updateBookmarkText("oObr", oper.oborud());
            writer.updateBookmarkText("oOst", oper.ostnasInstr());

            writer.setHeaderCellSegments(0, 1, 2, List.of(
                    Docx4jBookmarkWriter.CellSegment.text(designationsAssemblyUnit(dto.sborEds()), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 1, 3, List.of(
                    Docx4jBookmarkWriter.CellSegment.text(dto.sborEds().getFirst().nazv(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 2, 1, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Наименование операции: ", false),
                    Docx4jBookmarkWriter.CellSegment.text(oper.shifr(), false),
                    Docx4jBookmarkWriter.CellSegment.text(" ", false),
                    Docx4jBookmarkWriter.CellSegment.text(oper.name(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 2, 2, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Номер операции: ", false),
                    Docx4jBookmarkWriter.CellSegment.text(oper.numOper(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 2, 3, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Изделие: ", false),
                    Docx4jBookmarkWriter.CellSegment.text(dto.packageName(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 7, 0, List.of(
                    Docx4jBookmarkWriter.CellSegment.text(oper.oborud(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 7, 1, List.of(
                    Docx4jBookmarkWriter.CellSegment.text(oper.ostnasInstr(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);

            var rowIndices = writer.listBookmarkIndices("ri_r", "_funcName");
            for (int i = 0; i < Math.min(oper.funcs().size(), rowIndices.size()); i++) {
                var func = oper.funcs().get(i);
                int rowIndex = rowIndices.get(i);
                writer.updateBookmarkText("ri_r" + rowIndex + "_funcName", func.name());
                writer.updateBookmarkText("ri_r" + rowIndex + "_funcParam", func.param());
                writer.updateBookmarkText("ri_r" + rowIndex + "_funcSpec", func.specCharakt());
            }
            writer.save();
        }
    }
}
