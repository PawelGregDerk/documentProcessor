package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.excepts.DataNotFoundException;
import by.vstu.isit.documentprocessor.repositories.OperRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperServiceImplTest {

    @Mock
    private OperRepository repository;

    @InjectMocks
    private OperServiceImpl service;

    @Test
    @DisplayName("updateOper: сохраняет операцию через репозиторий")
    void updateOper_validEntity_savedSuccessfully() {
        Oper oper = Oper.builder().id(1L).numOper("005").name("Токарная").build();
        when(repository.save(oper)).thenReturn(oper);

        Oper result = service.updateOper(oper);

        assertThat(result).isSameAs(oper);
        verify(repository).save(oper);
    }

    @Test
    @DisplayName("getById: при существующем ID возвращает операцию")
    void getById_existingId_returnsOper() {
        Oper oper = Oper.builder().id(2L).numOper("010").build();
        when(repository.findById(2L)).thenReturn(Optional.of(oper));

        Oper result = service.getById(2L);

        assertThat(result).isSameAs(oper);
    }

    @Test
    @DisplayName("getById: при отсутствии ID выбрасывает DataNotFoundException")
    void getById_nonExistingId_throwsDataNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @DisplayName("getAll: возвращает список всех операций")
    void getAll_invoke_returnsAllFromRepository() {
        Oper oper1 = Oper.builder().id(1L).build();
        Oper oper2 = Oper.builder().id(2L).build();
        when(repository.findAll()).thenReturn(List.of(oper1, oper2));

        List<Oper> result = service.getAll();

        assertThat(result).containsExactly(oper1, oper2);
    }

    @Test
    @DisplayName("delete: удаляет операцию по ID при ее существовании")
    void delete_existingId_callsDeleteById() {
        when(repository.existsById(10L)).thenReturn(true);

        service.delete(10L);

        verify(repository).deleteById(10L);
    }
}
