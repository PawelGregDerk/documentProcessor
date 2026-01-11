package by.vstu.isit.documentprocessor.services.docx;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
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

    protected void clearCell(XWPFTableCell cell) {
        if (cell == null) {
            return;
        }

        for (int i = cell.getParagraphs().size() - 1; i >= 0; i--) {
            cell.removeParagraph(i);
        }
        cell.addParagraph();
    }

    protected void mergeVertical(XWPFTable table, int start, int end, int col) {
        for (int r = start; r <= end; r++) {
            var row = table.getRow(r);
            if (row == null) {
                continue;
            }

            var cell = row.getCell(col);
            if (cell == null) {
                continue;
            }

            var tcPr = cell.getCTTc().isSetTcPr()
                    ? cell.getCTTc().getTcPr()
                    : cell.getCTTc().addNewTcPr();
            var merge = tcPr.isSetVMerge() ? tcPr.getVMerge() : tcPr.addNewVMerge();
            merge.setVal(r == start ? STMerge.RESTART : STMerge.CONTINUE);
        }
    }

    protected void mergeHorizontal(XWPFTableRow row, int col, int span) {
        var cell = row.getCell(col);
        if (cell == null) {
            return;
        }

        var tcPr = cell.getCTTc().isSetTcPr()
                ? cell.getCTTc().getTcPr()
                : cell.getCTTc().addNewTcPr();
        if (tcPr.isSetGridSpan()) {
            tcPr.getGridSpan().setVal(BigInteger.valueOf(span));
        } else {
            tcPr.addNewGridSpan().setVal(BigInteger.valueOf(span));
        }
    }

    protected void fillHeaderFromSecondPage(
            XWPFDocument doc,
            String placeholder,
            String value
    ) {
        if (doc.getHeaderList().size() <= 1) {
            return;
        }

        var header = doc.getHeaderList().get(1);
        for (var p : header.getParagraphs()) {
            p.getRuns().forEach(r -> {
                if (r.text() != null && r.text().contains(placeholder)) {
                    r.setText(value, 0);
                }
            });
        }
    }

    protected void fillTextCell(XWPFTableCell cell, String value) {
        clearCell(cell);
        cell.addParagraph().createRun().setText(value);
    }

    protected void postProcess(DockPackageDto dto, String name) throws Exception {
        try (var in = new FileInputStream(tmpOut);
             var template = XWPFTemplate.compile(in).render(
                     Map.of("d", dto.getFirst().extra(),
                             "n", dto.fmeaName()));
             var out = new FileOutputStream(format(outPath, name))) {

            template.write(out);
        }
        Files.deleteIfExists(Path.of(tmpOut));
    }
}
