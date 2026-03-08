package by.vstu.isit.documentprocessor.services.docx;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;

public interface DocxDocumentService {
    default void upsert(DockPackageDto dto) throws Exception {
        upsert(dto, null);
    }

    void upsert(DockPackageDto dto, DockPackageDto originalDto) throws Exception;
}

