package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WiWordGeneratorTest {

    private byte[] createSampleWiDocx() throws Exception {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            doc.createTable(1, 2); // Table 0
            doc.createTable(1, 5); // Table 1 (used by WiWordGenerator)
            doc.write(bos);
            return bos.toByteArray();
        }
    }

    @Test
    @DisplayName("generate: выбрасывает NoSuchElementException, если список sborEds пуст")
    void generate_emptySborEds_throwsNoSuchElementException() throws Exception {
        byte[] docxBytes = createSampleWiDocx();
        Resource resource = new ByteArrayResource(docxBytes);
        WiWordGenerator generator = new WiWordGenerator(resource, "out/{0}/{1}.docx");

        DockPackageDto dto = new DockPackageDto(1L, "P", "folder", "pu", "spu", "kp", "fm", "viDoc",
                List.of(), List.of());

        assertThatThrownBy(() -> generator.generate(dto))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("generate: успешно формирует документ ВИ с таблицей операций")
    void generate_validDto_createsWiDocxFileSuccessfully(@TempDir Path tempDir) throws Exception {
        byte[] docxBytes = createSampleWiDocx();
        Resource resource = new ByteArrayResource(docxBytes);
        Path outPattern = tempDir.resolve("{0}/{1}.docx");
        WiWordGenerator generator = new WiWordGenerator(resource, outPattern.toString().replace("\\", "/"));

        OperDto oper = new OperDto(1L, 1L, "001", "15-01", "Оборудование", "Оснастка", "Операция 1", "ШИФР-1", "15", List.of());
        SborEdDto sborEd = new SborEdDto(1L, 1L, "Название ДСЕ", "Обозначение ДСЕ");

        DockPackageDto dto = new DockPackageDto(1L, "Пакет", "outDir", "pu", "spu", "kp", "fm", "vedInstDoc",
                List.of(oper), List.of(sborEd));

        generator.generate(dto);

        Path expectedFile = tempDir.resolve("outDir/vedInstDoc.docx");
        assertThat(Files.exists(expectedFile)).isTrue();
    }
}
