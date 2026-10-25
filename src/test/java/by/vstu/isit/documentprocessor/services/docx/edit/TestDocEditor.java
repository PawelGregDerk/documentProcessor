package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import com.spire.doc.Document;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

public class TestDocEditor extends AbstractDocEditor {

    public TestDocEditor(String path) {
        super(path, path);
    }

    public TestDocEditor() {
        super("src/{0}/{1}", "dest/{0}/{1}");
    }

    @Override
    public void edit(DockPackageDto dto, DockPackageDto savedDto) {
        // no-op for characterization tests
    }

    public void exposedUpdateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
        super.updateHeader(doc, oldData, newData);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getLastHeaderData() {
        return (Map<String, String>) ReflectionTestUtils.getField(this, "lastHeaderData");
    }
}
