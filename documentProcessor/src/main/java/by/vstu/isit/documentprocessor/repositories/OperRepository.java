package by.vstu.isit.documentprocessor.repositories;

import by.vstu.isit.documentprocessor.entities.Oper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OperRepository extends JpaRepository<Oper, Long>, JpaSpecificationExecutor<Oper> {

}