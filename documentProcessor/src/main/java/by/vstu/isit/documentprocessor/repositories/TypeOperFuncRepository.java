package by.vstu.isit.documentprocessor.repositories;

import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TypeOperFuncRepository extends JpaRepository<TypeOperFunc, Long>, JpaSpecificationExecutor<TypeOperFunc> {

}