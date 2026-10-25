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

class KpDocEditorHeaderTest {

    static class TestKpDocEditor extends KpDocEditor {
        public TestKpDocEditor() {
            super("out/{0}/kp.docx");
        }

        public void exposedUpdateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
            super.updateHeader(doc, oldData, newData);
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getLastHeaderData() {
            return (Map<String, String>) ReflectionTestUtils.getField(this, "lastHeaderData");
        }
    }

    private TestKpDocEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TestKpDocEditor();
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
        Document doc = createTestDocument("Контрольный план: {{d}}, Код: {{n}}");
        Map<String, String> oldData = Map.of("d", "старый_d", "n", "старый_n");
        Map<String, String> newData = Map.of("d", "Деталь_КП", "n", "КП-2026");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("Контрольный план: Деталь_КП")
                .contains("Код: КП-2026")
                .doesNotContain("{{d}}")
                .doesNotContain("{{n}}");
    }

    @Test
    @DisplayName("updateHeader: oldVal заменяется на newVal, если они различаются")
    void updateHeader_oldValDifferentFromNewVal_replacesOldValWithNewVal() {
        Document doc = createTestDocument("КП изд: ИЗД_1 и ревизия rev1");
        Map<String, String> oldData = Map.of("d", "ИЗД_1", "r", "rev1");
        Map<String, String> newData = Map.of("d", "ИЗД_2", "r", "rev2");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("КП изд: ИЗД_2 и ревизия rev2")
                .doesNotContain("ИЗД_1")
                .doesNotContain("rev1");
    }

    @Test
    @DisplayName("updateHeader: если oldVal == newVal, замена значения не происходит")
    void updateHeader_oldValEqualsNewVal_noValueReplacement() {
        Document doc = createTestDocument("КП статический текст: ЗНАЧЕНИЕ");
        Map<String, String> oldData = Map.of("d", "ЗНАЧЕНИЕ");
        Map<String, String> newData = Map.of("d", "ЗНАЧЕНИЕ");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText()).contains("КП статический текст: ЗНАЧЕНИЕ");
    }

    @Test
    @DisplayName("updateHeader: если newData.get(key) == null, плейсхолдер не заменяется")
    void updateHeader_newValIsNull_placeholderNotReplaced() {
        Document doc = createTestDocument("КП: {{d}} и старый_текст");
        Map<String, String> oldData = new HashMap<>();
        oldData.put("d", "старый_текст");
        Map<String, String> newData = new HashMap<>();
        newData.put("d", null);

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("{{d}}")
                .contains("старый_текст");
    }

    @Test
    @DisplayName("updateHeader: lastHeaderData сохраняет переданный newData")
    void updateHeader_storesNewDataInLastHeaderData() {
        Document doc = createTestDocument("Шапка КП");
        Map<String, String> oldData = Map.of("d", "old");
        Map<String, String> newData = Map.of("d", "newKp", "n", "KP-01");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(editor.getLastHeaderData()).isEqualTo(newData);
    }
}
