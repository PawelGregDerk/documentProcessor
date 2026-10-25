package by.vstu.isit.documentprocessor.services.docx.edit;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.NoSuchFileException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FmeaDocEditorTest {

    @Test
    @DisplayName("edit: если исходный документ FMEA не найден, выбрасывает NoSuchFileException")
    void edit_sourceFileNotFound_throwsNoSuchFileException() {
        FmeaDocEditor editor = new FmeaDocEditor("non_existent_fmea/{0}/{1}.docx");

        OperDto oper = new OperDto(1L, 1L, "001", "15-01", "Оборудование", "Оснастка", "Название", "ШИФР", "15", List.of());
        SborEdDto sborEd = new SborEdDto(1L, 1L, "Название", "Обозначение");
        DockPackageDto dto = new DockPackageDto(1L, "Пакет", "non_existent_folder", "PU01", "SPU01", "KP01", "FMEA01", "VI01",
                List.of(oper), List.of(sborEd));

        assertThatThrownBy(() -> editor.edit(dto, dto))
                .isInstanceOf(NoSuchFileException.class);
    }
}
