package by.vstu.isit.documentprocessor.controllers.form;

import java.util.List;

public record OperFormState(
        String numOper,
        String nomInstr,
        String oborud,
        String ostnas,
        String name,
        String shifr,
        String zech,
        String extra,
        List<FuncFormState> funcs
) {}

