package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.docx.write.impl.Table01WordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class Table01DocEditorTest {

    @Mock
    private Table01WordGenerator wordGenerator;

    @Test
    @DisplayName("edit: если сборочных единиц <= 1, метод завершается без генерации и ошибок")
    void edit_sborEdsCountLessThanOrEqualToOne_doesNothing() {
        Table01DocEditor editor = new Table01DocEditor("out/{0}/Таблица 01.docx", wordGenerator);

        SborEdDto s1 = new SborEdDto(1L, 1L, "Деталь", "ОБОЗН");
        DockPackageDto dto = new DockPackageDto(1L, "P", "folder", "pu", "spu", "kp", "fm", "vi", List.of(), List.of(s1));

        assertDoesNotThrow(() -> editor.edit(dto, dto));
        verifyNoInteractions(wordGenerator);
    }

    @Test
    @DisplayName("edit: если список сборочных единиц пуст, метод также ничего не делает")
    void edit_emptySborEds_doesNothing() {
        Table01DocEditor editor = new Table01DocEditor("out/{0}/Таблица 01.docx", wordGenerator);

        DockPackageDto dto = new DockPackageDto(1L, "P", "folder", "pu", "spu", "kp", "fm", "vi", List.of(), List.of());

        assertDoesNotThrow(() -> editor.edit(dto, dto));
        verifyNoInteractions(wordGenerator);
    }
}
