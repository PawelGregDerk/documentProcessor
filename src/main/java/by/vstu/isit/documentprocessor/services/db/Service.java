package by.vstu.isit.documentprocessor.services.db;

import by.vstu.isit.documentprocessor.excepts.DataNotFoundException;
import lombok.SneakyThrows;
import by.vstu.isit.documentprocessor.entities.AbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

public interface Service<
        T extends AbstractEntity,
        ID extends Serializable,
        R extends JpaRepository<T, ID>> {

    R getRepository();

    @Transactional(readOnly = true)
    default T getById(ID id) throws DataNotFoundException {
        return getRepository()
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("Entity not found: " + id));
    }

    @Transactional(readOnly = true)
    default List<T> getAll() {
        return getRepository().findAll();
    }

    @Transactional
    default T save(T entity) {
        return getRepository().save(entity);
    }

    @Transactional
    default void delete(T entity) {
        getRepository().delete(entity);
    }

    @Transactional
    default void delete(ID id) throws DataNotFoundException {
        if (!getRepository().existsById(id)) {
            throw new DataNotFoundException("Entity not found: " + id);
        }
        getRepository().deleteById(id);
    }
}
