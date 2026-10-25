package by.vstu.isit.documentprocessor.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class FileUtilsTest {

    @Test
    @DisplayName("sanitize: при передаче null должен возвращать null")
    void sanitize_nullInput_returnsNull() {
        String result = FileUtils.sanitize(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("sanitize: при передаче пустой строки возвращает пустую строку")
    void sanitize_emptyString_returnsEmptyString() {
        String result = FileUtils.sanitize("");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("sanitize: строка без запрещённых символов не изменяется")
    void sanitize_validString_returnsSameString() {
        String input = "Document-01.final_version";
        String result = FileUtils.sanitize(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    @DisplayName("sanitize: заменяет все спецсимволы [/\\:*?\"<>|] на символ подчеркивания")
    void sanitize_allProhibitedChars_allReplacedWithUnderscores() {
        String input = "/\\:*?\"<>|";
        String result = FileUtils.sanitize(input);

        assertThat(result).isEqualTo("_________");
    }

    @Test
    @DisplayName("sanitize: сохраняет кириллицу, пробелы и разрешенные спецсимволы")
    void sanitize_cyrillicWithSpaces_preserved() {
        String input = "Чертеж изделия №123-05 (копия)";
        String result = FileUtils.sanitize(input);

        assertThat(result).isEqualTo("Чертеж изделия №123-05 (копия)");
    }

    @ParameterizedTest
    @ValueSource(strings = {"test/name", "test\\name", "test:name", "test*name", "test?name", "test\"name", "test<name", "test>name", "test|name"})
    @DisplayName("sanitize: каждый запрещенный символ заменяется на _")
    void sanitize_singleProhibitedChar_replacedWithUnderscore(String input) {
        String result = FileUtils.sanitize(input);

        assertThat(result).isEqualTo("test_name");
    }
}
