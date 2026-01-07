package by.vstu.isit.documentprocessor.repositories;

import by.vstu.isit.documentprocessor.entities.SborEd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SborEdRepository extends JpaRepository<SborEd, Long>, JpaSpecificationExecutor<SborEd> {

}