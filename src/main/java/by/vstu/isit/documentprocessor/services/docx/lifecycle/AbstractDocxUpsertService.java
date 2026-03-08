package by.vstu.isit.documentprocessor.services.docx.lifecycle;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.DocxDocumentGenerator;
import by.vstu.isit.documentprocessor.services.docx.DocxDocumentService;
import by.vstu.isit.documentprocessor.services.docx.DocxDocumentUpdater;

public abstract class AbstractDocxUpsertService implements DocxDocumentService {
    private final DocxDocumentGenerator generator;
    private final DocxDocumentUpdater updater;

    protected AbstractDocxUpsertService(DocxDocumentGenerator generator, DocxDocumentUpdater updater) {
        this.generator = generator;
        this.updater = updater;
    }

    @Override
    public void upsert(DockPackageDto dto) throws Exception {
        if (generator.hasGeneratedFiles(dto)) {
            updater.update(dto);
        } else {
            generator.generate(dto);
        }
    }

    @Override
    public void upsert(DockPackageDto dto, DockPackageDto originalDto) throws Exception {
        if (originalDto != null && hasNameChanged(dto, originalDto)) {
            handleNameChange(dto, originalDto);
        } else {
            upsert(dto);
        }
    }

    protected abstract boolean hasNameChanged(DockPackageDto dto, DockPackageDto originalDto);
    
    protected abstract void handleNameChange(DockPackageDto dto, DockPackageDto originalDto) throws Exception;
}

