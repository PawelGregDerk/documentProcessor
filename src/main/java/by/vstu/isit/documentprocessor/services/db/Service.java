package by.vstu.isit.documentprocessor.services.db;

import by.vstu.isit.documentprocessor.excepts.DataNotFoundException;
import by.vstu.isit.documentprocessor.entities.AbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface Service<
        T extends AbstractEntity<ID>,
        ID extends Serializable,
        R extends JpaRepository<T, ID>> {

    R getRepository();

    @Transactional(readOnly = true)
    default T getById(ID id) {
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
    default void delete(ID id) {
        if (!getRepository().existsById(id)) {
            throw new DataNotFoundException("Entity not found: " + id);
        }

        getRepository().deleteById(id);
    }

    default void delete(T entity) throws DataNotFoundException {
        requireNonNull(entity, "Entity must not be null");

        ID id = entity.getId();
        if (id == null) {
            throw new IllegalArgumentException("Entity id must not be null");
        }

        delete(id);
    }
}
