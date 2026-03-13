package by.vstu.isit.documentprocessor.services.docx.update;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxUpdater;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KpWordUpdater extends AbstractDocxUpdater {
    public KpWordUpdater(
            @Value("${inp.kp.path}") Resource inp,
            @Value("${tmp.out.kp.path}") String tmp,
            @Value("${out.kp.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void update(DockPackageDto dto) throws Exception {
        path = dto.path();
        var source = resolveOutPath(path, dto.kpName());
        ensureExisting(source);
        var target = source; // Сохраняем в ту же папку
        updateDocument(dto, source, target);
    }
    
    @Override
    public void updateWithRename(DockPackageDto dto, DockPackageDto originalDto, java.nio.file.Path targetPath) throws Exception {
        path = dto.path();
        var source = resolveOutPath(path, originalDto.kpName());
        ensureExisting(source);
        updateDocument(dto, source, targetPath);
    }
    
    private void updateDocument(DockPackageDto dto, java.nio.file.Path source, java.nio.file.Path target) throws Exception {
        try (var writer = new Docx4jBookmarkWriter(inpPath, source, target)) {
            writer.updateBookmarkText("n", dto.kpName());
            writer.updateBookmarkText("p", dto.packageName());
            writer.updateBookmarkText("d", article(dto));

            writer.setHeaderCellSegments(0, 3, 0, List.of(
                    Docx4jBookmarkWriter.CellSegment.text("Карта потока процесса КП", false),
                    Docx4jBookmarkWriter.CellSegment.text(dto.kpName(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 3, 1, List.of(
                    Docx4jBookmarkWriter.CellSegment.text(dto.packageName(), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);
            writer.setHeaderCellSegments(0, 3, 2, List.of(
                    Docx4jBookmarkWriter.CellSegment.text(article(dto), false)
            ), Docx4jBookmarkWriter.HeaderFooterScope.DEFAULT_EVEN_ONLY);

            var table = writer.getTable(0);
            int totalRows = dto.opers().size();
            int rowIndex = table.getRows().getCount() - totalRows;
            for (var oper : dto.opers()) {
                var funcs = oper.funcs();
                writer.updateBookmarkText("kp_r" + rowIndex + "_numOper", oper.numOper());
                writer.updateBookmarkText("kp_r" + rowIndex + "_operName", oper.name());
                writer.updateBookmarkText("kp_r" + rowIndex + "_numZech", oper.numZech());
                writer.updateBookmarkText("kp_r" + rowIndex + "_oborud", oper.oborud());

                for (int i = 0; i < funcs.size(); i++) {
                    var func = funcs.get(i);
                    writer.updateBookmarkText("kp_r" + rowIndex + "_func" + i + "_spec", func.specCharakt());
                    if (func.isProd()) {
                        writer.updateBookmarkText("kp_r" + rowIndex + "_func" + i + "_name_prod", func.name());
                        writer.updateBookmarkText("kp_r" + rowIndex + "_func" + i + "_param_prod", func.param());
                    } else {
                        writer.updateBookmarkText("kp_r" + rowIndex + "_func" + i + "_name_proc", func.name());
                        writer.updateBookmarkText("kp_r" + rowIndex + "_func" + i + "_param_proc", func.param());
                    }
                }
                rowIndex++;
            }
            writer.save();
        }
    }
}

