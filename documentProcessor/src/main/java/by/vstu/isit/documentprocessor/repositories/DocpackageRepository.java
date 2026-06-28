package by.vstu.isit.documentprocessor.repositories;

import by.vstu.isit.documentprocessor.entities.Docpackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DocpackageRepository extends JpaRepository<Docpackage, Long>, JpaSpecificationExecutor<Docpackage> {

    List<Docpackage> findByPackageNameContainingIgnoreCase(String packageName);

}