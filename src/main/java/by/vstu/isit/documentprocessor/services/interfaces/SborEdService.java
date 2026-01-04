package by.vstu.isit.documentprocessor.services.interfaces;

import by.vstu.isit.documentprocessor.entities.SborEd;

import java.util.List;
import java.util.Optional;

public interface SborEdService {

    SborEd saveSborEd(SborEd sborEd);

    List<SborEd> findAllSborEds();

    Optional<SborEd> findSborEdById(Long id);

    SborEd updateSborEd(SborEd sborEd);

    void deleteSborEd(Long id);
}