package by.vstu.isit.documentprocessor.mappers.db;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.entities.SborEd;
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
class DocpackageMapperTest {

    @Mock
    private OperMapper operMapper;

    @Mock
    private SborEdMapper sborEdMapper;

    @InjectMocks
    private DocpackageMapperImpl mapper;

    @Test
    @DisplayName("toDto: null сущность возвращает null")
    void toDto_nullEntity_returnsNull() {
        DockPackageDto dto = mapper.toDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toDto: все поля пакета, операции и сборочные единицы маппятся корректно")
    void toDto_validEntity_mapsAllFields() {
        Oper oper = Oper.builder().id(10L).numOper("005").build();
        SborEd sborEd = SborEd.builder().id(20L).oboznach("СБ-1").build();
        OperDto operDto = new OperDto(10L, 1L, "005", "1", "o", "os", "n", "s", "1", List.of());
        SborEdDto sborEdDto = new SborEdDto(20L, 1L, "Деталь", "СБ-1");

        Docpackage entity = Docpackage.builder()
                .id(1L)
                .packageName("Изделие-100")
                .path("orders/100")
                .puName("ПУ01")
                .spuName("СХ01")
                .kpName("КП01")
                .fmeaName("Т01")
                .vedIName("ВИ01")
                .opers(List.of(oper))
                .sborEds(List.of(sborEd))
                .build();

        when(operMapper.toDtoList(List.of(oper))).thenReturn(List.of(operDto));
        when(sborEdMapper.toDtoList(List.of(sborEd))).thenReturn(List.of(sborEdDto));

        DockPackageDto dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.packageName()).isEqualTo("Изделие-100");
        assertThat(dto.path()).isEqualTo("orders/100");
        assertThat(dto.puName()).isEqualTo("ПУ01");
        assertThat(dto.spuName()).isEqualTo("СХ01");
        assertThat(dto.kpName()).isEqualTo("КП01");
        assertThat(dto.fmeaName()).isEqualTo("Т01");
        assertThat(dto.vedIName()).isEqualTo("ВИ01");
        assertThat(dto.opers()).containsExactly(operDto);
        assertThat(dto.sborEds()).containsExactly(sborEdDto);
    }

    @Test
    @DisplayName("toEntity: null dto возвращает null")
    void toEntity_nullDto_returnsNull() {
        Docpackage entity = mapper.toEntity(null);

        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("toEntity: все поля DTO маппятся в сущность Docpackage")
    void toEntity_validDto_mapsAllFields() {
        OperDto operDto = new OperDto(10L, 1L, "005", "1", "o", "os", "n", "s", "1", List.of());
        SborEdDto sborEdDto = new SborEdDto(20L, 1L, "Деталь", "СБ-1");
        DockPackageDto dto = new DockPackageDto(1L, "Изделие-100", "orders/100", "ПУ01", "СХ01", "КП01", "Т01", "ВИ01", List.of(operDto), List.of(sborEdDto));

        Oper oper = Oper.builder().id(10L).build();
        SborEd sborEd = SborEd.builder().id(20L).build();

        when(operMapper.toEntityList(List.of(operDto))).thenReturn(List.of(oper));
        when(sborEdMapper.toEntityList(List.of(sborEdDto))).thenReturn(List.of(sborEd));

        Docpackage entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getPackageName()).isEqualTo("Изделие-100");
        assertThat(entity.getPath()).isEqualTo("orders/100");
        assertThat(entity.getPuName()).isEqualTo("ПУ01");
        assertThat(entity.getSpuName()).isEqualTo("СХ01");
        assertThat(entity.getKpName()).isEqualTo("КП01");
        assertThat(entity.getFmeaName()).isEqualTo("Т01");
        assertThat(entity.getVedIName()).isEqualTo("ВИ01");
        assertThat(entity.getOpers()).containsExactly(oper);
        assertThat(entity.getSborEds()).containsExactly(sborEd);
    }
}
