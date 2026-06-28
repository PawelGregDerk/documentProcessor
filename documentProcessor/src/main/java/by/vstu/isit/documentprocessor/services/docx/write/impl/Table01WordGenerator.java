package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

import static org.apache.commons.lang3.StringUtils.right;

@Service
public class Table01WordGenerator extends AbstractWordGenerator {

    public Table01WordGenerator(
            @Value("${inp.tab01.path}") Resource inpPath,
            @Value("${out.tab01.path}") String outPath
    ) {
        super(inpPath, outPath);
    }

    public void generate(DockPackageDto dto) throws Exception {
        if (dto.sborEds().size() <= 1) {
            return;
        }

        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().get(0);
            // Генерация строк
            for (var se : dto.sborEds()) {
                var row = table.createRow();
                ensureCells(row, 5);
                setCellWithBookmark(row.getCell(0), "t01_name_" + se.id(),
                        se.nazv() + ", " + se.oboznach() + ", " + fillFirstCell(dto.packageName(), se));

                // Остальные столбцы пустые
                for (int c = 1; c < 5; c++) {
                    setCell(row.getCell(c), "");
                }
            }

            // Путь сохранения
            Path out = Path.of(MessageFormat.format(outPath, dto.path()));
            Files.createDirectories(out.getParent());

            // POI пишет сразу в итоговый файл
            try (var outStream = new FileOutputStream(out.toFile())) {
                doc.write(outStream);
            }
        }
    }

    private void setCell(XWPFTableCell cell, String text) {
        cell.removeParagraph(0);
        var p = cell.addParagraph();
        p.createRun().setText(text == null ? "" : text);
    }

    private void setCellWithBookmark(XWPFTableCell cell, String name, String text) {
        cell.removeParagraph(0);
        var p = cell.addParagraph();
        var ctp = p.getCTP();

        BigInteger id = BigInteger.valueOf(Math.abs(name.hashCode()));

        var start = ctp.addNewBookmarkStart();
        start.setName(name);
        start.setId(id);

        p.createRun().setText(text == null ? "" : text);

        var end = ctp.addNewBookmarkEnd();
        end.setId(id);
    }

    public String fillFirstCell(String name, SborEdDto dto) {
        String cellData = name;
        String[] parts = dto.oboznach().split("--");
        outer:
        {
            if (parts.length < 2) {
                break outer;
            }

            String first = right(parts[0].trim(), 1);
            String last = right(parts[1].trim(), 1);
            if ("0".equals(first)) {
                cellData = cellData + "-" + name;
            } else {
                cellData = cellData + "-" + first + "-" + name;
            }

            if (!"0".equals(last)) {
                cellData = cellData  + "-"+ last;
            }
        }
        return cellData;
    }
}
