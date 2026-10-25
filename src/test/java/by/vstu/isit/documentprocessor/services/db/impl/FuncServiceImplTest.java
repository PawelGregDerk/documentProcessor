package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.repositories.FuncRepository;
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
class FuncServiceImplTest {

    @Mock
    private FuncRepository repository;

    @InjectMocks
    private FuncServiceImpl service;

    @Test
    @DisplayName("updateFunc: сохраняет сущность Func через репозиторий")
    void updateFunc_validEntity_savedSuccessfully() {
        Func func = Func.builder().id(1L).name("Очистить контакты").build();
        when(repository.save(func)).thenReturn(func);

        Func result = service.updateFunc(func);

        assertThat(result).isSameAs(func);
        verify(repository).save(func);
    }

    @Test
    @DisplayName("getById: возвращает найденную сущность Func")
    void getById_existingId_returnsFunc() {
        Func func = Func.builder().id(2L).name("Запрессовка").build();
        when(repository.findById(2L)).thenReturn(Optional.of(func));

        Func result = service.getById(2L);

        assertThat(result).isSameAs(func);
    }
}
