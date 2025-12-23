package by.vstu.isit.documentprocessor.repositories;

import by.vstu.isit.documentprocessor.entities.TypeOper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TypeOperRepository extends JpaRepository<TypeOper, Long>, JpaSpecificationExecutor<TypeOper> {

}