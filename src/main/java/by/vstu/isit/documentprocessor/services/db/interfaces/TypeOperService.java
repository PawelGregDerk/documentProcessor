package by.vstu.isit.documentprocessor.services.db.interfaces;

import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.repositories.TypeOperRepository;

public interface TypeOperService extends Service<TypeOper, Long, TypeOperRepository> {
    TypeOper updateTypeOper(TypeOper typeOper);
}