package by.vstu.isit.documentprocessor.services.db.interfaces;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.repositories.TypeOperRepository;
import java.util.List;

public interface TypeOperService extends Service<TypeOper, Long, TypeOperRepository> {
    TypeOper updateTypeOper(TypeOper typeOper);
    List<TypeOperDto> findAll();
}