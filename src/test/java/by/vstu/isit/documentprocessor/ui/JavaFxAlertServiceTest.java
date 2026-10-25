package by.vstu.isit.documentprocessor.ui;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaFxAlertServiceTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Already initialized
        }
    }

    @Test
    @DisplayName("JavaFxAlertService: реализует AlertService и создается без ошибок")
    void alertService_implementsInterface() {
        JavaFxAlertService service = new JavaFxAlertService();
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(AlertService.class);
    }
}
