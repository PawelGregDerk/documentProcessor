package by.vstu.isit.documentprocessor.dto;

import java.util.List;

public record FmeaOperDto(
        String numOper,
        String nomInstr,
        String oborud,
        String ostnas,
        String name,
        String shifr,
        String zech,
        List<FmeaFuncDto> functions
) {
}
