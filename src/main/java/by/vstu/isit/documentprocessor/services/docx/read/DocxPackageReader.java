package by.vstu.isit.documentprocessor.services.docx.read;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.test.TestDockPackageFactory;
import org.springframework.stereotype.Service;

//import java.io.File;

@Service
public class DocxPackageReader {
    public DockPackageDto read(/*File file*/) {
        return new TestDockPackageFactory().createTestDto();
    }
}
