package by.vstu.isit.documentprocessor.controllers.form;

import java.util.List;

public record DockPackageFormState(
        String packageName,
        String pu,
        String spu,
        String kp,
        String fmea,
        String vedInstr,
        List<OperFormState> operations
) {}
