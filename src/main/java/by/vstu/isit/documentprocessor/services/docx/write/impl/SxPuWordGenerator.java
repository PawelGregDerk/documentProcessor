package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class SxPuWordGenerator extends AbstractWordGenerator {

    private static final int COLUMN_COUNT = 6;
    private static final int DATA_START_ROW = 3; // после заголовков

    public SxPuWordGenerator(
            @Value("${inp.sxpu.path}") Resource inp,
            @Value("${out.sxpu.path}") String out
    ) {
        super(inp, out);
    }

    @Override
    public void generate(DockPackageDto dto) throws Exception {

        // собираем все функции со спец. характеристиками
        List<FuncDto> funcs = dto.opers().stream()
                .flatMap(op -> op.funcs().stream())
                .filter(f -> isNotBlank(f.specCharakt()))
                .toList();

        if (funcs.isEmpty()) {
            return; // документ не создаётся
        }

        try (var inp = inpPath.getInputStream(); var doc = new XWPFDocument(inp)) {
            var table = doc.getTables().get(1);
            for (FuncDto func : funcs) {
                var row = table.createRow();
                ensureCells(row, COLUMN_COUNT);
                addBookmark(row.getCell(1), "func_" + func.id() + "_col_1", func.name());
                addBookmark(row.getCell(2), "func_" + func.id() + "_col_2", func.param());
                addBookmark(row.getCell(3), "func_" + func.id() + "_col_3", func.specCharakt());
                addBookmark(row.getCell(4), "func_" + func.id() + "_col_4", "");
                addBookmark(row.getCell(5), "func_" + func.id() + "_col_5", "");
            }

            postProcess(doc, dto.path(), dto.spuName(), Map.of(
                    "puName", dto.puName(),
                    "spuName", dto.spuName(),
                    "nazv", dto.sborEds().getFirst().nazv(),
                    "oboznach", designationsAssemblyUnit(dto.sborEds()),
                    "packageName", dto.packageName()
            ));
        }
    }
}
