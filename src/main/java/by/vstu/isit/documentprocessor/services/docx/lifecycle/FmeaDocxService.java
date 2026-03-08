package by.vstu.isit.documentprocessor.services.docx.lifecycle;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.generate.FmeaWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.update.FmeaWordUpdater;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class FmeaDocxService extends AbstractDocxUpsertService {
    private final FmeaWordGenerator generator;
    private final FmeaWordUpdater updater;
    
    public FmeaDocxService(FmeaWordGenerator generator, FmeaWordUpdater updater) {
        super(generator, updater);
        this.generator = generator;
        this.updater = updater;
    }
    
    @Override
    protected boolean hasNameChanged(DockPackageDto dto, DockPackageDto originalDto) {
        return !dto.fmeaName().equals(originalDto.fmeaName());
    }
    
    @Override
    protected void handleNameChange(DockPackageDto dto, DockPackageDto originalDto) throws Exception {
        Path originalPath = generator.resolveOutPath(dto.path(), originalDto.fmeaName());
        Path copyPath = generator.resolveOutPath(generator.copyPath(dto.path()), dto.fmeaName());
        
        if (Files.exists(originalPath)) {
            Files.createDirectories(copyPath.getParent());
            Files.copy(originalPath, copyPath, StandardCopyOption.REPLACE_EXISTING);
            updater.updateWithRename(dto, originalDto, copyPath);
        } else {
            generator.generate(dto);
        }
    }
}

