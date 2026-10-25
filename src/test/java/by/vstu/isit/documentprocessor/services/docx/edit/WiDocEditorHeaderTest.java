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

class WiDocEditorHeaderTest {

    static class TestWiDocEditor extends WiDocEditor {
        public TestWiDocEditor() {
            super("out/{0}/wi.docx");
        }

        public void exposedUpdateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
            super.updateHeader(doc, oldData, newData);
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getLastHeaderData() {
            return (Map<String, String>) ReflectionTestUtils.getField(this, "lastHeaderData");
        }
    }

    private TestWiDocEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TestWiDocEditor();
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
        Document doc = createTestDocument("Ведомость инструкций: {{d}}, Код: {{n}}");
        Map<String, String> oldData = Map.of("d", "старый_d", "n", "старый_n");
        Map<String, String> newData = Map.of("d", "ВИ_Изделия", "n", "ВИ-100");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("Ведомость инструкций: ВИ_Изделия")
                .contains("Код: ВИ-100")
                .doesNotContain("{{d}}")
                .doesNotContain("{{n}}");
    }

    @Test
    @DisplayName("updateHeader: oldVal заменяется на newVal, если они различаются")
    void updateHeader_oldValDifferentFromNewVal_replacesOldValWithNewVal() {
        Document doc = createTestDocument("ВИ для: Узел_10 и статус Черновой");
        Map<String, String> oldData = Map.of("d", "Узел_10", "s", "Черновой");
        Map<String, String> newData = Map.of("d", "Узел_20", "s", "Чистовой");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("ВИ для: Узел_20 и статус Чистовой")
                .doesNotContain("Узел_10")
                .doesNotContain("Черновой");
    }

    @Test
    @DisplayName("updateHeader: если oldVal == newVal, замена значения не происходит")
    void updateHeader_oldValEqualsNewVal_noValueReplacement() {
        Document doc = createTestDocument("Текст ВИ: БЕЗ_ИЗМЕНЕНИЙ");
        Map<String, String> oldData = Map.of("d", "БЕЗ_ИЗМЕНЕНИЙ");
        Map<String, String> newData = Map.of("d", "БЕЗ_ИЗМЕНЕНИЙ");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText()).contains("Текст ВИ: БЕЗ_ИЗМЕНЕНИЙ");
    }

    @Test
    @DisplayName("updateHeader: если newData.get(key) == null, плейсхолдер не заменяется")
    void updateHeader_newValIsNull_placeholderNotReplaced() {
        Document doc = createTestDocument("ВИ: {{d}} со старым текстом");
        Map<String, String> oldData = new HashMap<>();
        oldData.put("d", "со старым текстом");
        Map<String, String> newData = new HashMap<>();
        newData.put("d", null);

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("{{d}}")
                .contains("со старым текстом");
    }

    @Test
    @DisplayName("updateHeader: lastHeaderData сохраняет переданный newData")
    void updateHeader_storesNewDataInLastHeaderData() {
        Document doc = createTestDocument("Шапка ВИ");
        Map<String, String> oldData = Map.of("d", "old");
        Map<String, String> newData = Map.of("d", "newWi", "n", "WI-99");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(editor.getLastHeaderData()).isEqualTo(newData);
    }
}
