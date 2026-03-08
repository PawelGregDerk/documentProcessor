package by.vstu.isit.documentprocessor.services.docx.update;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxUpdater;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class WiWordUpdater extends AbstractDocxUpdater {
    public WiWordUpdater(
            @Value("${inp.wi.path}") Resource inp,
            @Value("${tmp.out.wi.path}") String tmp,
            @Value("${out.wi.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void update(DockPackageDto dto) throws Exception {
        path = dto.path();
        var source = resolveOutPath(path, dto.vedIName());
        ensureExisting(source);
        var target = resolveOutPath(copyPath(path), dto.vedIName());
        updateDocument(dto, source, target);
    }
    
    @Override
    public void updateWithRename(DockPackageDto dto, DockPackageDto originalDto, java.nio.file.Path targetPath) throws Exception {
        path = dto.path();
        var source = resolveOutPath(path, originalDto.vedIName());
        ensureExisting(source);
        updateDocument(dto, source, targetPath);
    }
    
    private void updateDocument(DockPackageDto dto, java.nio.file.Path source, java.nio.file.Path target) throws Exception {
        try (var writer = new Docx4jBookmarkWriter(inpPath, source, target)) {
            writer.updateBookmarkText("d", designationsAssemblyUnit(dto.sborEds()));
            writer.updateBookmarkText("d1", dto.sborEds().getFirst().nazv());
            writer.updateBookmarkText("p", dto.packageName());

            var table = writer.getTable(1);
            int totalRows = dto.opers().size();
            int rowIndex = table.getRows().getCount() - totalRows;
            for (var oper : dto.opers()) {
                writer.updateBookmarkText("wi_r" + rowIndex + "_numOper", oper.numOper());
                writer.updateBookmarkText("wi_r" + rowIndex + "_shifr", oper.shifr());
                writer.updateBookmarkText("wi_r" + rowIndex + "_operName", oper.name());
                writer.updateBookmarkText("wi_r" + rowIndex + "_numZech", oper.numZech());
                writer.updateBookmarkText("wi_r" + rowIndex + "_nomInstr", oper.nomInstr());
                rowIndex++;
            }
            writer.save();
        }
    }
}

