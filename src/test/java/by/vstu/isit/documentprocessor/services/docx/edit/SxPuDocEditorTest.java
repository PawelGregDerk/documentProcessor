package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.SxPuWordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SxPuDocEditorTest {

    @Mock
    private SxPuWordGenerator sxPuWordGenerator;

    @Test
    @DisplayName("edit: если нет функций со специальными характеристиками, метод сразу завершается")
    void edit_noSpecCharaktFunctions_doesNothing() {
        SxPuDocEditor editor = new SxPuDocEditor("out/{0}/{1}.docx");
        ReflectionTestUtils.setField(editor, "sxPuWordGenerator", sxPuWordGenerator);

        FuncDto f1 = new FuncDto(1L, "Простая функция", 1L, "Парам", true, null);
        OperDto oper = new OperDto(1L, 1L, "001", "15-01", "Оборудование", "Оснастка", "Название", "ШИФР", "15", List.of(f1));
        SborEdDto sborEd = new SborEdDto(1L, 1L, "Название", "Обозначение");
        DockPackageDto dto = new DockPackageDto(1L, "Пакет", "folder", "PU01", "SPU01", "KP01", "FMEA01", "VI01",
                List.of(oper), List.of(sborEd));

        assertDoesNotThrow(() -> editor.edit(dto, dto));
        verifyNoInteractions(sxPuWordGenerator);
    }
}
