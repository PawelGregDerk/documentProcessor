package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.TypeOperFuncDto;
import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeOperFuncMapperTest {

    private final TypeOperFuncMapper mapper = new TypeOperFuncMapperImpl();

    @Test
    @DisplayName("toDto: null сущность преобразуется в null")
    void toDto_nullEntity_returnsNull() {
        TypeOperFuncDto dto = mapper.toDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toDto: корректно отображает все поля и idTypeOper")
    void toDto_validEntity_mapsAllFields() {
        TypeOper typeOper = TypeOper.builder().id(99L).build();
        TypeOperFunc entity = TypeOperFunc.builder()
                .id(1L)
                .name("Фрезерование паза")
                .param("Глубина 5мм")
                .isProd(true)
                .specCharakt("Кл.1")
                .typeOper(typeOper)
                .build();

        TypeOperFuncDto dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Фрезерование паза");
        assertThat(dto.param()).isEqualTo("Глубина 5мм");
        assertThat(dto.isProd()).isTrue();
        assertThat(dto.specCharakt()).isEqualTo("Кл.1");
        assertThat(dto.idTypeOper()).isEqualTo(99L);
    }

    @Test
    @DisplayName("toEntity: null dto преобразуется в null")
    void toEntity_nullDto_returnsNull() {
        TypeOperFunc entity = mapper.toEntity(null);

        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("toEntity: корректно отображает все поля в сущность (typeOper игнорируется)")
    void toEntity_validDto_mapsAllFields() {
        TypeOperFuncDto dto = new TypeOperFuncDto(5L, "Сверление", 99L, "Диаметр 10", false, null);

        TypeOperFunc entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getName()).isEqualTo("Сверление");
        assertThat(entity.getParam()).isEqualTo("Диаметр 10");
        assertThat(entity.getIsProd()).isFalse();
        assertThat(entity.getTypeOper()).isNull();
    }
}
