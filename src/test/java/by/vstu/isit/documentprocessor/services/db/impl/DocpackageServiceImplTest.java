package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.Func;
import by.vstu.isit.documentprocessor.entities.Oper;
import by.vstu.isit.documentprocessor.entities.SborEd;
import by.vstu.isit.documentprocessor.excepts.DataNotFoundException;
import by.vstu.isit.documentprocessor.mappers.db.DocpackageMapper;
import by.vstu.isit.documentprocessor.mappers.db.FuncMapper;
import by.vstu.isit.documentprocessor.mappers.db.OperMapper;
import by.vstu.isit.documentprocessor.mappers.db.SborEdMapper;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
import by.vstu.isit.documentprocessor.repositories.OperRepository;
import by.vstu.isit.documentprocessor.repositories.SborEdRepository;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocpackageServiceImplTest {

    @Mock
    private DocpackageRepository repository;
    @Mock
    private DocpackageMapper docpackageMapper;
    @Mock
    private OperMapper operMapper;
    @Mock
    private FuncMapper funcMapper;
    @Mock
    private SborEdMapper sborEdMapper;
    @Mock
    private SborEdRepository sborEdRepository;
    @Mock
    private OperRepository operRepository;

    @InjectMocks
    private DocpackageServiceImpl service;

    @Test
    @DisplayName("updateDocpackage: сохраняет сущность через репозиторий")
    void updateDocpackage_validEntity_savedSuccessfully() {
        Docpackage entity = Docpackage.builder().id(1L).packageName("TestPkg").build();
        when(repository.save(entity)).thenReturn(entity);

        Docpackage result = service.updateDocpackage(entity);

        assertThat(result).isSameAs(entity);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("saveFullPackage: каскадно преобразует и сохраняет пакет, операции, функции и сборочные единицы")
    void saveFullPackage_fullDto_cascadesAndSaves() {
        FuncDto funcDto = new FuncDto(null, "Контроль", null, "0.1", true, "S");
        OperDto operDto = new OperDto(null, null, "005", "15-1", "Станок", "Оснастка", "Токарная", "Т01", "15", List.of(funcDto));
        SborEdDto sborEdDto = new SborEdDto(null, null, "Вал", "ВЛ-01");
        DockPackageDto inputDto = new DockPackageDto(null, "Пакет1", "path/1", "ПУ01", "СХ01", "КП01", "Ф01", "ВИ01", List.of(operDto), List.of(sborEdDto));

        Docpackage entity = Docpackage.builder().packageName("Пакет1").build();
        Oper operEntity = Oper.builder().numOper("005").build();
        Func funcEntity = Func.builder().name("Контроль").build();
        SborEd sborEdEntity = SborEd.builder().nazv("Вал").build();

        when(docpackageMapper.toEntity(inputDto)).thenReturn(entity);
        when(operMapper.toEntity(operDto)).thenReturn(operEntity);
        when(funcMapper.toEntity(funcDto)).thenReturn(funcEntity);
        when(sborEdMapper.toEntity(sborEdDto)).thenReturn(sborEdEntity);
        when(repository.save(any(Docpackage.class))).thenReturn(entity);
        when(docpackageMapper.toDto(entity)).thenReturn(inputDto);

        DockPackageDto result = service.saveFullPackage(inputDto);

        assertThat(result).isNotNull();
        assertThat(operEntity.getDocpackage()).isSameAs(entity);
        assertThat(funcEntity.getOper()).isSameAs(operEntity);
        assertThat(sborEdEntity.getDocpackage()).isSameAs(entity);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("updateFullPackage: при dto.id == null выбрасывает IllegalArgumentException")
    void updateFullPackage_nullId_throwsIllegalArgumentException() {
        DockPackageDto dto = new DockPackageDto(null, "Pkg", "p", "pu", "spu", "kp", "fmea", "vi", List.of(), List.of());

        assertThatThrownBy(() -> service.updateFullPackage(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID пакета не может быть null");
    }

    @Test
    @DisplayName("updateFullPackage: если пакет не найден в базе, выбрасывает DataNotFoundException")
    void updateFullPackage_packageNotFound_throwsDataNotFoundException() {
        DockPackageDto dto = new DockPackageDto(999L, "Pkg", "p", "pu", "spu", "kp", "fmea", "vi", List.of(), List.of());
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateFullPackage(dto))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @DisplayName("updateFullPackage: порядок операций не изменился -> обновляет на месте без смены ID (orderChanged = false)")
    void updateFullPackage_orderNotChanged_mergesInPlace() {
        Oper existingOper = Oper.builder().id(10L).numOper("005").funcs(new ArrayList<>()).build();
        Docpackage existing = Docpackage.builder().id(1L).packageName("Old").opers(new ArrayList<>(List.of(existingOper))).sborEds(new ArrayList<>()).build();

        OperDto updatedOperDto = new OperDto(10L, 1L, "005-New", "15", "Obr", "Ost", "Name", "Sh", "1", List.of());
        DockPackageDto inputDto = new DockPackageDto(1L, "NewPkg", "p", "pu", "spu", "kp", "fmea", "vi", List.of(updatedOperDto), List.of());

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(docpackageMapper.toDto(existing)).thenReturn(inputDto);

        DocpackageService.UpdateResult result = service.updateFullPackage(inputDto);

        assertThat(result.orderChanged()).isFalse();
        assertThat(existing.getPackageName()).isEqualTo("NewPkg");
        assertThat(existingOper.getNumOper()).isEqualTo("005-New");
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("updateFullPackage: порядок операций изменился -> пересоздает коллекции (orderChanged = true)")
    void updateFullPackage_orderChanged_recreatesCollections() {
        Oper oper1 = Oper.builder().id(10L).numOper("005").funcs(new ArrayList<>()).build();
        Oper oper2 = Oper.builder().id(20L).numOper("010").funcs(new ArrayList<>()).build();
        Docpackage existing = Docpackage.builder().id(1L).packageName("Old").opers(new ArrayList<>(List.of(oper1, oper2))).sborEds(new ArrayList<>()).build();

        // Меняем порядок операций: сначала id=20, потом id=10
        OperDto dto2 = new OperDto(20L, 1L, "010", "i", "o", "os", "n", "s", "1", List.of());
        OperDto dto1 = new OperDto(10L, 1L, "005", "i", "o", "os", "n", "s", "1", List.of());
        DockPackageDto inputDto = new DockPackageDto(1L, "NewPkg", "p", "pu", "spu", "kp", "fmea", "vi", List.of(dto2, dto1), List.of());

        Oper newOper2 = Oper.builder().numOper("010").build();
        Oper newOper1 = Oper.builder().numOper("005").build();
        when(operMapper.toEntity(dto2)).thenReturn(newOper2);
        when(operMapper.toEntity(dto1)).thenReturn(newOper1);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(docpackageMapper.toDto(existing)).thenReturn(inputDto);

        DocpackageService.UpdateResult result = service.updateFullPackage(inputDto);

        assertThat(result.orderChanged()).isTrue();
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("findDtoById: возвращает найденный DTO пакета")
    void findDtoById_existingId_returnsDto() {
        Docpackage entity = Docpackage.builder().id(1L).packageName("Pkg").build();
        DockPackageDto dto = new DockPackageDto(1L, "Pkg", "p", "pu", "spu", "kp", "fmea", "vi", List.of(), List.of());

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(docpackageMapper.toDto(entity)).thenReturn(dto);

        DockPackageDto result = service.findDtoById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("searchByPackageName: возвращает найденные пакеты по подстроке")
    void searchByPackageName_query_returnsMatchingDtos() {
        Docpackage pkg = Docpackage.builder().id(1L).packageName("АБВГ.123").build();
        DockPackageDto dto = new DockPackageDto(1L, "АБВГ.123", "p", "pu", "spu", "kp", "fmea", "vi", List.of(), List.of());

        when(repository.findByPackageNameContainingIgnoreCase("123")).thenReturn(List.of(pkg));
        when(docpackageMapper.toDto(pkg)).thenReturn(dto);

        List<DockPackageDto> results = service.searchByPackageName("123");

        assertThat(results).containsExactly(dto);
    }

    @Test
    @DisplayName("searchByOboznach: возвращает уникальные пакеты по обозначению сборочной единицы")
    void searchByOboznach_query_returnsUniquePackageDtos() {
        Docpackage pkg = Docpackage.builder().id(5L).packageName("Корпус").build();
        SborEd se = SborEd.builder().id(1L).oboznach("СБ-100").docpackage(pkg).build();
        DockPackageDto dto = new DockPackageDto(5L, "Корпус", "p", "pu", "spu", "kp", "fmea", "vi", List.of(), List.of());

        when(sborEdRepository.findByOboznachContainingIgnoreCase("СБ")).thenReturn(List.of(se));
        when(docpackageMapper.toDto(pkg)).thenReturn(dto);

        List<DockPackageDto> results = service.searchByOboznach("СБ");

        assertThat(results).containsExactly(dto);
    }
}
