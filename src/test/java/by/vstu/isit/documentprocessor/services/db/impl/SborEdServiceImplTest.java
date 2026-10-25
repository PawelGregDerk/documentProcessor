package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.entities.SborEd;
import by.vstu.isit.documentprocessor.repositories.SborEdRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SborEdServiceImplTest {

    @Mock
    private SborEdRepository repository;

    @InjectMocks
    private SborEdServiceImpl service;

    @Test
    @DisplayName("updateSborEd: сохраняет сборочную единицу через репозиторий")
    void updateSborEd_validEntity_savedSuccessfully() {
        SborEd entity = SborEd.builder().id(1L).nazv("Вал").oboznach("001").build();
        when(repository.save(entity)).thenReturn(entity);

        SborEd result = service.updateSborEd(entity);

        assertThat(result).isSameAs(entity);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("getById: возвращает сборочную единицу по ID")
    void getById_existingId_returnsSborEd() {
        SborEd entity = SborEd.builder().id(3L).nazv("Корпус").build();
        when(repository.findById(3L)).thenReturn(Optional.of(entity));

        SborEd result = service.getById(3L);

        assertThat(result).isSameAs(entity);
    }
}
