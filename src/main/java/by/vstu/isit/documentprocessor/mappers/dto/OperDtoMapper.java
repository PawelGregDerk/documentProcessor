package by.vstu.isit.documentprocessor.mappers.dto;

import by.vstu.isit.documentprocessor.controllers.form.OperFormState;
import by.vstu.isit.documentprocessor.dto.OperDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperDtoMapper {

    private final FuncDtoMapper funcMapper;

    public OperDto toDto(OperFormState o) {
        return new OperDto(
                o.numOper(),
                o.nomInstr(),
                o.oborud(),
                o.ostnas(),
                o.name(),
                o.shifr(),
                o.zech(),
                o.extra(),
                o.funcs().stream()
                        .map(funcMapper::toDto)
                        .toList()
        );
    }
}

