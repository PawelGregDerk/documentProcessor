package by.vstu.isit.documentprocessor.services.db;

import by.vstu.isit.documentprocessor.excepts.DataNotFoundException;
import lombok.SneakyThrows;
import by.vstu.isit.documentprocessor.entities.AbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

public interface Service<
        T extends AbstractEntity,
        ID extends Serializable,
        R extends JpaRepository<T, ID>> {

    R getRepository();

    @SneakyThrows()
    default T getById(ID id) {
        return getRepository().findById(id).orElseThrow(DataNotFoundException::new);
    }

    default List<T> getAll() {
        return getRepository().findAll();
    }

    default T save(T entity) {
        return getRepository().save(entity);
    }

    default boolean delete(T entity) {
        getRepository().delete(entity);
        return true;
    }

    default boolean delete(ID id) {
        getRepository().deleteById(id);
        return true;
    }
}
