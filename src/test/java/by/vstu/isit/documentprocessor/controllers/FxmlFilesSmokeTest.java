package by.vstu.isit.documentprocessor.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FxmlFilesSmokeTest {

    private static final List<String> FXML_FILES = List.of(
            "main-view.fxml",
            "docpackage-view.fxml",
            "docpackage-edit-view.fxml",
            "package-list-view.fxml",
            "type-oper-list-view.fxml",
            "type-oper-edit-view.fxml"
    );

    @Test
    @DisplayName("FXML: все экраны корректны, контроллеры существуют, fx:id уникальны")
    void allFxmlFiles_haveExistingControllersAndUniqueIds() throws Exception {
        for (String fileName : FXML_FILES) {
            URL url = getClass().getResource(
                    "/by/vstu/isit/documentprocessor/controllers/" + fileName);
            assertThat(url).as("FXML не найден: %s", fileName).isNotNull();

            Document document;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            try (InputStream in = url.openStream()) {
                document = factory.newDocumentBuilder().parse(in);
            }

            String controller = document.getDocumentElement().getAttribute("fx:controller");
            assertThat(controller).as("fx:controller отсутствует в %s", fileName).isNotBlank();
            assertThat(Class.forName(controller)).isNotNull();

            Set<String> ids = new HashSet<>();
            NodeList nodes = document.getElementsByTagName("*");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                if (element.hasAttribute("fx:id")) {
                    String id = element.getAttribute("fx:id");
                    assertThat(ids.add(id))
                            .as("Повторяющийся fx:id '%s' в %s", id, fileName)
                            .isTrue();
                }
            }
        }
    }
}
