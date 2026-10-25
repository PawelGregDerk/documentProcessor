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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RiWordGeneratorTest {

    private byte[] createSampleRiDocx() throws Exception {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            doc.createTable(1, 2); // Table 0
            doc.createTable(1, 2); // Table 1
            var table2 = doc.createTable(2, 4); // Table 2 (used by RiWordGenerator)
            doc.write(bos);
            return bos.toByteArray();
        }
    }

    @Test
    @DisplayName("generate: успешно создает файл РИ для каждой операции пакета")
    void generate_validPackage_createsRiFilesPerOperation(@TempDir Path tempDir) throws Exception {
        byte[] docxBytes = createSampleRiDocx();
        Resource resource = new ByteArrayResource(docxBytes);
        Path outPattern = tempDir.resolve("{0}/{1}/{2}.docx");
        RiWordGenerator generator = new RiWordGenerator(resource, outPattern.toString().replace("\\", "/"));

        FuncDto f1 = new FuncDto(1L, "Сборка узла", 1L, "Зазор 0.1 мм", true, "Критический");
        OperDto oper = new OperDto(1L, 1L, "010", "15-100", "Верстак", "Отвертка", "Сборочная", "СБ-1", "15", List.of(f1));
        SborEdDto sborEd = new SborEdDto(1L, 1L, "Узел 1", "УЗ.001");

        DockPackageDto dto = new DockPackageDto(1L, "Pkg", "testFolder", "pu", "spu", "kp", "fm", "vi",
                List.of(oper), List.of(sborEd));

        generator.generate(dto);

        Path expectedFile = tempDir.resolve("testFolder/ri/010 Сборочная.docx");
        assertThat(Files.exists(expectedFile)).isTrue();
    }

    @Test
    @DisplayName("generateOne: создает один документ РИ с переданными headerData")
    void generateOne_singleOper_createsDocxWithHeaderData(@TempDir Path tempDir) throws Exception {
        byte[] docxBytes = createSampleRiDocx();
        Resource resource = new ByteArrayResource(docxBytes);
        Path outPattern = tempDir.resolve("{0}/{1}/{2}.docx");
        RiWordGenerator generator = new RiWordGenerator(resource, outPattern.toString().replace("\\", "/"));

        FuncDto f1 = new FuncDto(2L, "Контроль", 5L, "Без дефектов", false, "");
        OperDto oper = new OperDto(5L, 1L, "020", "15-102", "Стенд", "Щуп", "Контрольная", "КН-1", "15", List.of(f1));

        generator.generateOne(oper, "oneFolder", Map.of("d", "Артикул"));

        Path expectedFile = tempDir.resolve("oneFolder/ri/020 Контрольная.docx");
        assertThat(Files.exists(expectedFile)).isTrue();
    }
}
