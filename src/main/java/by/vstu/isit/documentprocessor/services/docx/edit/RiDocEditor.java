package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.RiWordGenerator;
import com.spire.doc.Table;
import com.spire.doc.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream; // 👈 Добавили импорт
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.text.MessageFormat.format;

@Service
public class RiDocEditor extends AbstractDocEditor {
    private static final int COLUMN_COUNT = 4;
    private final Resource templateResource;
    private final RiWordGenerator generator;

    public RiDocEditor(
            @Value("${out.ri.path}") String src,
            @Value("${inp.ri.path}") Resource templateResource,
            RiWordGenerator generator
    ) {
        super(src, src);
        this.templateResource = templateResource;
        this.generator = generator;
    }

    @Override
    @Transactional(readOnly = true)
    public void edit(DockPackageDto dto, DockPackageDto savedDto) throws Exception {
        Set<String> savedFileNames = new HashSet<>();
        Set<Long> savedOperIds = new HashSet<>();
        for (OperDto oper : savedDto.opers()) {
            savedFileNames.add(fileName(oper));
            if (oper.id() != null) {
                savedOperIds.add(oper.id());
            }
        }

        for (OperDto oper : savedDto.opers()) {
            OperDto origOper = dto.opers().stream()
                    .filter(o -> o.id().equals(oper.id()))
                    .findFirst()
                    .orElse(null);

            boolean isNew = origOper == null;

            if (isNew) {
                generator.generateOne(oper, savedDto.path(), Map.of(
                        "d", designationsAssemblyUnit(savedDto.sborEds()),
                        "d1", dseText(savedDto.sborEds(), savedDto.sborEds().getFirst().nazv()),
                        "p", dseText(savedDto.sborEds(), savedDto.packageName()),
                        "wi", oper.nomInstr(),
                        "shop", oper.shifr(),
                        "namOp", oper.name(),
                        "numOp", oper.numOper(),
                        "oObr", oper.oborud(),
                        "oOst", oper.ostnasInstr(),
                        "nZech", oper.numZech()
                ));
                continue;
            }

            Document doc;
            try {
                doc = loadCopySubByBookmark(
                        dto.path(),
                        "ri",
                        fileName(origOper),
                        fileName(oper),
                        2,
                        sourceBookmark(origOper)
                );
            } catch (NoSuchFileException ex) {
                doc = new Document();
                try (InputStream is = templateResource.getInputStream()) {
                    doc.loadFromStream(is, FileFormat.Docx);
                }
            }

            Table table = doc.getSections().get(0).getTables().get(2);

            Set<Long> processedFuncIds = new HashSet<>();

            for (FuncDto func : oper.funcs()) {
                processedFuncIds.add(func.id());
                boolean exists = findCellByBookmark(table, "func_" + func.id() + "_name") != null;

                if (exists) {
                    updateCell(table, "func_" + func.id() + "_name", func.name());
                    updateCell(table, "func_" + func.id() + "_param", func.param());
                    updateCell(table, "func_" + func.id() + "_col_2", func.specCharakt());
                } else {
                    TableRow newRow = insertRowAfterLastBookmark(table, "func_", COLUMN_COUNT);
                    var cell1 = newRow.getCells().get(1);
                    setShadedBookmarkedText(cell1, "func_" + func.id() + "_name", func.name());
                    appendShadedBookmark(cell1.getParagraphs().get(0), "func_" + func.id() + "_param", func.param());
                    setShadedBookmarkedText(newRow.getCells().get(2), "func_" + func.id() + "_col_2", func.specCharakt());
                }
            }

            removeDeletedRows(table, processedFuncIds, "func_", "_name");

            HeaderUpdateHelper.update(this, doc, Map.of(
                    "d", designationsAssemblyUnit(dto.sborEds()),
                    "d1", dseText(dto.sborEds(), dto.sborEds().getFirst().nazv()),
                    "p", dseText(dto.sborEds(), dto.packageName()),
                    "wi", oper.nomInstr(),
                    "shop", origOper.shifr(),
                    "namOp", origOper.name(),
                    "numOp", origOper.numOper(),
                    "oObr", origOper.oborud(),
                    "oOst", origOper.ostnasInstr(),
                    "nZech", oper.numZech()
            ), Map.of(
                    "d", designationsAssemblyUnit(savedDto.sborEds()),
                    "d1", dseText(savedDto.sborEds(), savedDto.sborEds().getFirst().nazv()),
                    "p", dseText(savedDto.sborEds(), savedDto.packageName()),
                    "wi", oper.nomInstr(),
                    "shop", oper.shifr(),
                    "namOp", oper.name(),
                    "numOp", oper.numOper(),
                    "oObr", oper.oborud(),
                    "oOst", oper.ostnasInstr(),
                    "nZech", oper.numZech()
            ));

            saveSub(doc, savedDto.path(), "ri", fileName(oper));

            if (!fileName(origOper).equals(fileName(oper))) {
                Path oldFile = Path.of(format(destPath, copyFolder(savedDto.path()), "ri", fileName(origOper)));
                if (!savedFileNames.contains(fileName(origOper))) {
                    Files.deleteIfExists(oldFile);
                }
            }
        }

        deleteRemovedOperationFiles(dto, savedDto, savedOperIds, savedFileNames);
    }

    private void deleteRemovedOperationFiles(DockPackageDto original,
                                             DockPackageDto saved,
                                             Set<Long> savedOperIds,
                                             Set<String> savedFileNames) {
        for (OperDto originalOper : original.opers()) {
            if (originalOper.id() == null || savedOperIds.contains(originalOper.id())) {
                continue;
            }
            String removedName = fileName(originalOper);
            if (savedFileNames.contains(removedName)) {
                continue;
            }
            Path removedFile = Path.of(format(destPath, copyFolder(saved.path()), "ri", removedName));
            try {
                Files.deleteIfExists(removedFile);
                deleteMeta(removedFile);
            } catch (Exception ignored) {
                // stale RI file must not fail the whole package save
            }
        }
    }

    private void deleteMeta(Path document) {
        Path meta = document.getParent().resolve(".docproc-meta")
                .resolve(document.getFileName() + ".hdr.properties");
        try {
            Files.deleteIfExists(meta);
        } catch (Exception ignored) {
            // metadata is auxiliary
        }
    }

    private String fileName(OperDto oper) {
        return oper.numOper() + " " + oper.name();
    }

    private String sourceBookmark(OperDto oper) {
        if (oper.funcs() == null || oper.funcs().isEmpty()) {
            return null;
        }
        var func = oper.funcs().getFirst();
        return func.id() == null ? null : "func_" + func.id() + "_name";
    }
}
