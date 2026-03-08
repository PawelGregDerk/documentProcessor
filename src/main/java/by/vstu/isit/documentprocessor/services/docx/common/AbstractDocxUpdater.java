package by.vstu.isit.documentprocessor.services.docx.common;

import by.vstu.isit.documentprocessor.services.docx.DocxDocumentUpdater;
import org.springframework.core.io.Resource;

public abstract class AbstractDocxUpdater extends AbstractDocxTemplateProcessor implements DocxDocumentUpdater {
    protected AbstractDocxUpdater(Resource inpPath, String tmpOut, String outPath) {
        super(inpPath, tmpOut, outPath);
    }
}

