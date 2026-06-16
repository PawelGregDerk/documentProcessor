package by.vstu.isit.documentprocessor.dto;

import java.util.ArrayList;
import java.util.List;

public record OperDto(
        Long id,
        Long idDocPackage,
        String numOper,
        String nomInstr,
        String oborud,
        String ostnasInstr,
        String name,
        String shifr,
        String numZech,
        List<FuncDto> funcs
) {
    public OperDto {
        if (funcs == null) {
            funcs = new ArrayList<>();
        }
    }
}
