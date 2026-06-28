package by.vstu.isit.documentprocessor.services.db.interfaces;

import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.repositories.OperRepository;

public interface OperService extends Service<Oper, Long, OperRepository> {
    Oper updateOper(Oper oper);
}