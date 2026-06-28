package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.repositories.FuncRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.services.db.interfaces.FuncService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FuncServiceImpl implements FuncService {
    @Getter
    private final FuncRepository repository;

    @Override
    @Transactional
    public Func updateFunc(Func func) {
        return repository.save(func);
    }
}