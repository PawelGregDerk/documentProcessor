package by.vstu.isit.documentprocessor.test;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public final class TestDockPackageFactory {

    public DockPackageDto createTestDto() {

        List<OperDto> operations = new ArrayList<>();

        // --- Операция .001 (Хранение материалов) ---
        operations.add(new OperDto(
                ".001",
                "",
                "Поддон, Стеллаж для хранения материалов",
                "",
                "Хранение материалов на складе ОМТС",
                "",
                "22",
                List.of(
                        new FuncDto(
                                "Хранение материалов",
                                "По партиям в зависимости от даты поступления",
                                false,
                                ""
                        ),
                        new FuncDto(
                                "Температура и влажность",
                                "(+5°C)-(+40°C) 30-80 %",
                                false,
                                ""
                        )
                )
        ));

        // --- Операция .045 (Регулировка) ---
        operations.add(new OperDto(
                ".045",
                "009",
                "Установка ОМА-1321 с ротором согласно таблице 01, Шкаф сушки плат ШСП-004-07",
                "Осциллограф С1-83, Тара-шкатулка, Частотомер РЧ3-07-0002, Миллиамперметр Э536, Вольтметр M2017 или Э532, БП Б5-47",
                "Регулировка",
                "8501",
                "15",
                List.of(
                        new FuncDto(
                                "Наличие выходных импульсных сигналов",
                                "15-17 В — высокий уровень, 0-0,5 В — низкий уровень. Зазор 1,4 мм",
                                true,
                                "!FD1 <FF> (K) <>"
                        ),
                        new FuncDto(
                                "Срок хранения цианоакрилатного клея",
                                "12 месяцев с даты изготовления",
                                true,
                                ""
                        ),
                        new FuncDto(
                                "Напряжение питания",
                                "16 +/-1В",
                                false,
                                ""
                        )
                )
        ));

        // --- Операция 100 (повтор хранения) ---
        operations.add(new OperDto(
                "100",
                "",
                "Поддон, Стеллаж для хранения материалов",
                "",
                "Хранение материалов на складе ОМТС",
                "",
                "",
                List.of(
                        new FuncDto(
                                "Хранение материалов",
                                "По партиям в зависимости от даты поступления",
                                false,
                                ""
                        ),
                        new FuncDto(
                                "Температура и влажность",
                                "(+5°C)-(+40°C) 30-80 %",
                                false,
                                ""
                        )
                )
        ));

        // --- Сборочные единицы ---
        List<SborEdDto> sborEds = List.of(
                new SborEdDto(
                        "Датчики",
                        "1ПМ.292.001-11ПМ.292.001-7"
                )
        );

        return new DockPackageDto(
                "ПД8093-ПД8093-7",
                "Проект/1ПМ.292.001.000-007",
                "ПУ 0098",
                "СХ ПУ 0098",
                "КП0058",
                "FMEA T 0114",
                "15-132",
                operations,
                sborEds
        );
    }
}
