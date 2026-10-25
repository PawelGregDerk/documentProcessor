package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.SborEd;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SborEdMapperTest {

    private final SborEdMapper mapper = new SborEdMapperImpl();

    @Test
    @DisplayName("toDto: null сущность преобразуется в null")
    void toDto_nullEntity_returnsNull() {
        SborEdDto dto = mapper.toDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toDto: корректно отображает все поля и idDocpackage")
    void toDto_validEntity_mapsAllFields() {
        Docpackage docpackage = Docpackage.builder().id(123L).build();
        SborEd entity = SborEd.builder()
                .id(1L)
                .nazv("Вал привода")
                .oboznach("ВП.01.002")
                .docpackage(docpackage)
                .build();

        SborEdDto dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.nazv()).isEqualTo("Вал привода");
        assertThat(dto.oboznach()).isEqualTo("ВП.01.002");
        assertThat(dto.idDocpackage()).isEqualTo(123L);
    }

    @Test
    @DisplayName("toEntity: null dto преобразуется в null")
    void toEntity_nullDto_returnsNull() {
        SborEd entity = mapper.toEntity(null);

        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("toEntity: корректно отображает поля DTO (docpackage игнорируется)")
    void toEntity_validDto_mapsAllFields() {
        SborEdDto dto = new SborEdDto(5L, 123L, "Корпус", "КП.00.001");

        SborEd entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getNazv()).isEqualTo("Корпус");
        assertThat(entity.getOboznach()).isEqualTo("КП.00.001");
        assertThat(entity.getDocpackage()).isNull();
    }

    @Test
    @DisplayName("toDtoList и toEntityList: списки корректно маппятся")
    void toDtoListAndEntityList_collections_mappedSuccessfully() {
        assertThat(mapper.toDtoList(null)).isNull();
        assertThat(mapper.toEntityList(null)).isNull();

        SborEd sborEd = SborEd.builder().id(1L).oboznach("001").build();
        List<SborEdDto> dtos = mapper.toDtoList(List.of(sborEd));

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).oboznach()).isEqualTo("001");
    }
}
