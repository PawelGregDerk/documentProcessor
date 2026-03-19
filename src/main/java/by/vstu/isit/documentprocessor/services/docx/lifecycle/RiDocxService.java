package by.vstu.isit.documentprocessor.services.docx.lifecycle;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.DocxDocumentService;
import by.vstu.isit.documentprocessor.services.docx.generate.RiWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.update.RiWordUpdater;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.util.HashMap;

@Service
@Order(100)
public class RiDocxService implements DocxDocumentService {
    private final RiWordGenerator generator;
    private final RiWordUpdater updater;

    public RiDocxService(RiWordGenerator generator, RiWordUpdater updater) {
        this.generator = generator;
        this.updater = updater;
    }

    @Override
    public void upsert(DockPackageDto dto) throws Exception {
        for (var oper : dto.opers()) {
            if (generator.hasGeneratedFile(dto, oper)) {
                updater.updateForOper(dto, oper, oper.name(), dto.path(), dto.path());
            } else {
                generator.generateForOper(dto, oper, dto.path());
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
        String copyPath = generator.copyPath(originalPath);
        boolean copyExists = Files.exists(generator.outputDir(copyPath).getParent());

        var byNumOper = new HashMap<String, String>();
        for (var oper : originalDto.opers()) {
            byNumOper.put(oper.numOper(), oper.name());
        }

        for (var oper : dto.opers()) {
            String oldName = byNumOper.get(oper.numOper());
            String targetPath = copyExists ? copyPath : originalPath;
            if (oldName != null && generator.existsAt(originalPath, oldName)) {
                updater.updateForOper(dto, oper, oldName, originalPath, targetPath);
            } else {
                generator.generateForOper(dto, oper, targetPath);
            }
        }
    }

}

