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

class RiDocEditorHeaderTest {

    static class TestRiDocEditor extends RiDocEditor {
        public TestRiDocEditor() {
            super("out/{0}/ri.docx", null, null);
        }

        public void exposedUpdateHeader(Document doc, Map<String, String> oldData, Map<String, String> newData) {
            super.updateHeader(doc, oldData, newData);
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getLastHeaderData() {
            return (Map<String, String>) ReflectionTestUtils.getField(this, "lastHeaderData");
        }
    }

    private TestRiDocEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TestRiDocEditor();
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
        Document doc = createTestDocument("Рабочая инструкция: {{d}}, Код: {{n}}");
        Map<String, String> oldData = Map.of("d", "старый_d", "n", "старый_n");
        Map<String, String> newData = Map.of("d", "Операция_РИ", "n", "РИ-001");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("Рабочая инструкция: Операция_РИ")
                .contains("Код: РИ-001")
                .doesNotContain("{{d}}")
                .doesNotContain("{{n}}");
    }

    @Test
    @DisplayName("updateHeader: oldVal заменяется на newVal, если они различаются")
    void updateHeader_oldValDifferentFromNewVal_replacesOldValWithNewVal() {
        Document doc = createTestDocument("РИ для: Станок_А и дата 2025");
        Map<String, String> oldData = Map.of("d", "Станок_А", "t", "2025");
        Map<String, String> newData = Map.of("d", "Станок_Б", "t", "2026");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("РИ для: Станок_Б и дата 2026")
                .doesNotContain("Станок_А")
                .doesNotContain("2025");
    }

    @Test
    @DisplayName("updateHeader: если oldVal == newVal, замена значения не происходит")
    void updateHeader_oldValEqualsNewVal_noValueReplacement() {
        Document doc = createTestDocument("РИ инструкция: СТАНДАРТ");
        Map<String, String> oldData = Map.of("d", "СТАНДАРТ");
        Map<String, String> newData = Map.of("d", "СТАНДАРТ");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText()).contains("РИ инструкция: СТАНДАРТ");
    }

    @Test
    @DisplayName("updateHeader: если newData.get(key) == null, плейсхолдер не заменяется")
    void updateHeader_newValIsNull_placeholderNotReplaced() {
        Document doc = createTestDocument("РИ: {{d}} и старое_наименование");
        Map<String, String> oldData = new HashMap<>();
        oldData.put("d", "старое_наименование");
        Map<String, String> newData = new HashMap<>();
        newData.put("d", null);

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(doc.getText())
                .contains("{{d}}")
                .contains("старое_наименование");
    }

    @Test
    @DisplayName("updateHeader: lastHeaderData сохраняет переданный newData")
    void updateHeader_storesNewDataInLastHeaderData() {
        Document doc = createTestDocument("Шапка РИ");
        Map<String, String> oldData = Map.of("d", "old");
        Map<String, String> newData = Map.of("d", "newRi", "n", "RI-05");

        editor.exposedUpdateHeader(doc, oldData, newData);

        assertThat(editor.getLastHeaderData()).isEqualTo(newData);
    }
}
