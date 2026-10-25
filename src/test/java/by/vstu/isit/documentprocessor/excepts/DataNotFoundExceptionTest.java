package by.vstu.isit.documentprocessor.excepts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataNotFoundExceptionTest {

    @Test
    @DisplayName("конструктор с сообщением: сохраняет переданный текст")
    void constructor_customMessage_storesMessage() {
        DataNotFoundException ex = new DataNotFoundException("Пакет не найден");

        assertThat(ex.getMessage()).isEqualTo("Пакет не найден");
    }

    @Test
    @DisplayName("конструктор по умолчанию: подтягивает локализованное сообщение из LocalizeHelper")
    void defaultConstructor_defaultMessage_loadsFromLocalizeHelper() {
        DataNotFoundException ex = new DataNotFoundException();

        assertThat(ex.getMessage()).isNotNull();
        assertThat(ex.getMessage()).isNotEmpty();
    }
}
