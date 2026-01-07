package by.vstu.isit.documentprocessor.services.db.interfaces;

import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;

public interface DocpackageService extends Service<Docpackage, Long, DocpackageRepository> {
    Docpackage updateDocpackage(Docpackage docpackage);
}