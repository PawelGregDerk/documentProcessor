package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.excepts.DataNotFoundException;
import by.vstu.isit.documentprocessor.mappers.db.TypeOperMapper;
import by.vstu.isit.documentprocessor.repositories.TypeOperRepository;
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
class TypeOperServiceImplTest {

    @Mock
    private TypeOperRepository repository;

    @Mock
    private TypeOperMapper typeOperMapper;

    @InjectMocks
    private TypeOperServiceImpl service;

    @Test
    @DisplayName("getById: при существующем ID возвращает операцию с функциями")
    void getById_existingId_returnsTypeOper() {
        TypeOper typeOper = TypeOper.builder().id(1L).name("Токарная").build();
        when(repository.findWithFuncsById(1L)).thenReturn(Optional.of(typeOper));

        TypeOper result = service.getById(1L);

        assertThat(result).isSameAs(typeOper);
        verify(repository).findWithFuncsById(1L);
    }

    @Test
    @DisplayName("getById: при несуществующем ID выбрасывает DataNotFoundException")
    void getById_nonExistingId_throwsDataNotFoundException() {
        when(repository.findWithFuncsById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @DisplayName("save: сохраняет типовую операцию через репозиторий")
    void save_validEntity_savedSuccessfully() {
        TypeOper entity = TypeOper.builder().name("Фрезерная").build();
        when(repository.save(entity)).thenReturn(entity);

        TypeOper result = service.save(entity);

        assertThat(result).isSameAs(entity);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("findAll: возвращает список DTO через маппер")
    void findAll_existingEntities_returnsDtoList() {
        TypeOper op = TypeOper.builder().id(1L).name("Сборка").build();
        TypeOperDto dto = new TypeOperDto(1L, "001", "15", "O", "Os", "Сборка", "S", "1", List.of());

        when(repository.findAll()).thenReturn(List.of(op));
        when(typeOperMapper.toDtoList(List.of(op))).thenReturn(List.of(dto));

        List<TypeOperDto> result = service.findAll();

        assertThat(result).containsExactly(dto);
    }

    @Test
    @DisplayName("updateTypeOper: обновляет и сохраняет сущность")
    void updateTypeOper_validEntity_returnsSavedEntity() {
        TypeOper op = TypeOper.builder().id(1L).name("Обновленная").build();
        when(repository.save(op)).thenReturn(op);

        TypeOper result = service.updateTypeOper(op);

        assertThat(result).isSameAs(op);
        verify(repository).save(op);
    }

    @Test
    @DisplayName("delete(id): если сущность не существует, выбрасывает DataNotFoundException")
    void delete_nonExistingId_throwsDataNotFoundException() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(DataNotFoundException.class);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete(id): если сущность существует, удаляет по ID")
    void delete_existingId_deletesSuccessfully() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete(entity): при entity == null выбрасывает NullPointerException")
    void delete_nullEntity_throwsNullPointerException() {
        assertThatThrownBy(() -> service.delete((TypeOper) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Entity must not be null");
    }

    @Test
    @DisplayName("delete(entity): при entity.getId() == null выбрасывает IllegalArgumentException")
    void delete_entityWithNullId_throwsIllegalArgumentException() {
        TypeOper entity = TypeOper.builder().id(null).build();

        assertThatThrownBy(() -> service.delete(entity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Entity id must not be null");
    }
}
