package by.vstu.isit.documentprocessor.services.docx.write.abstracts;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    protected void postProcess(XWPFDocument doc, String name, Map<String, String> colData) throws Exception {
        Path tmp = Path.of(tmpOut);
        Path out = Path.of(format(outPath, name));

        try (var outTmp = new FileOutputStream(tmp.toFile())) {
            doc.write(outTmp);
        }

        try (var in = new FileInputStream(tmp.toFile());
             var template = XWPFTemplate.compile(in).render(colData);
             var outFinal = new FileOutputStream(out.toFile())) {
            template.write(outFinal);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    protected String designationsAssemblyUnit(List<SborEdDto> sborEdList) {
        String first = sborEdList.getFirst().oboznach();
        String last = sborEdList.getLast().oboznach();
        return first + "\u2014" + last;
    }
}
