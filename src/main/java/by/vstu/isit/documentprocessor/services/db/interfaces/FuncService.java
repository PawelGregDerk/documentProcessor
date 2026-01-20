package by.vstu.isit.documentprocessor.services.db.interfaces;

import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.repositories.FuncRepository;

public interface FuncService extends Service<Func, Long, FuncRepository> {
    Func updateFunc(Func func);
}