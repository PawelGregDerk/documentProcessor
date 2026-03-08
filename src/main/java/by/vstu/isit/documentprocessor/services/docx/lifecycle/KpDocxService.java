package by.vstu.isit.documentprocessor.services.docx.lifecycle;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.generate.KpWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.update.KpWordUpdater;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class KpDocxService extends AbstractDocxUpsertService {
    private final KpWordGenerator generator;
    private final KpWordUpdater updater;
    
    public KpDocxService(KpWordGenerator generator, KpWordUpdater updater) {
        super(generator, updater);
        this.generator = generator;
        this.updater = updater;
    }
    
    @Override
    protected boolean hasNameChanged(DockPackageDto dto, DockPackageDto originalDto) {
        return !dto.kpName().equals(originalDto.kpName());
    }
    
    @Override
    protected void handleNameChange(DockPackageDto dto, DockPackageDto originalDto) throws Exception {
        Path originalPath = generator.resolveOutPath(dto.path(), originalDto.kpName());
        Path copyPath = generator.resolveOutPath(generator.copyPath(dto.path()), dto.kpName());
        
        if (Files.exists(originalPath)) {
            Files.createDirectories(copyPath.getParent());
            Files.copy(originalPath, copyPath, StandardCopyOption.REPLACE_EXISTING);
            updater.updateWithRename(dto, originalDto, copyPath);
        } else {
            generator.generate(dto);
        }
    }
}

