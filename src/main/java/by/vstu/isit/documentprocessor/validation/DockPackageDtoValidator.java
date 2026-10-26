package by.vstu.isit.documentprocessor.validation;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import lombok.experimental.UtilityClass;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

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
        validatePackagePath(dto.path());
        validateUniqueOperationNumbers(dto);
    }

    private void validatePackagePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Путь пакета не может быть пустым");
        }
        String value = path.trim();
        if (value.startsWith("/") || value.startsWith("\\") || value.matches("^[A-Za-z]:.*")) {
            throw new IllegalStateException("Путь пакета должен быть относительным, например 0360");
        }
        Path relative;
        try {
            relative = Path.of(value);
        } catch (InvalidPathException e) {
            throw new IllegalStateException("Путь пакета содержит недопустимые символы: " + path);
        }
        for (Path segment : relative) {
            String part = segment.toString();
            if (".".equals(part) || "..".equals(part)) {
                throw new IllegalStateException("Путь пакета не должен содержать «.» или «..»");
            }
        }
    }

    private void validateUniqueOperationNumbers(DockPackageDto dto) {
        Set<String> seen = new LinkedHashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (OperDto oper : dto.opers()) {
            String num = oper.numOper() == null ? "" : oper.numOper().trim();
            if (!seen.add(num)) {
                duplicates.add(num);
            }
        }
        if (!duplicates.isEmpty()) {
            throw new IllegalStateException(
                    "Номера операций не должны повторяться. Измените номер операции: "
                            + String.join(", ", duplicates));
        }
    }
}
