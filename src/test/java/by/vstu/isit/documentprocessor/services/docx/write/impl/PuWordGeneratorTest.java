package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.excepts.NoFunctionException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PuWordGeneratorTest {

    @Test
    @DisplayName("generate: если у операции нет функций, выбрасывает NoFunctionException")
    void generate_operWithoutFunctions_throwsNoFunctionException() throws Exception {
        // Создаем валидный пустой docx документ с одной таблицей в памяти
        byte[] docxBytes;
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            doc.createTable(3, 14);
            doc.write(bos);
            docxBytes = bos.toByteArray();
        }

        Resource resource = new ByteArrayResource(docxBytes);
        PuWordGenerator generator = new PuWordGenerator(resource, "out/{0}/PU{1}.docx");

        OperDto operWithoutFuncs = new OperDto(1L, 1L, "005", "1", "o", "os", "Токарная", "s", "1", List.of());
        DockPackageDto dto = new DockPackageDto(1L, "P", "p", "pu", "spu", "kp", "fm", "vi", List.of(operWithoutFuncs), List.of());

        assertThatThrownBy(() -> generator.generate(dto))
                .isInstanceOf(NoFunctionException.class);
    }
}
