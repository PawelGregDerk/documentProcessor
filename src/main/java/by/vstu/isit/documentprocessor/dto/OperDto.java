package by.vstu.isit.documentprocessor.dto;

import java.util.List;

public record OperDto(
        String numOper,
        String nomInstr,
        String oborud,
        String ostnasInstr,
        String name,
        String shifr,
        String numZech,
        List<FuncDto> funcs
) {
}
