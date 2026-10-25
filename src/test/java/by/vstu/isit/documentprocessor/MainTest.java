package by.vstu.isit.documentprocessor;

import javafx.application.Application;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    @DisplayName("Main class configuration: is annotated with @SpringBootApplication and extends JavaFX Application")
    void mainClass_annotationsAndInheritance_correct() {
        assertTrue(Application.class.isAssignableFrom(Main.class));

        SpringBootApplication annotation = Main.class.getAnnotation(SpringBootApplication.class);
        assertNotNull(annotation, "Main should be annotated with @SpringBootApplication");
    }
}
