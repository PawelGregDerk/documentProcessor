package by.vstu.isit.documentprocessor.services.docx.lifecycle;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.generate.WiWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.update.WiWordUpdater;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class WiDocxService extends AbstractDocxUpsertService {
    private final WiWordGenerator generator;
    private final WiWordUpdater updater;
    
    public WiDocxService(WiWordGenerator generator, WiWordUpdater updater) {
        super(generator, updater);
        this.generator = generator;
        this.updater = updater;
    }
    
    @Override
    protected boolean hasNameChanged(DockPackageDto dto, DockPackageDto originalDto) {
        return !dto.vedIName().equals(originalDto.vedIName());
    }
    
    @Override
    protected void handleNameChange(DockPackageDto dto, DockPackageDto originalDto) throws Exception {
        Path originalPath = generator.resolveOutPath(dto.path(), originalDto.vedIName());
        Path copyPath = generator.resolveOutPath(generator.copyPath(dto.path()), dto.vedIName());
        
        if (Files.exists(originalPath)) {
            Files.createDirectories(copyPath.getParent());
            Files.copy(originalPath, copyPath, StandardCopyOption.REPLACE_EXISTING);
            updater.updateWithRename(dto, originalDto, copyPath);
        } else {
            generator.generate(dto);
        }
    }
}

