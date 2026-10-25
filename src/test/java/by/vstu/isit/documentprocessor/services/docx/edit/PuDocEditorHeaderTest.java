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

class PuDocEditorHeaderTest {

    static class TestPuDocEditor extends PuDocEditor {
        public TestPuDocEditor() {
            super("out/{0}/pu.docx");
        }

        public void exposedUpdateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
            super.updateHeader(doc, oldData, newData);
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getLastHeaderData() {
            return (Map<String, String>) ReflectionTestUtils.getField(this, "lastHeaderData");
        }
    }

    private TestPuDocEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TestPuDocEditor();
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
        Document doc = createTestDocument("Документ: {{d}}, Номер: {{n}}");
        Map<String, String> oldData = Map.of("d", "старый_d", "n", "старый_n");
        Map<String, String> newData = Map.of("d", "Новая Деталь", "n", "12345");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("Документ: Новая Деталь")
                .contains("Номер: 12345")
                .doesNotContain("{{d}}")
                .doesNotContain("{{n}}");
    }

    @Test
    @DisplayName("updateHeader: oldVal заменяется на newVal, если они различаются")
    void updateHeader_oldValDifferentFromNewVal_replacesOldValWithNewVal() {
        Document doc = createTestDocument("Текущий артикул: АБВГ.123456 и версия 1.0");
        Map<String, String> oldData = Map.of("d", "АБВГ.123456", "v", "1.0");
        Map<String, String> newData = Map.of("d", "ДЕЖЗ.654321", "v", "2.0");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("Текущий артикул: ДЕЖЗ.654321 и версия 2.0")
                .doesNotContain("АБВГ.123456")
                .doesNotContain("1.0");
    }

    @Test
    @DisplayName("updateHeader: если oldVal == newVal, замена значения не происходит")
    void updateHeader_oldValEqualsNewVal_noValueReplacement() {
        Document doc = createTestDocument("Артикул: АБВГ.123456");
        Map<String, String> oldData = Map.of("d", "АБВГ.123456");
        Map<String, String> newData = Map.of("d", "АБВГ.123456");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText()).contains("Артикул: АБВГ.123456");
    }

    @Test
    @DisplayName("updateHeader: если newData.get(key) == null, плейсхолдер не заменяется")
    void updateHeader_newValIsNull_placeholderNotReplaced() {
        Document doc = createTestDocument("Документ: {{d}} и значение старый_d");
        Map<String, String> oldData = new HashMap<>();
        oldData.put("d", "старый_d");
        Map<String, String> newData = new HashMap<>();
        newData.put("d", null);

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("{{d}}")
                .contains("старый_d");
    }

    @Test
    @DisplayName("updateHeader: lastHeaderData сохраняет переданный newData")
    void updateHeader_storesNewDataInLastHeaderData() {
        Document doc = createTestDocument("Шапка");
        Map<String, String> oldData = Map.of("d", "old");
        Map<String, String> newData = Map.of("d", "newVal", "n", "num1");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(editor.getLastHeaderData()).isEqualTo(newData);
    }
}
