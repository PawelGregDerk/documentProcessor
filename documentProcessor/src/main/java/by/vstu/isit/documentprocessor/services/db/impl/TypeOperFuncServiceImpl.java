package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.repositories.TypeOperFuncRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperFuncService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TypeOperFuncServiceImpl implements TypeOperFuncService {
    @Getter
    private final TypeOperFuncRepository repository;

    @Override
    @Transactional
    public TypeOperFunc updateTypeOperFunc(TypeOperFunc typeOperFunc) {
        return repository.save(typeOperFunc);
    }
}