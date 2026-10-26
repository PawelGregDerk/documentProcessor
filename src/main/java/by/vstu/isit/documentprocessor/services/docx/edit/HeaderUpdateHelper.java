package by.vstu.isit.documentprocessor.services.docx.edit;

import com.spire.doc.Document;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class HeaderUpdateHelper {

    public void update(AbstractDocEditor editor,
                       Document document,
                       Map<String, String> oldFallback,
                       Map<String, String> newData) {
        editor.updateHeader(document, editor.resolveHeaderOldData(oldFallback), newData);
    }
}
