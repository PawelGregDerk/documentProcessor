package by.vstu.isit.documentprocessor.services.docx.lifecycle;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.generate.PuWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.update.PuWordUpdater;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class PuDocxService extends AbstractDocxUpsertService {
    private final PuWordGenerator generator;
    private final PuWordUpdater updater;
    
    public PuDocxService(PuWordGenerator generator, PuWordUpdater updater) {
        super(generator, updater);
        this.generator = generator;
        this.updater = updater;
    }
    
    @Override
    protected boolean hasNameChanged(DockPackageDto dto, DockPackageDto originalDto) {
        return !dto.puName().equals(originalDto.puName());
    }
    
    @Override
    protected void handleNameChange(DockPackageDto dto, DockPackageDto originalDto) throws Exception {
        Path originalPath = generator.resolveOutPath(dto.path(), originalDto.puName());
        Path copyPath = generator.resolveOutPath(generator.copyPath(dto.path()), dto.puName());
        
        if (Files.exists(originalPath)) {
            Files.createDirectories(copyPath.getParent());
            Files.copy(originalPath, copyPath, StandardCopyOption.REPLACE_EXISTING);
            updater.updateWithRename(dto, originalDto, copyPath);
        } else {
            generator.generate(dto);
        }
    }
}

