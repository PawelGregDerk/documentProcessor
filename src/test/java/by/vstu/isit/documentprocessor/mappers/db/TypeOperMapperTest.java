package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeOperMapperTest {

    @Mock
    private TypeOperFuncMapper typeOperFuncMapper;

    @InjectMocks
    private TypeOperMapperImpl mapper;

    @Test
    @DisplayName("toDto: null сущность преобразуется в null")
    void toDto_nullEntity_returnsNull() {
        TypeOperDto dto = mapper.toDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toDto: все поля и вложенные функции корректно маппятся")
    void toDto_validEntity_mapsAllFields() {
        TypeOperFunc func = TypeOperFunc.builder().id(1L).name("Шлифовка").build();
        TypeOperFuncDto funcDto = new TypeOperFuncDto(1L, "Шлифовка", 10L, "Ra 0.8", false, null);

        TypeOper entity = TypeOper.builder()
                .id(10L)
                .numOper("020")
                .nomInstr("15-20")
                .oborud("Шлифовальный")
                .ostnasInstr("Круг")
                .name("Круглое шлифование")
                .shifr("ШЛ01")
                .numZech("15")
                .typeOperFuncs(List.of(func))
                .build();

        when(typeOperFuncMapper.toDto(func)).thenReturn(funcDto);

        TypeOperDto dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.numOper()).isEqualTo("020");
        assertThat(dto.nomInstr()).isEqualTo("15-20");
        assertThat(dto.oborud()).isEqualTo("Шлифовальный");
        assertThat(dto.ostnasInstr()).isEqualTo("Круг");
        assertThat(dto.name()).isEqualTo("Круглое шлифование");
        assertThat(dto.shifr()).isEqualTo("ШЛ01");
        assertThat(dto.numZech()).isEqualTo("15");
        assertThat(dto.funcs()).containsExactly(funcDto);
    }

    @Test
    @DisplayName("toEntity: null dto преобразуется в null")
    void toEntity_nullDto_returnsNull() {
        TypeOper entity = mapper.toEntity(null);

        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("toEntity: поля DTO корректно маппятся (typeOperFuncs игнорируется)")
    void toEntity_validDto_mapsAllFields() {
        TypeOperDto dto = new TypeOperDto(10L, "020", "15-20", "Станок", "Оснастка", "Название", "ШИФР", "15", List.of());

        TypeOper entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getNumOper()).isEqualTo("020");
        assertThat(entity.getNomInstr()).isEqualTo("15-20");
        assertThat(entity.getOborud()).isEqualTo("Станок");
        assertThat(entity.getOstnasInstr()).isEqualTo("Оснастка");
        assertThat(entity.getName()).isEqualTo("Название");
        assertThat(entity.getShifr()).isEqualTo("ШИФР");
        assertThat(entity.getNumZech()).isEqualTo("15");
        assertThat(entity.getTypeOperFuncs()).isNull();
    }

    @Test
    @DisplayName("toDtoList и toEntityList: списки корректно маппятся")
    void toDtoListAndEntityList_collections_mappedSuccessfully() {
        assertThat(mapper.toDtoList(null)).isNull();
        assertThat(mapper.toEntityList(null)).isNull();

        TypeOper op = TypeOper.builder().id(1L).numOper("001").build();
        List<TypeOperDto> dtos = mapper.toDtoList(List.of(op));

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).numOper()).isEqualTo("001");
    }
}
