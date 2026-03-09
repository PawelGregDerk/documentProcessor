package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.mappers.db.DocpackageMapper;
import by.vstu.isit.documentprocessor.mappers.db.OperMapper;
import by.vstu.isit.documentprocessor.mappers.db.FuncMapper;
import by.vstu.isit.documentprocessor.mappers.db.SborEdMapper;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
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

    @Override
    @Transactional
    public Docpackage updateDocpackage(Docpackage docpackage) {
        return repository.save(docpackage);
    }

    @Override
    @Transactional
    public DockPackageDto getLastPackageDto() {
        List<Docpackage> allPackages = repository.findAll();
        if (allPackages.isEmpty()) {
            return null;
        }
        return docpackageMapper.toDto(allPackages.get(allPackages.size() - 1));
    }

    @Override
    @Transactional
    public DockPackageDto saveFullPackage(DockPackageDto dto) {
        Docpackage docpackage = docpackageMapper.toEntity(dto);
        docpackage.setOpers(new ArrayList<>());
        docpackage.setSborEds(new ArrayList<>());

        // Сохраняем Docpackage, чтобы получить ID
        docpackage = repository.save(docpackage);

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
