package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import com.spire.doc.Document;
import com.spire.doc.Section;
import com.spire.doc.documents.Paragraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderUpdateHelperTest {

    static class TestEditor extends AbstractDocEditor {
        TestEditor() {
            super("src/{0}", "dest/{0}");
        }

        @Override
        public void edit(DockPackageDto dto, DockPackageDto savedDto) {
        }
    }

    @Test
    @DisplayName("update: заменяет плейсхолдеры и старые значения")
    void update_replacesPlaceholdersAndOldValues() {
        Document doc = new Document();
        Section section = doc.addSection();
        Paragraph paragraph = section.addParagraph();
        paragraph.appendText("Проект {{d}}, объект: Старый");
        TestEditor editor = new TestEditor();

        HeaderUpdateHelper.update(editor, doc,
                Map.of("d", "Старый"),
                Map.of("d", "Новый"));

        assertThat(doc.getText())
                .contains("Проект Новый")
                .contains("объект: Новый")
                .doesNotContain("{{d}}")
                .doesNotContain("Старый");
    }
}
