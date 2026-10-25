package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.entities.Oper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FuncMapperTest {

    private final FuncMapper mapper = new FuncMapperImpl();

    @Test
    @DisplayName("toDto: null сущность возвращает null")
    void toDto_nullEntity_returnsNull() {
        FuncDto dto = mapper.toDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toDto: корректно отображает все поля и idOper")
    void toDto_validEntity_mapsAllFields() {
        Oper oper = Oper.builder().id(55L).build();
        Func func = Func.builder()
                .id(1L)
                .name("Очистить контакты")
                .param("Чистота поверхности 100%")
                .isProd(true)
                .specCharakt("Класс А")
                .oper(oper)
                .build();

        FuncDto dto = mapper.toDto(func);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Очистить контакты");
        assertThat(dto.param()).isEqualTo("Чистота поверхности 100%");
        assertThat(dto.isProd()).isTrue();
        assertThat(dto.specCharakt()).isEqualTo("Класс А");
        assertThat(dto.idOper()).isEqualTo(55L);
    }

    @Test
    @DisplayName("toEntity: null dto возвращает null")
    void toEntity_nullDto_returnsNull() {
        Func entity = mapper.toEntity(null);

        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("toEntity: корректно отображает все поля в сущность (oper игнорируется)")
    void toEntity_validDto_mapsAllFields() {
        FuncDto dto = new FuncDto(2L, "Запрессовка", 55L, "Усилие 50Н", false, null);

        Func entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getName()).isEqualTo("Запрессовка");
        assertThat(entity.getParam()).isEqualTo("Усилие 50Н");
        assertThat(entity.getIsProd()).isFalse();
        assertThat(entity.getSpecCharakt()).isNull();
        assertThat(entity.getOper()).isNull(); // oper игнорируется в маппинге
    }

    @Test
    @DisplayName("toDtoList и toEntityList: списки корректно маппятся")
    void toDtoListAndEntityList_collections_mappedSuccessfully() {
        assertThat(mapper.toDtoList(null)).isNull();
        assertThat(mapper.toEntityList(null)).isNull();

        Func func = Func.builder().id(1L).name("Контроль").build();
        List<FuncDto> dtos = mapper.toDtoList(List.of(func));

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).name()).isEqualTo("Контроль");
    }
}
