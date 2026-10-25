package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;

class SxPuWordGeneratorTest {

    private final Resource dummyResource = new ByteArrayResource(new byte[0]);
    private final SxPuWordGenerator generator = new SxPuWordGenerator(dummyResource, "out/{0}/test.docx");

    @Test
    @DisplayName("generate: если нет функций со спец. характеристиками, документ не создается (early return)")
    void generate_noSpecCharacteristics_exitsWithoutReadingTemplate() throws Exception {
        FuncDto f1 = new FuncDto(1L, "Точение", 1L, "D20", false, null);
        FuncDto f2 = new FuncDto(2L, "Контроль", 1L, "0.1", true, "   ");
        OperDto oper = new OperDto(1L, 1L, "005", "1", "o", "os", "n", "s", "1", List.of(f1, f2));
        DockPackageDto dto = new DockPackageDto(1L, "P", "p", "pu", "spu", "kp", "fm", "vi", List.of(oper), List.of());

        // Не должно выбрасывать исключений при пустом фиктивном шаблоне
        generator.generate(dto);
    }
}
