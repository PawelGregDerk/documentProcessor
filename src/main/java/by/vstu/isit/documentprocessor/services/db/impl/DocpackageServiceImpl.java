package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.mappers.db.DocpackageMapper;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocpackageServiceImpl implements DocpackageService {
    @Getter
    private final DocpackageRepository repository;
    private final DocpackageMapper docpackageMapper;

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
}
