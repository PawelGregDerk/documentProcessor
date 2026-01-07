package by.vstu.isit.documentprocessor.repositories;

import by.vstu.isit.documentprocessor.entities.Docpackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocpackageRepository extends JpaRepository<Docpackage, Long>, JpaSpecificationExecutor<Docpackage> {

}