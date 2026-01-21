package by.vstu.isit.documentprocessor.services.docx.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.abstracts.AbstractWordGenerator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WiWordGenerator extends AbstractWordGenerator {
    private static final int COLUMN_COUNT = 5;

    public WiWordGenerator(
            @Value("${inp.wi.path}") Resource inp,
            @Value("${tmp.out.wi.path}") String tmp,
            @Value("${out.wi.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().get(1);
            table.getRow(0).setRepeatHeader(true);
            for (var oper : dto.opers()) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                row.getCell(0).setText(oper.numOper());
                row.getCell(1).setText(oper.shifr() + " " + oper.name());
                row.getCell(2).setText(oper.numZech() + "-" + oper.nomInstr());
            }

            for (var oper : dto.opers()) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                row.getCell(0).setText(oper.numOper());
                row.getCell(1).setText(oper.shifr() + " " + oper.name());
                row.getCell(2).setText(oper.numZech() + "-" + oper.nomInstr());
            }

            for (var oper : dto.opers()) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                row.getCell(0).setText(oper.numOper());
                row.getCell(1).setText(oper.shifr() + " " + oper.name());
                row.getCell(2).setText(oper.numZech() + "-" + oper.nomInstr());
            }

            postProcess(doc, "WI-0001", Map.of("d", dto.vedIName(), "p", dto.packageName()));
        }
    }
}
