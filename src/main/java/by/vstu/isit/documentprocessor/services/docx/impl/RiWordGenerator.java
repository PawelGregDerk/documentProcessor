package by.vstu.isit.documentprocessor.services.docx.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.abstracts.AbstractWordGenerator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RiWordGenerator extends AbstractWordGenerator {
    private static final int COLUMN_COUNT = 5;

    public RiWordGenerator(
            @Value("${inp.ri.path}") Resource inp,
            @Value("${tmp.out.ri.path}") String tmp,
            @Value("${out.ri.path}") String out
    ) {
        super(inp, tmp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().getFirst();
            for (var oper : dto.opers()) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                row.getCell(0).setText(oper.numOper());
                row.getCell(1).setText(oper.shifr() + " " + oper.name());
                row.getCell(2).setText(oper.numZech() + "-" + oper.nomInstr());
            }

            postProcess(doc, "RI-0001", Map.of("d", dto.extra()));
        }
    }
}
