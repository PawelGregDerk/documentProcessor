package by.vstu.isit.documentprocessor.repositories;

import by.vstu.isit.documentprocessor.entities.TypeOper;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TypeOperRepository extends JpaRepository<TypeOper, Long>, JpaSpecificationExecutor<TypeOper> {

    @EntityGraph(attributePaths = "typeOperFuncs")
    Optional<TypeOper> findWithFuncsById(Long id);
}