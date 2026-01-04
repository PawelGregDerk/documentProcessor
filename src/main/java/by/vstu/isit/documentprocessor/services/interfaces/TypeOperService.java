package by.vstu.isit.documentprocessor.services.interfaces;

import by.vstu.isit.documentprocessor.entities.TypeOper;

import java.util.List;
import java.util.Optional;

public interface TypeOperService {

    TypeOper saveTypeOper(TypeOper typeOper);

    List<TypeOper> findAllTypeOpers();

    Optional<TypeOper> findTypeOperById(Long id);

    TypeOper updateTypeOper(TypeOper typeOper);

    void deleteTypeOper(Long id);
}