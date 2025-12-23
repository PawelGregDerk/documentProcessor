package by.vstu.isit.documentprocessor.repositories;

import by.vstu.isit.documentprocessor.entities.Func;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FuncRepository extends JpaRepository<Func, Integer>, JpaSpecificationExecutor<Func> {

}