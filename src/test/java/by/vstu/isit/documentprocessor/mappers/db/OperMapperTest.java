package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.entities.Oper;
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
class OperMapperTest {

    @Mock
    private FuncMapper funcMapper;

    @InjectMocks
    private OperMapperImpl mapper;

    @Test
    @DisplayName("toDto: null сущность преобразуется в null")
    void toDto_nullEntity_returnsNull() {
        OperDto result = mapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toDto: корректно отображает поля сущности и извлекает idDocPackage")
    void toDto_validEntity_mapsAllFields() {
        Docpackage docpackage = Docpackage.builder().id(77L).build();
        Func func = Func.builder().id(1L).name("Отрезка").build();
        FuncDto funcDto = new FuncDto(1L, "Отрезка", null, "0.5", false, null);

        Oper oper = Oper.builder()
                .id(10L)
                .numOper("005")
                .nomInstr("15-10")
                .oborud("Токарный 16К20")
                .ostnasInstr("Резец")
                .name("Токарная черновая")
                .shifr("Т05")
                .numZech("15")
                .docpackage(docpackage)
                .funcs(List.of(func))
                .build();

        when(funcMapper.toDtoList(List.of(func))).thenReturn(List.of(funcDto));

        OperDto dto = mapper.toDto(oper);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.idDocPackage()).isEqualTo(77L);
        assertThat(dto.numOper()).isEqualTo("005");
        assertThat(dto.nomInstr()).isEqualTo("15-10");
        assertThat(dto.oborud()).isEqualTo("Токарный 16К20");
        assertThat(dto.ostnasInstr()).isEqualTo("Резец");
        assertThat(dto.name()).isEqualTo("Токарная черновая");
        assertThat(dto.shifr()).isEqualTo("Т05");
        assertThat(dto.numZech()).isEqualTo("15");
        assertThat(dto.funcs()).containsExactly(funcDto);
    }

    @Test
    @DisplayName("toEntity: null dto преобразуется в null")
    void toEntity_nullDto_returnsNull() {
        Oper result = mapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toEntity: корректно отображает поля DTO в сущность")
    void toEntity_validDto_mapsAllFields() {
        FuncDto funcDto = new FuncDto(1L, "Отрезка", null, "0.5", false, null);
        Func func = Func.builder().id(1L).name("Отрезка").build();
        OperDto dto = new OperDto(10L, 77L, "005", "15-10", "Станок", "Оснастка", "Операция", "Ш1", "15", List.of(funcDto));

        when(funcMapper.toEntityList(List.of(funcDto))).thenReturn(List.of(func));

        Oper entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getNumOper()).isEqualTo("005");
        assertThat(entity.getNomInstr()).isEqualTo("15-10");
        assertThat(entity.getOborud()).isEqualTo("Станок");
        assertThat(entity.getOstnasInstr()).isEqualTo("Оснастка");
        assertThat(entity.getName()).isEqualTo("Операция");
        assertThat(entity.getShifr()).isEqualTo("Ш1");
        assertThat(entity.getNumZech()).isEqualTo("15");
        assertThat(entity.getFuncs()).containsExactly(func);
    }

    @Test
    @DisplayName("setOperFk: проверяет логику @AfterMapping по установке внешнего ключа docpackage")
    void setOperFk_dtoWithDocpackageId_setsDocpackageOnEntity() {
        Oper entity = Oper.builder().build();
        OperDto dto = new OperDto(1L, 99L, "005", "1", "o", "os", "n", "s", "1", List.of());

        mapper.setOperFk(entity, dto);

        assertThat(entity.getDocpackage()).isNotNull();
        assertThat(entity.getDocpackage().getId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("toDtoList и toEntityList: корректно обрабатывают списки и null")
    void toDtoListAndEntityList_collections_mappedSuccessfully() {
        assertThat(mapper.toDtoList(null)).isNull();
        assertThat(mapper.toEntityList(null)).isNull();

        Oper oper = Oper.builder().id(1L).numOper("001").build();
        List<OperDto> dtos = mapper.toDtoList(List.of(oper));
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).numOper()).isEqualTo("001");
    }
}
