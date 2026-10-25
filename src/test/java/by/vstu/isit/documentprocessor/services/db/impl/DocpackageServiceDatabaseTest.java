package by.vstu.isit.documentprocessor.services.db.impl;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.entities.TypeOper;
import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
import by.vstu.isit.documentprocessor.excepts.DataNotFoundException;
import by.vstu.isit.documentprocessor.repositories.DocpackageRepository;
import by.vstu.isit.documentprocessor.repositories.FuncRepository;
import by.vstu.isit.documentprocessor.repositories.SborEdRepository;
import by.vstu.isit.documentprocessor.repositories.TypeOperRepository;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.main.web-application-type=none")
@Transactional
@EnabledIf("databaseAvailable")
class DocpackageServiceDatabaseTest {

    private static final String LOCAL_URL =
            "jdbc:mysql://localhost:3306/vsep?useSSL=false&serverTimezone=UTC&connectTimeout=3000";

    @Autowired
    private DocpackageService service;
    @Autowired
    private DocpackageRepository repository;
    @Autowired
    private FuncRepository funcRepository;
    @Autowired
    private SborEdRepository sborEdRepository;
    @Autowired
    private TypeOperService typeOperService;
    @Autowired
    private TypeOperRepository typeOperRepository;
    @Autowired
    private EntityManager entityManager;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        Properties local = localConfig();
        registry.add("spring.datasource.url", () -> LOCAL_URL);
        registry.add("spring.datasource.username",
                () -> local.getProperty("spring.datasource.username", ""));
        registry.add("spring.datasource.password",
                () -> local.getProperty("spring.datasource.password", ""));
        registry.add("spring.datasource.driverClassName", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    static boolean databaseAvailable() {
        Properties local = localConfig();
        String user = local.getProperty("spring.datasource.username");
        String password = local.getProperty("spring.datasource.password");
        if (user == null || password == null) {
            return false;
        }
        try (Connection ignored = DriverManager.getConnection(LOCAL_URL, user, password)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Properties localConfig() {
        Path path = Path.of(System.getProperty("user.dir"), "config", "application.properties");
        Properties props = new Properties();
        if (!Files.exists(path)) {
            return props;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (Exception ignored) {
            // tests will be skipped if credentials could not be read
        }
        return props;
    }

    @Test
    @DisplayName("saveFullPackage: реальная БД — сохраняет пакет, операции, функции и сборочные единицы")
    void saveFullPackage_persistsWholeGraph() {
        DockPackageDto saved = service.saveFullPackage(packageDto(marker()));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.opers()).hasSize(2);
        assertThat(saved.sborEds()).hasSize(2);

        assertThat(saved.opers()).allSatisfy(oper -> {
            assertThat(oper.id()).isNotNull();
            assertThat(oper.funcs()).isNotEmpty();
            assertThat(oper.funcs()).allSatisfy(func -> {
                assertThat(func.id()).isNotNull();
                assertThat(func.idOper()).isEqualTo(oper.id());
            });
        });
        assertThat(saved.sborEds()).allSatisfy(sborEd -> {
            assertThat(sborEd.id()).isNotNull();
            assertThat(sborEd.idDocpackage()).isEqualTo(saved.id());
        });

        Docpackage entity = repository.findById(saved.id()).orElseThrow();
        assertThat(entity.getOpers()).hasSize(2);
        assertThat(entity.getSborEds()).hasSize(2);
        assertThat(entity.getOpers().stream().flatMap(oper -> oper.getFuncs().stream()).count())
                .isEqualTo(3);
    }

    @Test
    @DisplayName("updateFullPackage: без смены порядка обновляет на месте и сохраняет ID")
    void updateFullPackage_withoutOrderChange_mergesInPlace() {
        DockPackageDto saved = service.saveFullPackage(packageDto(marker()));
        OperDto first = saved.opers().get(0);
        OperDto second = saved.opers().get(1);
        FuncDto keptFunc = first.funcs().get(0);
        FuncDto removedFunc = first.funcs().get(1);
        SborEdDto keptSborEd = saved.sborEds().get(0);
        SborEdDto removedSborEd = saved.sborEds().get(1);

        FuncDto updatedFunc = new FuncDto(
                keptFunc.id(), "Обновлённая функция", keptFunc.idOper(), "Новый параметр", true, "S2");
        FuncDto newFunc = new FuncDto(null, "Новая функция", null, "Новый параметр 2", false, "");
        OperDto updatedFirst = new OperDto(
                first.id(), saved.id(), "005", "15-132/005",
                "Станок NEW", "Оснастка NEW", "Токарная NEW", "Т01", "16",
                List.of(updatedFunc, newFunc));
        SborEdDto updatedSborEd = new SborEdDto(
                keptSborEd.id(), keptSborEd.idDocpackage(), "Вал NEW", keptSborEd.oboznach());
        SborEdDto newSborEd = new SborEdDto(null, null, "Крышка", "TEST-" + marker() + "-NEW");
        DockPackageDto updated = new DockPackageDto(
                saved.id(), "TEST-PKG-UPDATED", saved.path(), saved.puName(), saved.spuName(),
                saved.kpName(), saved.fmeaName(), saved.vedIName(),
                List.of(updatedFirst, second), List.of(updatedSborEd, newSborEd));

        DocpackageService.UpdateResult result = service.updateFullPackage(updated);

        assertThat(result.orderChanged()).isFalse();
        DockPackageDto dto = result.dto();
        assertThat(dto.packageName()).isEqualTo("TEST-PKG-UPDATED");
        assertThat(dto.opers()).hasSize(2);
        assertThat(dto.opers().get(0).id()).isEqualTo(first.id());
        assertThat(dto.opers().get(0).name()).isEqualTo("Токарная NEW");
        assertThat(dto.opers().get(0).funcs()).hasSize(2);
        assertThat(dto.opers().get(0).funcs().get(0).id()).isEqualTo(keptFunc.id());
        assertThat(dto.opers().get(0).funcs().get(0).name()).isEqualTo("Обновлённая функция");
        assertThat(dto.opers().get(0).funcs().get(1).id()).isNotNull();
        assertThat(dto.sborEds()).hasSize(2);
        assertThat(dto.sborEds().get(0).id()).isEqualTo(keptSborEd.id());
        assertThat(dto.sborEds().get(0).nazv()).isEqualTo("Вал NEW");
        assertThat(dto.sborEds().get(1).id()).isNotNull();

        entityManager.flush();
        entityManager.clear();
        assertThat(funcRepository.findById(removedFunc.id())).isEmpty();
        assertThat(sborEdRepository.findById(removedSborEd.id())).isEmpty();
        assertThat(funcRepository.findById(dto.opers().get(0).funcs().get(1).id())).isPresent();
    }

    @Test
    @DisplayName("updateFullPackage: при смене порядка пересоздаёт операции и сборочные единицы")
    void updateFullPackage_withOrderChange_recreatesGraph() {
        DockPackageDto saved = service.saveFullPackage(packageDto(marker()));
        OperDto first = saved.opers().get(0);
        OperDto second = saved.opers().get(1);
        List<Long> oldOperIds = saved.opers().stream().map(OperDto::id).toList();
        List<Long> oldSborEdIds = saved.sborEds().stream().map(SborEdDto::id).toList();

        DockPackageDto reordered = new DockPackageDto(
                saved.id(), saved.packageName(), saved.path(), saved.puName(), saved.spuName(),
                saved.kpName(), saved.fmeaName(), saved.vedIName(),
                List.of(second, first), saved.sborEds());

        DocpackageService.UpdateResult result = service.updateFullPackage(reordered);

        assertThat(result.orderChanged()).isTrue();
        assertThat(result.dto().opers()).hasSize(2);
        assertThat(result.dto().opers().get(0).numOper()).isEqualTo("010");
        assertThat(result.dto().opers().get(1).numOper()).isEqualTo("005");
        assertThat(result.dto().opers()).allSatisfy(oper -> assertThat(oldOperIds).doesNotContain(oper.id()));
        assertThat(result.dto().sborEds()).allSatisfy(sborEd -> assertThat(oldSborEdIds).doesNotContain(sborEd.id()));
    }

    @Test
    @DisplayName("updateFullPackage: отсутствующий пакет — DataNotFoundException")
    void updateFullPackage_missingPackage_throwsDataNotFoundException() {
        DockPackageDto missing = new DockPackageDto(
                999_999_999L, "X", "x", "pu", "spu", "kp", "fmea", "vi",
                List.of(), List.of());

        assertThatThrownBy(() -> service.updateFullPackage(missing))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @DisplayName("search: находит тестовые пакеты и не дублирует сборочные единицы")
    void search_findsPackagesAndDeduplicatesSborEds() {
        String marker = marker();
        DockPackageDto first = service.saveFullPackage(packageDto(marker));
        DockPackageDto second = service.saveFullPackage(packageDto(marker + "B"));

        List<DockPackageDto> byName = service.searchByPackageName("test-pkg-" + marker);
        assertThat(byName).extracting(DockPackageDto::id)
                .contains(first.id(), second.id());

        List<DockPackageDto> byOboznach = service.searchByOboznach("test-" + marker);
        assertThat(byOboznach).extracting(DockPackageDto::id)
                .containsExactlyInAnyOrder(first.id(), second.id());

        assertThat(service.findDtoById(first.id()).packageName())
                .isEqualTo(first.packageName());
    }

    @Test
    @DisplayName("TypeOperService: реальная БД — getById загружает функции вместе с операцией")
    void typeOperService_getById_loadsFunctions() {
        TypeOper oper = TypeOper.builder()
                .numOper("T-" + marker())
                .nomInstr("15-132/T")
                .oborud("Станок")
                .ostnasInstr("Оснастка")
                .name("Тестовая операция")
                .shifr("Т99")
                .numZech("15")
                .build();
        TypeOperFunc func = TypeOperFunc.builder()
                .name("Тестовая функция")
                .param("Параметр")
                .isProd(true)
                .specCharakt("S")
                .typeOper(oper)
                .build();
        oper.setTypeOperFuncs(new ArrayList<>(List.of(func)));

        TypeOper saved = typeOperRepository.save(oper);
        entityManager.flush();
        entityManager.clear();

        TypeOper loaded = typeOperService.getById(saved.getId());
        assertThat(loaded.getTypeOperFuncs()).hasSize(1);
        assertThat(loaded.getTypeOperFuncs().get(0).getName()).isEqualTo("Тестовая функция");
    }

    private String marker() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private DockPackageDto packageDto(String marker) {
        FuncDto firstFunc = new FuncDto(null, "Функция 1 " + marker, null, "Параметр 1", true, "S");
        FuncDto secondFunc = new FuncDto(null, "Функция 2 " + marker, null, "Параметр 2", false, null);
        OperDto firstOper = new OperDto(
                null, null, "005", "15-132/005",
                "Станок 1", "Оснастка 1", "Токарная", "Т01", "15",
                List.of(firstFunc, secondFunc));
        OperDto secondOper = new OperDto(
                null, null, "010", "15-132/010",
                "Станок 2", "Оснастка 2", "Фрезерная", "Т02", "15",
                List.of(new FuncDto(null, "Функция 3 " + marker, null, "Параметр 3", true, "")));

        return new DockPackageDto(
                null, "TEST-PKG-" + marker, "test-folder-" + marker,
                "PU-" + marker, "SPU-" + marker, "KP-" + marker, "FMEA-" + marker, "VI-" + marker,
                List.of(firstOper, secondOper),
                List.of(
                        new SborEdDto(null, null, "Вал " + marker, "TEST-" + marker + "-01"),
                        new SborEdDto(null, null, "Втулка " + marker, "TEST-" + marker + "-02")
                ));
    }
}
