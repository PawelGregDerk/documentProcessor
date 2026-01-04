package by.vstu.isit.documentprocessor.services.interfaces;

import by.vstu.isit.documentprocessor.entities.Docpackage;

import java.util.List;
import java.util.Optional;

public interface DocpackageService {

    Docpackage saveDocpackage(Docpackage docpackage);

    List<Docpackage> findAllDocpackages();

    Optional<Docpackage> findDocpackageById(Long id);

    Docpackage updateDocpackage(Docpackage docpackage);

    void deleteDocpackage(Long id);
}