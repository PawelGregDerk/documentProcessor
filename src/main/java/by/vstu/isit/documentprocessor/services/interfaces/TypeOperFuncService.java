package by.vstu.isit.documentprocessor.services.interfaces;

import by.vstu.isit.documentprocessor.entities.TypeOperFunc;

import java.util.List;
import java.util.Optional;

public interface TypeOperFuncService {

    TypeOperFunc saveTypeOperFunc(TypeOperFunc typeOperFunc);

    List<TypeOperFunc> findAllTypeOperFuncs();

    Optional<TypeOperFunc> findTypeOperFuncById(Long id);

    TypeOperFunc updateTypeOperFunc(TypeOperFunc typeOperFunc);

    void deleteTypeOperFunc(Long id);
}