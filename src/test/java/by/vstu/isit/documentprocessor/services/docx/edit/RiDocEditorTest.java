package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.RiWordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RiDocEditorTest {

    @Mock
    private Resource templateResource;

    @Mock
    private RiWordGenerator generator;

    @Test
    @DisplayName("edit: для новой операции (отсутствующей в исходном dto) вызывается generator.generateOne")
    void edit_newOperationAdded_invokesGenerateOne() throws Exception {
        RiDocEditor editor = new RiDocEditor("out/{0}/{1}/{2}.docx", templateResource, generator);

        SborEdDto sborEd = new SborEdDto(1L, 1L, "Название", "Обозначение");
        OperDto newOper = new OperDto(100L, 1L, "099", "15-99", "Станок", "Оснастка", "Новая операция", "Ш-99", "15", List.of());

        DockPackageDto origDto = new DockPackageDto(1L, "Пакет", "folder", "PU01", "SPU01", "KP01", "FMEA01", "VI01",
                List.of(), List.of(sborEd));
        DockPackageDto savedDto = new DockPackageDto(1L, "Пакет", "folder", "PU01", "SPU01", "KP01", "FMEA01", "VI01",
                List.of(newOper), List.of(sborEd));

        assertDoesNotThrow(() -> editor.edit(origDto, savedDto));

        verify(generator).generateOne(eq(newOper), eq("folder"), any(Map.class));
    }
}
