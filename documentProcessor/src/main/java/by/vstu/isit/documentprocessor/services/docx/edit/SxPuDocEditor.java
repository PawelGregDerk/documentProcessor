package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.SxPuWordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;

@Service
public class SxPuDocEditor extends AbstractDocEditor {

    private static final int COLUMN_COUNT = 6;
    @Autowired
    private SxPuWordGenerator sxPuWordGenerator;

    public SxPuDocEditor(
            @Value("${out.sxpu.path}") String src
    ) {
        super(src, src);
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {

        // собираем функции со спец. характеристиками
        var funcs = savedDto.opers().stream()
                .flatMap(op -> op.funcs().stream())
                .filter(f -> f.specCharakt() != null && !f.specCharakt().isBlank())
                .toList();

        // если функций нет, ничего не делаем
        if (funcs.isEmpty()) {
            return;
        }

        try {
            var doc = loadCopyByBookmark(
                    dto.path(),
                    dto.spuName(),
                    savedDto.spuName(),
                    0,
                    sourceBookmark(dto)
            );

            var table = doc.getSections().get(0).getTables().get(1);

            var processedIds = new HashSet<Long>();

            for (FuncDto func : funcs) {
                processedIds.add(func.id());

                boolean exists = findCellByBookmark(table, "func_" + func.id() + "_col_1") != null;

                if (exists) {
                    updateCell(table, "func_" + func.id() + "_col_1", func.name());
                    updateCell(table, "func_" + func.id() + "_col_2", func.param());
                    updateCell(table, "func_" + func.id() + "_col_3", func.specCharakt());
                } else {
                    var row = insertRowAfterLastBookmark(table, "func_", COLUMN_COUNT);
                    setShadedText(row.getCells().get(1), func.name());
                    setShadedText(row.getCells().get(2), func.param());
                    setShadedText(row.getCells().get(3), func.specCharakt());
                }
            }

            removeDeletedRows(table, processedIds, "func_", "_col_1");

            Map<String, String> oldHeader = resolveHeaderOldData(
                    Map.of(
                            "puName", dto.puName(),
                            "spuName", dto.spuName(),
                            "nazv", dto.sborEds().getFirst().nazv(),
                            "oboznach", dto.sborEds().getFirst().oboznach(),
                            "packageName", dto.packageName()
                    )
            );

            updateHeader(doc, oldHeader, Map.of(
                    "puName", savedDto.puName(),
                    "spuName", savedDto.spuName(),
                    "nazv", savedDto.sborEds().getFirst().nazv(),
                    "oboznach", designationsAssemblyUnit(savedDto.sborEds()),
                    "packageName", savedDto.packageName()
            ));

            save(doc, savedDto.path(), savedDto.spuName());
        } catch (java.nio.file.NoSuchFileException ex) {
            // если документа не было в оригинале, создаём его заново
            sxPuWordGenerator.generate(savedDto);
            // перемещаем созданный файл в папку копия_
            Path originalPath = Path.of(MessageFormat.format(srcPath, savedDto.path(), savedDto.spuName()));
            Path copyPath = Path.of(MessageFormat.format(destPath, copyFolder(savedDto.path()), savedDto.spuName()));
            Files.createDirectories(copyPath.getParent());
            Files.move(originalPath, copyPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String sourceBookmark(DockPackageDto dto) {
        return dto.opers().stream()
                .flatMap(op -> op.funcs().stream())
                .filter(f -> f.specCharakt() != null && !f.specCharakt().isBlank())
                .findFirst()
                .map(f -> "func_" + f.id() + "_col_1")
                .orElse(null);
    }
}

