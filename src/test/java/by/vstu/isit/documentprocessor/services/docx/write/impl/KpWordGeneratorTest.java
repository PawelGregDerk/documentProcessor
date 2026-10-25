package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
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

class KpWordGeneratorTest {

    @Test
    @DisplayName("generate: выбрасывает NoSuchElementException, если список sborEds пуст")
    void generate_emptySborEds_throwsNoSuchElementException() throws Exception {
        byte[] docxBytes;
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            doc.createTable(1, 11);
            doc.write(bos);
            docxBytes = bos.toByteArray();
        }

        Resource resource = new ByteArrayResource(docxBytes);
        KpWordGenerator generator = new KpWordGenerator(resource, "out/{0}/{1}.docx");

        DockPackageDto dto = new DockPackageDto(1L, "P", "p", "pu", "spu", "kp", "fm", "vi", List.of(), List.of());

        assertThatThrownBy(() -> generator.generate(dto))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("generate: успешно формирует документ КП с операциями, функциями и закладками")
    void generate_validDto_generatesDocumentSuccessfully(@TempDir Path tempDir) throws Exception {
        byte[] docxBytes;
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            doc.createTable(1, 11);
            doc.write(bos);
            docxBytes = bos.toByteArray();
        }

        Path outPattern = tempDir.resolve("{0}/{1}.docx");
        Resource resource = new ByteArrayResource(docxBytes);
        KpWordGenerator generator = new KpWordGenerator(resource, outPattern.toString().replace("\\", "/"));

        FuncDto f1 = new FuncDto(10L, "Продукт операция", 1L, "Параметр 1", true, "Спец 1");
        FuncDto f2 = new FuncDto(11L, "Процесс операция", 1L, "Параметр 2", false, null);
        OperDto oper = new OperDto(1L, 1L, "005", "WI-1", "Станок", "Оснастка", "Точение", "Ш-1", "01", List.of(f1, f2));
        SborEdDto sborEd = new SborEdDto(100L, 1L, "Деталь", "АБВГ.123");

        DockPackageDto dto = new DockPackageDto(1L, "PkgName", "subfolder", "pu", "spu", "kpDoc", "fm", "vi",
                List.of(oper), List.of(sborEd));

        generator.generate(dto);

        Path expectedFile = tempDir.resolve("subfolder/kpDoc.docx");
        assertThat(Files.exists(expectedFile)).isTrue();

        try (var in = Files.newInputStream(expectedFile); var resDoc = new XWPFDocument(in)) {
            assertThat(resDoc.getTables()).isNotEmpty();
            var table = resDoc.getTables().getFirst();
            assertThat(table.getNumberOfRows()).isGreaterThan(1);
        }
    }
}
