package by.vstu.isit.documentprocessor.services.docx.lifecycle;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.DocxDocumentService;
import by.vstu.isit.documentprocessor.services.docx.generate.RiWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.update.RiWordUpdater;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

@Service
public class RiDocxService implements DocxDocumentService {
    private final RiWordGenerator generator;
    private final RiWordUpdater updater;

    public RiDocxService(RiWordGenerator generator, RiWordUpdater updater) {
        this.generator = generator;
        this.updater = updater;
    }

    @Override
    public void upsert(DockPackageDto dto) throws Exception {
        boolean hasAnyOriginal = hasAnyOriginalRi(dto.path());
        for (var oper : dto.opers()) {
            if (generator.hasGeneratedFile(dto, oper)) {
                updater.updateForOper(dto, oper);
            } else {
                String basePath = hasAnyOriginal ? copyPath(dto.path()) : dto.path();
                generator.generateForOper(dto, oper, basePath);
            }
        }
    }

    @Override
    public void upsert(DockPackageDto dto, DockPackageDto originalDto) throws Exception {
        if (originalDto == null) {
            upsert(dto);
            return;
        }
        String originalPath = originalDto.path();
        boolean hasAnyOriginal = hasAnyOriginalRi(originalPath);
        String targetBasePath = hasAnyOriginal ? copyPath(originalPath) : originalPath;

        var byNumOper = new HashMap<String, String>();
        for (var oper : originalDto.opers()) {
            byNumOper.put(oper.numOper(), oper.name());
        }

        for (var oper : dto.opers()) {
            String oldName = byNumOper.get(oper.numOper());
            if (oldName != null && !oldName.equals(oper.name()) && generator.existsAt(originalPath, oldName)) {
                updater.updateForOper(dto, oper, oldName, targetBasePath);
                continue;
            }

            if (oldName != null && generator.existsAt(originalPath, oldName)) {
                updater.updateForOper(dto, oper);
            } else if (hasAnyOriginal) {
                generator.generateForOper(dto, oper, targetBasePath);
            } else {
                generator.generateForOper(dto, oper, originalPath);
            }
        }
    }

    private boolean hasAnyOriginalRi(String basePath) {
        Path dir = generator.outputDir(basePath);
        if (dir == null || !Files.exists(dir)) {
            return false;
        }
        try (var stream = Files.list(dir)) {
            return stream.anyMatch(path -> path.toString().toLowerCase().endsWith(".docx"));
        } catch (IOException ex) {
            return false;
        }
    }

    private String copyPath(String basePath) {
        return "копия" + basePath;
    }
}

