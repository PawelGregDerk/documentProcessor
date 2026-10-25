package by.vstu.isit.documentprocessor.services.docx.edit;

import com.spire.doc.Document;
import com.spire.doc.Section;
import com.spire.doc.documents.Paragraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FmeaDocEditorHeaderTest {

    static class TestFmeaDocEditor extends FmeaDocEditor {
        public TestFmeaDocEditor() {
            super("out/{0}/fmea.docx");
        }

        public void exposedUpdateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
            super.updateHeader(doc, oldData, newData);
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getLastHeaderData() {
            return (Map<String, String>) ReflectionTestUtils.getField(this, "lastHeaderData");
        }
    }

    private TestFmeaDocEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TestFmeaDocEditor();
    }

    private Document createTestDocument(String initialText) {
        Document doc = new Document();
        Section section = doc.addSection();
        Paragraph paragraph = section.addParagraph();
        paragraph.appendText(initialText);
        return doc;
    }

    @Test
    @DisplayName("updateHeader: плейсхолдер {{key}} успешно заменяется на newVal")
    void updateHeader_placeholderPresent_replacesWithNewVal() {
        Document doc = createTestDocument("FMEA анализ: {{d}}, Шифр: {{n}}");
        Map<String, String> oldData = Map.of("d", "старый_d", "n", "старый_n");
        Map<String, String> newData = Map.of("d", "FMEA Узла", "n", "FM-01");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("FMEA анализ: FMEA Узла")
                .contains("Шифр: FM-01")
                .doesNotContain("{{d}}")
                .doesNotContain("{{n}}");
    }

    @Test
    @DisplayName("updateHeader: oldVal заменяется на newVal, если они различаются")
    void updateHeader_oldValDifferentFromNewVal_replacesOldValWithNewVal() {
        Document doc = createTestDocument("Объект анализа: Узел_А и статус Черновик");
        Map<String, String> oldData = Map.of("d", "Узел_А", "s", "Черновик");
        Map<String, String> newData = Map.of("d", "Узел_Б", "s", "Утвержден");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("Объект анализа: Узел_Б и статус Утвержден")
                .doesNotContain("Узел_А")
                .doesNotContain("Черновик");
    }

    @Test
    @DisplayName("updateHeader: если oldVal == newVal, замена значения не происходит")
    void updateHeader_oldValEqualsNewVal_noValueReplacement() {
        Document doc = createTestDocument("Процесс: Сборка узла");
        Map<String, String> oldData = Map.of("d", "Сборка узла");
        Map<String, String> newData = Map.of("d", "Сборка узла");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText()).contains("Процесс: Сборка узла");
    }

    @Test
    @DisplayName("updateHeader: если newData.get(key) == null, плейсхолдер не заменяется")
    void updateHeader_newValIsNull_placeholderNotReplaced() {
        Document doc = createTestDocument("FMEA: {{d}} со значением старое_значение");
        Map<String, String> oldData = new HashMap<>();
        oldData.put("d", "старое_значение");
        Map<String, String> newData = new HashMap<>();
        newData.put("d", null);

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("{{d}}")
                .contains("старое_значение");
    }

    @Test
    @DisplayName("updateHeader: lastHeaderData сохраняет переданный newData")
    void updateHeader_storesNewDataInLastHeaderData() {
        Document doc = createTestDocument("FMEA Шапка");
        Map<String, String> oldData = Map.of("d", "old");
        Map<String, String> newData = Map.of("d", "newFmea", "p", "План_1");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(editor.getLastHeaderData()).isEqualTo(newData);
    }
}
