package by.vstu.isit.documentprocessor.services.db.interfaces;

import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import by.vstu.isit.documentprocessor.repositories.TypeOperFuncRepository;

public interface TypeOperFuncService extends Service<TypeOperFunc, Long, TypeOperFuncRepository> {
    TypeOperFunc updateTypeOperFunc(TypeOperFunc typeOperFunc);
}