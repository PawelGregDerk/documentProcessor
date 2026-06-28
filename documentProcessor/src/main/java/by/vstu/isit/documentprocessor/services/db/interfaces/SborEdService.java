package by.vstu.isit.documentprocessor.services.db.interfaces;

import by.vstu.isit.documentprocessor.entities.SborEd;
import by.vstu.isit.documentprocessor.repositories.SborEdRepository;

public interface SborEdService extends Service<SborEd, Long, SborEdRepository> {
    SborEd updateSborEd(SborEd sborEd);
}