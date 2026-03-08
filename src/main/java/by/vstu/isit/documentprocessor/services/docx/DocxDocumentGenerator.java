package by.vstu.isit.documentprocessor.services.docx;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;

public interface DocxDocumentGenerator {
    void generate(DockPackageDto dto) throws Exception;

    boolean hasGeneratedFiles(DockPackageDto dto);
}

