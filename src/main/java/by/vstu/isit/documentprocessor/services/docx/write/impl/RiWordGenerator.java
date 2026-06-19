package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.VerticalMerger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;

import static java.text.MessageFormat.format;

@Service
public class RiWordGenerator extends AbstractWordGenerator implements VerticalMerger {
    private static final int COLUMN_COUNT = 4;
    private static final int DATA_START_ROW = 1;

    public RiWordGenerator(
            @Value("${inp.ri.path}") Resource inp,
            @Value("${out.ri.path}") String out
    ) {
        super(inp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {
        for (var oper : dto.opers()) {
            try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
                var table = doc.getTables().get(2);

                for (var fun : oper.funcs()) {
                    var row = createRow(table, fun);
                    addBookmark(row.getCell(1), "func_" + fun.id() + "_name", fun.name());
                    appendBookmark(row.getCell(1), "func_" + fun.id() + "_param", fun.param());
                    addBookmark(row.getCell(2), "func_" + fun.id() + "_col_2", fun.specCharakt());
                }

                int endRow = table.getNumberOfRows() - 1;
                mergeVertical(table, DATA_START_ROW, endRow, 3);

                String article = designationsAssemblyUnit(dto.sborEds());
                postProcess(doc, dto.path(), "ri", oper.name(), Map.of(
                        "d", article,
                        "d1", dto.sborEds().getFirst().nazv(),
                        "p", dto.packageName(),
                        "shop", oper.shifr(),
                        "namOp", oper.name(),
                        "numOp", oper.numOper(),
                        "oObr", oper.oborud(),
                        "oOst", oper.ostnasInstr(),
                        "nZech", oper.numZech()
                ));
            }
        }
    }

    public XWPFDocument generateOne(OperDto oper) throws Exception {

        var inp = inpPath.getInputStream();
        var doc = new XWPFDocument(inp);

        var table = doc.getTables().get(2);

        for (var fun : oper.funcs()) {
            var row = createRow(table, fun);
            addBookmark(row.getCell(1), "func_" + fun.id() + "_name", fun.name());
            appendBookmark(row.getCell(1), "func_" + fun.id() + "_param", fun.param());
            addBookmark(row.getCell(2), "func_" + fun.id() + "_col_2", fun.specCharakt());
        }

        int endRow = table.getNumberOfRows() - 1;
        mergeVertical(table, DATA_START_ROW, endRow, 3);

        inp.close(); // можно закрыть поток, но НЕ документ

        return doc;
    }

    private void postProcess(XWPFDocument doc, String folder, String sub, String name, Map<String, String> colData) throws Exception {
        writeDocument(doc, Path.of(format(outPath, folder, sub, name)), colData);
    }

    private XWPFTableRow createRow(XWPFTable table, FuncDto fun) {
        var row = table.createRow();
        ensureCells(row, COLUMN_COUNT);
        return row;
    }
}
