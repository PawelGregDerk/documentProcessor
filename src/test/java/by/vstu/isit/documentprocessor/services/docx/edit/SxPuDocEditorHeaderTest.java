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

class SxPuDocEditorHeaderTest {

    static class TestSxPuDocEditor extends SxPuDocEditor {
        public TestSxPuDocEditor() {
            super("out/{0}/sxpu.docx");
        }

        public void exposedUpdateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
            super.updateHeader(doc, oldData, newData);
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getLastHeaderData() {
            return (Map<String, String>) ReflectionTestUtils.getField(this, "lastHeaderData");
        }
    }

    private TestSxPuDocEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TestSxPuDocEditor();
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
        Document doc = createTestDocument("СХПУ документ: {{d}}, Код: {{n}}");
        Map<String, String> oldData = Map.of("d", "старый_d", "n", "старый_n");
        Map<String, String> newData = Map.of("d", "СпецХарактеристики", "n", "СХ-01");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("СХПУ документ: СпецХарактеристики")
                .contains("Код: СХ-01")
                .doesNotContain("{{d}}")
                .doesNotContain("{{n}}");
    }

    @Test
    @DisplayName("updateHeader: oldVal заменяется на newVal, если они различаются")
    void updateHeader_oldValDifferentFromNewVal_replacesOldValWithNewVal() {
        Document doc = createTestDocument("СХПУ для детали: ДЕТАЛЬ_А и параметр П1");
        Map<String, String> oldData = Map.of("d", "ДЕТАЛЬ_А", "p", "П1");
        Map<String, String> newData = Map.of("d", "ДЕТАЛЬ_Б", "p", "П2");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("СХПУ для детали: ДЕТАЛЬ_Б и параметр П2")
                .doesNotContain("ДЕТАЛЬ_А")
                .doesNotContain("П1");
    }

    @Test
    @DisplayName("updateHeader: если oldVal == newVal, замена значения не происходит")
    void updateHeader_oldValEqualsNewVal_noValueReplacement() {
        Document doc = createTestDocument("СХПУ постоянный текст: ПАРАМЕТР");
        Map<String, String> oldData = Map.of("d", "ПАРАМЕТР");
        Map<String, String> newData = Map.of("d", "ПАРАМЕТР");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText()).contains("СХПУ постоянный текст: ПАРАМЕТР");
    }

    @Test
    @DisplayName("updateHeader: если newData.get(key) == null, плейсхолдер не заменяется")
    void updateHeader_newValIsNull_placeholderNotReplaced() {
        Document doc = createTestDocument("СХПУ: {{d}} и старое_значение");
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
        Document doc = createTestDocument("Шапка СХПУ");
        Map<String, String> oldData = Map.of("d", "old");
        Map<String, String> newData = Map.of("d", "newSxPu", "n", "SX-007");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(editor.getLastHeaderData()).isEqualTo(newData);
    }
}
