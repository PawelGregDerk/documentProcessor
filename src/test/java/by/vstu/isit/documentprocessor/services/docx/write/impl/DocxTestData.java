package by.vstu.isit.documentprocessor.services.docx.write.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;

import java.util.List;

final class DocxTestData {

    static final String PACKAGE_NAME = "АБВГ.123456.001";
    static final String PACKAGE_FOLDER = "pkg-1";
    static final String PU_NAME = "0360";
    static final String SPU_NAME = "0002";
    static final String KP_NAME = "0309";
    static final String FMEA_NAME = "T0363";
    static final String VED_I_NAME = "15-132";

    private DocxTestData() {
    }

    static FuncDto func(long id, String name, String param, boolean isProd, String specCharakt) {
        return new FuncDto(id, name, 101L, param, isProd, specCharakt);
    }

    static OperDto oper(long id, String numOper, String name, List<FuncDto> funcs) {
        return new OperDto(
                id, 1L, numOper, VED_I_NAME + "/" + numOper,
                "Станок " + numOper, "Оснастка " + numOper, name, "Т" + numOper, "15",
                funcs
        );
    }

    static SborEdDto sborEd(long id, String nazv, String oboznach) {
        return new SborEdDto(id, 1L, nazv, oboznach);
    }

    static DockPackageDto dto(OperDto oper, List<SborEdDto> sborEds) {
        return dto(List.of(oper), sborEds);
    }

    static DockPackageDto dto(List<OperDto> opers, List<SborEdDto> sborEds) {
        return new DockPackageDto(
                1L, PACKAGE_NAME, PACKAGE_FOLDER,
                PU_NAME, SPU_NAME, KP_NAME, FMEA_NAME, VED_I_NAME,
                opers, sborEds
        );
    }
}
