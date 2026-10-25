package by.vstu.isit.documentprocessor.validation;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DockPackageDtoValidator {

    public void validate(DockPackageDto dto) {
        if (dto.sborEds().isEmpty()) {
            throw new IllegalStateException("Необходимо указать хотя бы одну сборочную единицу");
        }
        if (dto.opers().isEmpty()) {
            throw new IllegalStateException("Необходимо указать хотя бы одну операцию");
        }
        if (dto.opers().stream().anyMatch(oper -> oper.funcs().isEmpty())) {
            throw new IllegalStateException("Каждая операция должна содержать хотя бы одну функцию");
        }
    }
}
