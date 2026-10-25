package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import by.vstu.isit.documentprocessor.repositories.TypeOperFuncRepository;
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
class TypeOperFuncServiceImplTest {

    @Mock
    private TypeOperFuncRepository repository;

    @InjectMocks
    private TypeOperFuncServiceImpl service;

    @Test
    @DisplayName("updateTypeOperFunc: сохраняет функцию типовой операции через репозиторий")
    void updateTypeOperFunc_validEntity_savedSuccessfully() {
        TypeOperFunc entity = TypeOperFunc.builder().id(1L).name("Зенкование").build();
        when(repository.save(entity)).thenReturn(entity);

        TypeOperFunc result = service.updateTypeOperFunc(entity);

        assertThat(result).isSameAs(entity);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("getById: возвращает функцию типовой операции")
    void getById_existingId_returnsTypeOperFunc() {
        TypeOperFunc entity = TypeOperFunc.builder().id(10L).name("Нарезка резьбы").build();
        when(repository.findById(10L)).thenReturn(Optional.of(entity));

        TypeOperFunc result = service.getById(10L);

        assertThat(result).isSameAs(entity);
    }
}
