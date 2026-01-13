package by.vstu.isit.documentprocessor.services.docx.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.services.docx.abstracts.AbstractWordGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static org.apache.commons.lang3.stream.LangCollectors.joining;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class KpWordGenerator extends AbstractWordGenerator {
    private static final int COLUMN_COUNT = 11;

    protected KpWordGenerator(
            @Value("${inp.kp.path}") Resource inp,
            @Value("${tmp.out.kp.path}") String tmp,
            @Value("${out.kp.path}") String out
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
                var funcs = oper.funcs();
                row.getCell(0).setText(oper.numOper());
                row.getCell(7).setText(format("%s Цех %s %s", oper.name(), oper.numZech(), oper.oborud()));
                var specChars = funcs.stream()
                        .map(FuncDto::specCharakt)
                        .filter(StringUtils::isNotBlank)
                        .collect(joining(" "));
                row.getCell(8).setText(specChars);
                var funcProd = fillFuncCell(funcs, true);
                row.getCell(9).setText(funcProd);
                var funcProc = fillFuncCell(funcs, false);
                row.getCell(10).setText(funcProc);

            }
            try (var out = new FileOutputStream(tmpOut)) {
                doc.write(out);
            }

            postProcess(dto.kpName(), Map.of("d", dto.extra(), "n", dto.kpName()));
        }
    }

    private String fillFuncCell(List<FuncDto> funcs, boolean isProd) {
        return funcs.stream()
                .filter(f -> f.isProd() == isProd)
                .map(f -> StringUtils.joinWith(" ", f.name(), f.param()))
                .filter(StringUtils::isNotBlank)
                .collect(joining(" "));
    }
}
