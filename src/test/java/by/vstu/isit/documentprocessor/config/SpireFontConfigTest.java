package by.vstu.isit.documentprocessor.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SpireFontConfigTest {

    @Test
    @DisplayName("registerFonts: executes without throwing exceptions")
    void registerFonts_execution_doesNotThrow() {
        SpireFontConfig config = new SpireFontConfig();
        assertDoesNotThrow(config::registerFonts);
    }
}
