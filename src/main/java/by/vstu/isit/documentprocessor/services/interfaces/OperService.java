package by.vstu.isit.documentprocessor.services.interfaces;

import by.vstu.isit.documentprocessor.entities.Oper;

import java.util.List;
import java.util.Optional;

public interface OperService {

    Oper saveOper(Oper oper);

    List<Oper> findAllOpers();

    Optional<Oper> findOperById(Long id);

    Oper updateOper(Oper oper);

    void deleteOper(Long id);
}