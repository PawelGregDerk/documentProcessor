package by.vstu.isit.documentprocessor.services.docx.abstracts;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.text.MessageFormat.format;

public abstract class AbstractWordGenerator {
    protected final Resource inpPath;
    protected final String tmpOut;
    protected final String outPath;

    protected AbstractWordGenerator(Resource inpPath, String tmpOut, String outPath) {
        this.inpPath = inpPath;
        this.tmpOut = tmpOut;
        this.outPath = outPath;
    }

    public abstract void generate(DockPackageDto dto) throws Exception;

    protected void ensureCells(XWPFTableRow row, int columnCount) {
        while (row.getTableCells().size() < columnCount) {
            row.createCell();
        }
    }

    protected void postProcess(String name, Map<String, String> colData) throws Exception {
        try (var in = new FileInputStream(tmpOut);
             var template = XWPFTemplate.compile(in).render(colData);
             var out = new FileOutputStream(format(outPath, name))) {

            template.write(out);
        }
        Files.deleteIfExists(Path.of(tmpOut));
    }
}
