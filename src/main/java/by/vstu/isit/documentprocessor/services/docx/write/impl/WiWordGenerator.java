package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WiWordGenerator extends AbstractWordGenerator {
    private static final int COLUMN_COUNT = 5;

    public WiWordGenerator(
            @Value("${inp.wi.path}") Resource inp,
            @Value("${out.wi.path}") String out
    ) {
        super(inp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().get(1);
            table.getRow(0).setRepeatHeader(true);

            for (var oper : dto.opers()) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                addBookmark(row.getCell(0), "oper_" + oper.id() + "_col_0", oper.numOper());
                addBookmark(row.getCell(1), "oper_" + oper.id() + "_shifr", oper.shifr());
                appendBookmark(row.getCell(1), "oper_" + oper.id() + "_name", oper.name());
                addBookmark(row.getCell(2), "oper_" + oper.id() + "_numZech", oper.numZech());
                appendBookmark(row.getCell(2), "oper_" + oper.id() + "_nomInstr", oper.nomInstr());
            }

            postProcess(doc, dto.path(), dto.vedIName(), Map.of(
                    "d", designationsAssemblyUnit(dto.sborEds()),
                    "d1", dto.sborEds().getFirst().nazv(),
                    "p", dto.packageName()));
        }
    }

}
