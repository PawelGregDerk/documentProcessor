package by.vstu.isit.documentprocessor.services.docx.common;

import by.vstu.isit.documentprocessor.services.docx.DocxDocumentGenerator;
import org.springframework.core.io.Resource;

import java.nio.file.Path;

public abstract class AbstractDocxGenerator extends AbstractDocxTemplateProcessor implements DocxDocumentGenerator {
    protected AbstractDocxGenerator(Resource inpPath, String tmpOut, String outPath) {
        super(inpPath, tmpOut, outPath);
    }
    
    public Path resolveOutPath(String basePath, String name) {
        return super.resolveOutPath(basePath, name);
    }
    
    public String copyPath(String basePath) {
        return super.copyPath(basePath);
    }
}

