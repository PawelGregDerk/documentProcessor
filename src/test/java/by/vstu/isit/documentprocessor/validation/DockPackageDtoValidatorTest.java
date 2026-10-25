package by.vstu.isit.documentprocessor.validation;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DockPackageDtoValidatorTest {

    @Test
    @DisplayName("validate: корректный пакет не вызывает ошибок")
    void validate_validPackage_doesNotThrow() {
        DockPackageDto dto = dto(List.of(oper(List.of(func()))), List.of(sborEd()));

        assertThatCode(() -> DockPackageDtoValidator.validate(dto))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validate: пустой список сборочных единиц вызывает ошибку")
    void validate_emptySborEds_throws() {
        DockPackageDto dto = dto(List.of(oper(List.of(func()))), List.of());

        assertThatThrownBy(() -> DockPackageDtoValidator.validate(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Необходимо указать хотя бы одну сборочную единицу");
    }

    @Test
    @DisplayName("validate: пустой список операций вызывает ошибку")
    void validate_emptyOpers_throws() {
        DockPackageDto dto = dto(List.of(), List.of(sborEd()));

        assertThatThrownBy(() -> DockPackageDtoValidator.validate(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Необходимо указать хотя бы одну операцию");
    }

    @Test
    @DisplayName("validate: операция без функций вызывает ошибку")
    void validate_operWithoutFunctions_throws() {
        DockPackageDto dto = dto(List.of(oper(List.of())), List.of(sborEd()));

        assertThatThrownBy(() -> DockPackageDtoValidator.validate(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Каждая операция должна содержать хотя бы одну функцию");
    }

    private FuncDto func() {
        return new FuncDto(1L, "Функция", 1L, "Параметр", true, "");
    }

    private OperDto oper(List<FuncDto> funcs) {
        return new OperDto(1L, 1L, "005", "15-132/005", "Станок", "Оснастка", "Операция", "Ш", "15", funcs);
    }

    private SborEdDto sborEd() {
        return new SborEdDto(1L, 1L, "Вал", "АБВГ.123");
    }

    private DockPackageDto dto(List<OperDto> opers, List<SborEdDto> sborEds) {
        return new DockPackageDto(
                1L, "Пакет", "path", "pu", "spu", "kp", "fmea", "vi", opers, sborEds);
    }
}
