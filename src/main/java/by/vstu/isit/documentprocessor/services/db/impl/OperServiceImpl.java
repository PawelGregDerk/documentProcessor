package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.repositories.OperRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.services.db.interfaces.OperService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperServiceImpl implements OperService {
    private final OperRepository repository;

    @Override
    public OperRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public Oper updateOper(Oper oper) {
        return repository.save(oper);
    }
}