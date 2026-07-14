package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.mappers.db.DocpackageMapper;
import by.vstu.isit.documentprocessor.mappers.db.OperMapper;
import by.vstu.isit.documentprocessor.mappers.db.FuncMapper;
import by.vstu.isit.documentprocessor.mappers.db.SborEdMapper;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
import by.vstu.isit.documentprocessor.repositories.OperRepository;
import by.vstu.isit.documentprocessor.repositories.SborEdRepository;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.entities.SborEd;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocpackageServiceImpl implements DocpackageService {
    @Getter
    private final DocpackageRepository repository;
    private final DocpackageMapper docpackageMapper;
    private final OperMapper operMapper;
    private final FuncMapper funcMapper;
    private final SborEdMapper sborEdMapper;
    private final SborEdRepository sborEdRepository;
    private final OperRepository operRepository;

    @Override
    @Transactional
    public Docpackage updateDocpackage(Docpackage docpackage) {
        return repository.save(docpackage);
    }

    @Override
    @Transactional
    public DockPackageDto saveFullPackage(DockPackageDto dto) {
        Docpackage docpackage = docpackageMapper.toEntity(dto);
        
        // Сохраняем операции
        if (dto.opers() != null && !dto.opers().isEmpty()) {
            List<Oper> savedOpers = new ArrayList<>();
            for (OperDto operDto : dto.opers()) {
                Oper oper = operMapper.toEntity(operDto);
                oper.setDocpackage(docpackage);
                
                // Сохраняем функции для операции
                if (operDto.funcs() != null && !operDto.funcs().isEmpty()) {
                    List<Func> savedFuncs = new ArrayList<>();
                    for (FuncDto funcDto : operDto.funcs()) {
                        Func func = funcMapper.toEntity(funcDto);
                        func.setOper(oper);
                        savedFuncs.add(func);
                    }
                    oper.setFuncs(savedFuncs);
                }
                savedOpers.add(oper);
            }
            docpackage.setOpers(savedOpers);
        }
        
        // Сохраняем сборные единицы
        if (dto.sborEds() != null && !dto.sborEds().isEmpty()) {
            List<SborEd> savedSborEds = new ArrayList<>();
            for (var sborEdDto : dto.sborEds()) {
                SborEd sborEd = sborEdMapper.toEntity(sborEdDto);
                sborEd.setDocpackage(docpackage);
                savedSborEds.add(sborEd);
            }
            docpackage.setSborEds(savedSborEds);
        }
        
        // Сохраняем окончательно с каскадом
        docpackage = repository.save(docpackage);
        
        return docpackageMapper.toDto(docpackage);
    }

    @Override
    @Transactional
    public DocpackageService.UpdateResult updateFullPackage(DockPackageDto dto) {
        // Проверка: если ID null - нечего обновлять
        if (dto.id() == null) {
            throw new IllegalArgumentException("ID пакета не может быть null для обновления");
        }
        
        // Поиск существующего пакета по ID
        Docpackage existing = repository.findById(dto.id())
                .orElseThrow(() -> new by.vstu.isit.documentprocessor.excepts.DataNotFoundException(
                        "Пакет не найден: " + dto.id()));
        
        // Обновление полей Docpackage
        existing.setPackageName(dto.packageName());
        existing.setPuName(dto.puName());
        existing.setSpuName(dto.spuName());
        existing.setKpName(dto.kpName());
        existing.setFmeaName(dto.fmeaName());
        existing.setVedIName(dto.vedIName());

        // Определяем, изменился ли порядок существующих операций
        var oldOperIds = existing.getOpers().stream().map(Oper::getId).toList();
        boolean orderChanged = false;
        for (int i = 0; i < dto.opers().size(); i++) {
            Long id = dto.opers().get(i).id();
            if (id != null) {
                int oldIdx = oldOperIds.indexOf(id);
                if (oldIdx != i) {
                    orderChanged = true;
                    break;
                }
            }
        }
        
        if (orderChanged) {
            // Порядок изменился — полная очистка и пересоздание с новыми ID
            existing.getOpers().clear();
            existing.getSborEds().clear();

            if (dto.opers() != null) {
                for (OperDto odto : dto.opers()) {
                    Oper oper = operMapper.toEntity(odto);
                    oper.setId(null);
                    oper.setDocpackage(existing);
                    oper.setFuncs(new ArrayList<>());

                    if (odto.funcs() != null) {
                        for (FuncDto fdto : odto.funcs()) {
                            Func func = funcMapper.toEntity(fdto);
                            func.setId(null);
                            func.setOper(oper);
                            oper.getFuncs().add(func);
                        }
                    }
                    existing.getOpers().add(oper);
                }
            }

            if (dto.sborEds() != null) {
                for (var sdto : dto.sborEds()) {
                    SborEd sborEd = sborEdMapper.toEntity(sdto);
                    sborEd.setId(null);
                    sborEd.setDocpackage(existing);
                    existing.getSborEds().add(sborEd);
                }
            }
        } else {
            // Порядок не изменился — обновление на месте без пересоздания
            mergeOpers(existing, dto);
            mergeSborEds(existing, dto);
        }
        
        return new DocpackageService.UpdateResult(
                docpackageMapper.toDto(repository.save(existing)),
                orderChanged
        );
    }

    private void mergeOpers(Docpackage existing, DockPackageDto dto) {
        Set<Long> dtoOperIds = dto.opers() != null
                ? dto.opers().stream().map(OperDto::id).filter(Objects::nonNull).collect(Collectors.toSet())
                : Set.of();
        existing.getOpers().removeIf(o -> !dtoOperIds.contains(o.getId()));

        Map<Long, Oper> operMap = existing.getOpers().stream()
                .collect(Collectors.toMap(Oper::getId, o -> o));

        if (dto.opers() != null) {
            for (OperDto odto : dto.opers()) {
                if (odto.id() != null) {
                    Oper oper = operMap.get(odto.id());
                    if (oper != null) {
                        oper.setNumOper(odto.numOper());
                        oper.setNomInstr(odto.nomInstr());
                        oper.setOborud(odto.oborud());
                        oper.setOstnasInstr(odto.ostnasInstr());
                        oper.setName(odto.name());
                        oper.setShifr(odto.shifr());
                        oper.setNumZech(odto.numZech());
                        mergeFuncs(oper, odto.funcs());
                    }
                } else {
                    Oper oper = operMapper.toEntity(odto);
                    oper.setDocpackage(existing);
                    oper.setFuncs(new ArrayList<>());
                    if (odto.funcs() != null) {
                        for (FuncDto fdto : odto.funcs()) {
                            Func func = funcMapper.toEntity(fdto);
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
        oper.getFuncs().removeIf(f -> !dtoFuncIds.contains(f.getId()));

        Map<Long, Func> funcMap = oper.getFuncs().stream()
                .collect(Collectors.toMap(Func::getId, f -> f));

        if (funcDtos != null) {
            for (FuncDto fdto : funcDtos) {
                if (fdto.id() != null) {
                    Func func = funcMap.get(fdto.id());
                    if (func != null) {
                        func.setName(fdto.name());
                        func.setParam(fdto.param());
                        func.setIsProd(fdto.isProd());
                        func.setSpecCharakt(fdto.specCharakt());
                    }
                } else {
                    Func func = funcMapper.toEntity(fdto);
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
        existing.getSborEds().removeIf(s -> !dtoSborEdIds.contains(s.getId()));

        Map<Long, SborEd> sborEdMap = existing.getSborEds().stream()
                .collect(Collectors.toMap(SborEd::getId, s -> s));

        if (dto.sborEds() != null) {
            for (var sdto : dto.sborEds()) {
                if (sdto.id() != null) {
                    SborEd sborEd = sborEdMap.get(sdto.id());
                    if (sborEd != null) {
                        sborEd.setNazv(sdto.nazv());
                        sborEd.setOboznach(sdto.oboznach());
                    }
                } else {
                    SborEd sborEd = sborEdMapper.toEntity(sdto);
                    sborEd.setDocpackage(existing);
                    existing.getSborEds().add(sborEd);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DockPackageDto findDtoById(Long id) {
        return repository.findById(id)
                .map(docpackageMapper::toDto)
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DockPackageDto> searchByOboznach(String oboznach) {
        // Ищем сборные единицы по обозначению
        List<SborEd> sborEds = sborEdRepository.findByOboznachContainingIgnoreCase(oboznach);
        
        // Получаем уникальные Docpackage из найденных сборных единиц
        Set<Docpackage> uniquePackages = new HashSet<>();
        for (SborEd se : sborEds) {
            if (se.getDocpackage() != null) {
                uniquePackages.add(se.getDocpackage());
            }
        }
        
        // Маппим в DTO
        return uniquePackages.stream()
                .map(docpackageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DockPackageDto> searchByPackageName(String packageName) {
        // Ищем пакеты документов по обозначению изделия
        List<Docpackage> packages = repository.findByPackageNameContainingIgnoreCase(packageName);
        
        // Маппим в DTO
        return packages.stream()
                .map(docpackageMapper::toDto)
                .collect(Collectors.toList());
    }
}
