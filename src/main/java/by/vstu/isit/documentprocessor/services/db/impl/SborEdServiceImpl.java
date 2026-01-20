package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.repositories.SborEdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.isit.documentprocessor.entities.SborEd;
import by.vstu.isit.documentprocessor.services.db.interfaces.SborEdService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SborEdServiceImpl implements SborEdService {
    private final SborEdRepository repository;

    @Override
    public SborEdRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public SborEd updateSborEd(SborEd sborEd) {
        return repository.save(sborEd);
    }
}