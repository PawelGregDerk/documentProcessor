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

    @Test
    @DisplayName("validate: пустой путь пакета вызывает ошибку")
    void validate_emptyPath_throws() {
        DockPackageDto dto = dto("", List.of(oper(List.of(func()))), List.of(sborEd()));

        assertThatThrownBy(() -> DockPackageDtoValidator.validate(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Путь пакета не может быть пустым");
    }

    @Test
    @DisplayName("validate: путь из точки или двух точек вызывает ошибку")
    void validate_dotSegments_throw() {
        for (String path : List.of(".", "..", "../0360", "0360/../010")) {
            DockPackageDto dto = dto(path, List.of(oper(List.of(func()))), List.of(sborEd()));

            assertThatThrownBy(() -> DockPackageDtoValidator.validate(dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Путь пакета не должен содержать «.» или «..»");
        }
    }

    @Test
    @DisplayName("validate: абсолютный путь вызывает ошибку")
    void validate_absolutePath_throws() {
        DockPackageDto dto = dto("C:/temp", List.of(oper(List.of(func()))), List.of(sborEd()));

        assertThatThrownBy(() -> DockPackageDtoValidator.validate(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Путь пакета должен быть относительным, например 0360");
    }

    @Test
    @DisplayName("validate: точка внутри имени папки допустима")
    void validate_dotInsideName_allowed() {
        DockPackageDto dto = dto("0360.1", List.of(oper(List.of(func()))), List.of(sborEd()));

        assertThatCode(() -> DockPackageDtoValidator.validate(dto))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validate: повторяющиеся номера операций вызывают ошибку")
    void validate_duplicateOperationNumbers_throw() {
        DockPackageDto dto = dto("path",
                List.of(oper("005", List.of(func())), oper("005", List.of(func()))),
                List.of(sborEd()));

        assertThatThrownBy(() -> DockPackageDtoValidator.validate(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Номера операций не должны повторяться. Измените номер операции: 005");
    }

    private FuncDto func() {
        return new FuncDto(1L, "Функция", 1L, "Параметр", true, "");
    }

    private OperDto oper(List<FuncDto> funcs) {
        return oper("005", funcs);
    }

    private OperDto oper(String numOper, List<FuncDto> funcs) {
        return new OperDto(1L, 1L, numOper, "15-132/005", "Станок", "Оснастка", "Операция", "Ш", "15", funcs);
    }

    private SborEdDto sborEd() {
        return new SborEdDto(1L, 1L, "Вал", "АБВГ.123");
    }

    private DockPackageDto dto(List<OperDto> opers, List<SborEdDto> sborEds) {
        return dto("path", opers, sborEds);
    }

    private DockPackageDto dto(String path, List<OperDto> opers, List<SborEdDto> sborEds) {
        return new DockPackageDto(
                1L, "Пакет", path, "pu", "spu", "kp", "fmea", "vi", opers, sborEds);
    }
}
