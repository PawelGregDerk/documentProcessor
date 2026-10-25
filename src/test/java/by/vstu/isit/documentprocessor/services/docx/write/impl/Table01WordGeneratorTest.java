package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Table01WordGeneratorTest {

    private final Resource dummyResource = new ByteArrayResource(new byte[0]);
    private final Table01WordGenerator generator = new Table01WordGenerator(dummyResource, "out/{0}/test.docx");

    @Test
    @DisplayName("fillFirstCell: если в обозначении нет разделителя '--', возвращает исходное имя")
    void fillFirstCell_noDoubleDash_returnsOriginalName() {
        SborEdDto dto = new SborEdDto(1L, 1L, "Деталь", "АБВГ.123456.001");

        String result = generator.fillFirstCell("Втулка", dto);

        assertThat(result).isEqualTo("Втулка");
    }

    @Test
    @DisplayName("fillFirstCell: разделитель '--', first == '0', last == '0' -> name-name")
    void fillFirstCell_firstZeroLastZero_formatsCorrectly() {
        SborEdDto dto = new SborEdDto(1L, 1L, "Деталь", "АБВ0--ГДЕ0");

        String result = generator.fillFirstCell("Втулка", dto);

        assertThat(result).isEqualTo("Втулка-Втулка");
    }

    @Test
    @DisplayName("fillFirstCell: разделитель '--', first != '0', last != '0' -> name-first-name-last")
    void fillFirstCell_firstNonZeroLastNonZero_formatsCorrectly() {
        SborEdDto dto = new SborEdDto(1L, 1L, "Деталь", "АБВ1--ГДЕ2");

        String result = generator.fillFirstCell("Корпус", dto);

        assertThat(result).isEqualTo("Корпус-1-Корпус-2");
    }

    @Test
    @DisplayName("generate: если сборочных единиц <= 1, генерация не выполняется")
    void generate_singleOrEmptySborEd_doesNotThrowException() throws Exception {
        DockPackageDto dtoSingle = new DockPackageDto(1L, "P", "p", "pu", "spu", "kp", "fm", "vi", List.of(), List.of(new SborEdDto(1L, 1L, "N", "O")));
        DockPackageDto dtoEmpty = new DockPackageDto(1L, "P", "p", "pu", "spu", "kp", "fm", "vi", List.of(), List.of());

        // Метод должен завершиться досрочно без попытки чтения ресурса
        generator.generate(dtoSingle);
        generator.generate(dtoEmpty);
    }
}
