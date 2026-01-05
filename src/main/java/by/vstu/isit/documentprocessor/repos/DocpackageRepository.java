package by.vstu.isit.documentprocessor.repos;

import by.vstu.isit.documentprocessor.entities.db.Docpackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocpackageRepository extends JpaRepository<Docpackage, Long>, JpaSpecificationExecutor<Docpackage> {

}