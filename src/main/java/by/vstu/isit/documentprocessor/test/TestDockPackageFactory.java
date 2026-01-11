package by.vstu.isit.documentprocessor.test;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public final class TestDockPackageFactory {
    private TestDockPackageFactory() {}

    public DockPackageDto createTestDto() {
        List<OperDto> operations = new ArrayList<>();
        int operIndex = 1;

        // 3 операции по 3 функции
        for (int i = 0; i < 3; i++) {
            operations.add(createOper(operIndex++, 3));
        }

        // 3 операции по 2 функции
        for (int i = 0; i < 3; i++) {
            operations.add(createOper(operIndex++, 2));
        }

        // 2 операции по 1 функции
        for (int i = 0; i < 2; i++) {
            operations.add(createOper(operIndex++, 1));
        }

        // 2 операции без функций
        for (int i = 0; i < 2; i++) {
            operations.add(createOper(operIndex++, 0));
        }

        return new DockPackageDto(
                "Тестовый пакет документов",
                "ПУ-0001",
                "СХПУ-0001",
                "КП-0001",
                "FMEA-0001",
                "15-132",
                "Доп. поле пакета ",
                operations
        );
    }

    private OperDto createOper(int index, int funcCount) {
        List<FuncDto> funcs = new ArrayList<>();

        for (int i = 1; i <= funcCount; i++) {
            funcs.add(new FuncDto(
                    "Функция " + i + " операции " + index,
                    "Параметры функции " + i,
                    i % 2 == 0,
                    "SC-" + index + "-" + i
            ));
        }

        return new OperDto(
                String.format("%02d", index),
                "ИНСТ-" + index,
                "Оборудование " + index,
                "Оснастка " + index,
                "Операция " + index,
                "Ш" + index,
                "15",
                funcs
        );
    }
}

