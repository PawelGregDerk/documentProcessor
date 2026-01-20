package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.repositories.FuncRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.services.db.interfaces.FuncService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FuncServiceImpl implements FuncService {
    private final FuncRepository repository;

    @Override
    public FuncRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public Func updateFunc(Func func) {
        return repository.save(func);
    }
}