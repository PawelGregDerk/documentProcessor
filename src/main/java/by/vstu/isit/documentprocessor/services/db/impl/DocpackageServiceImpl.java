package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocpackageServiceImpl implements DocpackageService {
    private final DocpackageRepository repository;

    @Override
    public DocpackageRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public Docpackage updateDocpackage(Docpackage docpackage) {
        return repository.save(docpackage);
    }
}