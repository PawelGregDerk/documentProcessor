package by.vstu.isit.documentprocessor.services.interfaces;

import by.vstu.isit.documentprocessor.entities.Func;

import java.util.List;
import java.util.Optional;

public interface FuncService {

    Func saveFunc(Func func);

    List<Func> findAllFuncs();

    Optional<Func> findFuncById(Long id);

    Func updateFunc(Func func);

    void deleteFunc(Long id);
}