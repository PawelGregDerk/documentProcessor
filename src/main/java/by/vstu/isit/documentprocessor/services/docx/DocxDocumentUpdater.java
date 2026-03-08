package by.vstu.isit.documentprocessor.services.docx;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;

import java.nio.file.Path;

public interface DocxDocumentUpdater {
    void update(DockPackageDto dto) throws Exception;
    
    default void updateWithRename(DockPackageDto dto, DockPackageDto originalDto, Path targetPath) throws Exception {
        update(dto);
    }
}

