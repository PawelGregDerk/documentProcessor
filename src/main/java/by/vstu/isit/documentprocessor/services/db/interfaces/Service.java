package by.vstu.isit.documentprocessor.services.db.interfaces;

import by.vstu.isit.documentprocessor.excepts.DataNotFoundException;
import by.vstu.isit.documentprocessor.entities.db.AbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface Service<
        T extends AbstractEntity<ID>,
        ID extends Serializable,
        R extends JpaRepository<T, ID>> {

    final class LogHolder {
        static final Logger log = LoggerFactory.getLogger(Service.class);
    }

    R getRepository();

    @Transactional(readOnly = true)
    default T getById(ID id) {
        return getRepository()
                .findById(id)
                .orElseThrow(DataNotFoundException::new);
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
            throw new DataNotFoundException();
        }

        getRepository().deleteById(id);
    }

    default void delete(T entity) {
        requireNonNull(entity, "Entity must not be null");

        ID id = entity.getId();
        if (id == null) {
            throw new IllegalArgumentException("Entity id must not be null");
        }

        delete(id);
    }
}
