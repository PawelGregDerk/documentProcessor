package by.vstu.isit.documentprocessor.services.docx.write.abstracts;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractWordGeneratorTest {

    private static class TestGenerator extends AbstractWordGenerator {
        protected TestGenerator() {
            super(new ByteArrayResource(new byte[0]), "out/{0}/{1}.docx");
        }

        @Override
        public void generate(DockPackageDto dto) {
            // no-op
        }
    }

    private final TestGenerator generator = new TestGenerator();

    @Test
    @DisplayName("designationsAssemblyUnit: если сборочных единиц > 1, возвращает 'согласно таблице 1'")
    void designationsAssemblyUnit_multipleItems_returnsAccordingToTable1() {
        List<SborEdDto> list = List.of(
                new SborEdDto(1L, 1L, "Деталь 1", "001"),
                new SborEdDto(2L, 1L, "Деталь 2", "002")
        );

        String result = generator.designationsAssemblyUnit(list);

        assertThat(result).isEqualTo("согласно таблице 1");
    }

    @Test
    @DisplayName("designationsAssemblyUnit: если одна сборочная единица, возвращает ее обозначение")
    void designationsAssemblyUnit_singleItem_returnsOboznach() {
        List<SborEdDto> list = List.of(new SborEdDto(1L, 1L, "Деталь", "АБВГ.123"));

        String result = generator.designationsAssemblyUnit(list);

        assertThat(result).isEqualTo("АБВГ.123");
    }

    @Test
    @DisplayName("dseText: если сборочных единиц > 1, возвращает 'Таблица 01'")
    void dseText_multipleItems_returnsTable01() {
        List<SborEdDto> list = List.of(
                new SborEdDto(1L, 1L, "Деталь 1", "001"),
                new SborEdDto(2L, 1L, "Деталь 2", "002")
        );

        String result = generator.dseText(list, "АБВГ.000");

        assertThat(result).isEqualTo("Таблица 01");
    }

    @Test
    @DisplayName("dseText: если одна сборочная единица, возвращает переданное значение по умолчанию")
    void dseText_singleItem_returnsDefaultSingleValue() {
        List<SborEdDto> list = List.of(new SborEdDto(1L, 1L, "Деталь", "001"));

        String result = generator.dseText(list, "ОбозначениеИзделия");

        assertThat(result).isEqualTo("ОбозначениеИзделия");
    }
}
