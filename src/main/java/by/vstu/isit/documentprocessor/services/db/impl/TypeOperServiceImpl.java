package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.repositories.TypeOperRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TypeOperServiceImpl implements TypeOperService {
    private final TypeOperRepository repository;

    @Override
    public TypeOperRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public TypeOper updateTypeOper(TypeOper typeOper) {
        return repository.save(typeOper);
    }
}