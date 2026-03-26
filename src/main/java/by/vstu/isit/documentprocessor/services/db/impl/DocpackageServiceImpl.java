package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
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
    public DockPackageDto updateFullPackage(DockPackageDto dto) {
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
        
        // Удаляем старые связанные записи из БД
        operRepository.deleteAll(existing.getOpers());
        sborEdRepository.deleteAll(existing.getSborEds());
        existing.getOpers().clear();
        existing.getSborEds().clear();
        
        // Добавление новых операций
        if (dto.opers() != null) {
            for (OperDto odto : dto.opers()) {
                Oper oper = operMapper.toEntity(odto);
                oper.setDocpackage(existing);
                
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
        
        // Добавление новых сборных единиц
        if (dto.sborEds() != null) {
            for (var sdto : dto.sborEds()) {
                SborEd sborEd = sborEdMapper.toEntity(sdto);
                sborEd.setDocpackage(existing);
                existing.getSborEds().add(sborEd);
            }
        }
        
        return docpackageMapper.toDto(repository.save(existing));
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
