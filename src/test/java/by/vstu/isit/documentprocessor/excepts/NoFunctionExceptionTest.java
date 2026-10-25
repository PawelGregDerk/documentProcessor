package by.vstu.isit.documentprocessor.excepts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoFunctionExceptionTest {

    @Test
    @DisplayName("конструктор с произвольным сообщением: сохраняет текст")
    void constructor_customMessage_storesMessage() {
        NoFunctionException ex = new NoFunctionException("Ошибка: нет функций");

        assertThat(ex.getMessage()).isEqualTo("Ошибка: нет функций");
    }

    @Test
    @DisplayName("конструктор с номером и наименованием операции: форматирует сообщение с параметрами")
    void constructor_numAndName_formatsMessage() {
        NoFunctionException ex = new NoFunctionException("015", "Сварка");

        assertThat(ex.getMessage()).isNotNull();
        assertThat(ex.getMessage()).contains("015");
        assertThat(ex.getMessage()).contains("Сварка");
    }
}
