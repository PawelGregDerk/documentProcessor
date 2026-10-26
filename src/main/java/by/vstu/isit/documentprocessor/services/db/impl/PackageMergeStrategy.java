package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.entities.SborEd;
import by.vstu.isit.documentprocessor.mappers.db.FuncMapper;
import by.vstu.isit.documentprocessor.mappers.db.OperMapper;
import by.vstu.isit.documentprocessor.mappers.db.SborEdMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PackageMergeStrategy {

    private final OperMapper operMapper;
    private final FuncMapper funcMapper;
    private final SborEdMapper sborEdMapper;

    public PackageMergeStrategy(OperMapper operMapper, FuncMapper funcMapper, SborEdMapper sborEdMapper) {
        this.operMapper = operMapper;
        this.funcMapper = funcMapper;
        this.sborEdMapper = sborEdMapper;
    }

    public void merge(Docpackage existing, DockPackageDto dto) {
        mergeOpers(existing, dto);
        mergeSborEds(existing, dto);
    }

    private void mergeOpers(Docpackage existing, DockPackageDto dto) {
        Set<Long> dtoOperIds = dto.opers() != null
                ? dto.opers().stream().map(OperDto::id).filter(Objects::nonNull).collect(Collectors.toSet())
                : Set.of();
        existing.getOpers().removeIf(oper -> !dtoOperIds.contains(oper.getId()));

        Map<Long, Oper> operMap = existing.getOpers().stream()
                .collect(Collectors.toMap(Oper::getId, oper -> oper));

        if (dto.opers() != null) {
            for (OperDto operDto : dto.opers()) {
                if (operDto.id() != null) {
                    Oper oper = operMap.get(operDto.id());
                    if (oper != null) {
                        oper.setNumOper(operDto.numOper());
                        oper.setNomInstr(operDto.nomInstr());
                        oper.setOborud(operDto.oborud());
                        oper.setOstnasInstr(operDto.ostnasInstr());
                        oper.setName(operDto.name());
                        oper.setShifr(operDto.shifr());
                        oper.setNumZech(operDto.numZech());
                        mergeFuncs(oper, operDto.funcs());
                    }
                } else {
                    Oper oper = operMapper.toEntity(operDto);
                    oper.setDocpackage(existing);
                    oper.setFuncs(new ArrayList<>());
                    if (operDto.funcs() != null) {
                        for (FuncDto funcDto : operDto.funcs()) {
                            Func func = funcMapper.toEntity(funcDto);
                            func.setOper(oper);
                            oper.getFuncs().add(func);
                        }
                    }
                    existing.getOpers().add(oper);
                }
            }
        }
    }

    private void mergeFuncs(Oper oper, List<FuncDto> funcDtos) {
        Set<Long> dtoFuncIds = funcDtos != null
                ? funcDtos.stream().map(FuncDto::id).filter(Objects::nonNull).collect(Collectors.toSet())
                : Set.of();
        oper.getFuncs().removeIf(func -> !dtoFuncIds.contains(func.getId()));

        Map<Long, Func> funcMap = oper.getFuncs().stream()
                .collect(Collectors.toMap(Func::getId, func -> func));

        if (funcDtos != null) {
            for (FuncDto funcDto : funcDtos) {
                if (funcDto.id() != null) {
                    Func func = funcMap.get(funcDto.id());
                    if (func != null) {
                        func.setName(funcDto.name());
                        func.setParam(funcDto.param());
                        func.setIsProd(funcDto.isProd());
                        func.setSpecCharakt(funcDto.specCharakt());
                    }
                } else {
                    Func func = funcMapper.toEntity(funcDto);
                    func.setOper(oper);
                    oper.getFuncs().add(func);
                }
            }
        }
    }

    private void mergeSborEds(Docpackage existing, DockPackageDto dto) {
        Set<Long> dtoSborEdIds = dto.sborEds() != null
                ? dto.sborEds().stream().map(SborEdDto::id).filter(Objects::nonNull).collect(Collectors.toSet())
                : Set.of();
        existing.getSborEds().removeIf(sborEd -> !dtoSborEdIds.contains(sborEd.getId()));

        Map<Long, SborEd> sborEdMap = existing.getSborEds().stream()
                .collect(Collectors.toMap(SborEd::getId, sborEd -> sborEd));

        if (dto.sborEds() != null) {
            for (SborEdDto sborEdDto : dto.sborEds()) {
                if (sborEdDto.id() != null) {
                    SborEd sborEd = sborEdMap.get(sborEdDto.id());
                    if (sborEd != null) {
                        sborEd.setNazv(sborEdDto.nazv());
                        sborEd.setOboznach(sborEdDto.oboznach());
                    }
                } else {
                    SborEd sborEd = sborEdMapper.toEntity(sborEdDto);
                    sborEd.setDocpackage(existing);
                    existing.getSborEds().add(sborEd);
                }
            }
        }
    }
}
