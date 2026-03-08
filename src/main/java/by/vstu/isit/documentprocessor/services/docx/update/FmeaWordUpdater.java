package by.vstu.isit.documentprocessor.services.docx.update;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.bookmarks.Docx4jBookmarkWriter;
import by.vstu.isit.documentprocessor.services.docx.common.AbstractDocxUpdater;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FmeaWordUpdater extends AbstractDocxUpdater {
    public FmeaWordUpdater(
            @Value("${inp.fmea.path}") Resource inp,
            @Value("${tmp.out.fmea.path}") String tmp,
            @Value("${out.fmea.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void update(DockPackageDto dto) throws Exception {
        path = dto.path();
        var source = resolveOutPath(path, dto.fmeaName());
        ensureExisting(source);
        var target = resolveOutPath(copyPath(path), dto.fmeaName());
        updateDocument(dto, source, target);
    }
    
    @Override
    public void updateWithRename(DockPackageDto dto, DockPackageDto originalDto, java.nio.file.Path targetPath) throws Exception {
        path = dto.path();
        var source = resolveOutPath(path, originalDto.fmeaName());
        ensureExisting(source);
        updateDocument(dto, source, targetPath);
    }
    
    private void updateDocument(DockPackageDto dto, java.nio.file.Path source, java.nio.file.Path target) throws Exception {
        try (var writer = new Docx4jBookmarkWriter(inpPath, source, target)) {
            writer.updateBookmarkText("p", dto.packageName());
            writer.updateBookmarkText("d", article(dto));
            writer.updateBookmarkText("n", dto.fmeaName());

            var table = writer.getTable(0);
            int totalRows = dto.opers().stream().mapToInt(o -> o.funcs().size()).sum();
            int fallbackRowIndex = table.getRows().getCount() - totalRows;
            var bookmarkRows = writer.listBookmarkIndices("fmea_r", "_funcName");
            int pos = 0;
            for (var oper : dto.opers()) {
                for (int funcIndex = 0; funcIndex < oper.funcs().size(); funcIndex++) {
                    var func = oper.funcs().get(funcIndex);
                    int rowIndex = pos < bookmarkRows.size() ? bookmarkRows.get(pos) : fallbackRowIndex;
                    if (funcIndex == 0) {
                        writer.updateBookmarkText("fmea_r" + rowIndex + "_vedI", dto.vedIName());
                        writer.updateBookmarkText("fmea_r" + rowIndex + "_numOper", oper.numOper());
                        writer.updateBookmarkText("fmea_r" + rowIndex + "_operName", oper.name());
                        writer.updateBookmarkText("fmea_r" + rowIndex + "_numZech", oper.numZech());
                    }
                    writer.updateBookmarkText("fmea_r" + rowIndex + "_funcName", func.name());
                    writer.updateBookmarkText("fmea_r" + rowIndex + "_funcSpec", func.specCharakt());
                    pos++;
                    fallbackRowIndex++;
                }
            }
            writer.save();
        }
    }
}

