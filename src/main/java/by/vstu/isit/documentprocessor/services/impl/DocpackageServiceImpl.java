package by.vstu.isit.documentprocessor.services.impl;

import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
import by.vstu.isit.documentprocessor.services.interfaces.DocpackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocpackageServiceImpl implements DocpackageService {

    private final DocpackageRepository docpackageRepository;

    @Override
    @Transactional
    public Docpackage saveDocpackage(Docpackage docpackage) {
        return docpackageRepository.save(docpackage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Docpackage> findAllDocpackages() {
        return docpackageRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Docpackage> findDocpackageById(Long id) {
        return docpackageRepository.findById(id);
    }

    @Override
    @Transactional
    public Docpackage updateDocpackage(Docpackage docpackage) {
        return docpackageRepository.save(docpackage);
    }

    @Override
    @Transactional
    public void deleteDocpackage(Long id) {
        docpackageRepository.deleteById(id);
    }
}