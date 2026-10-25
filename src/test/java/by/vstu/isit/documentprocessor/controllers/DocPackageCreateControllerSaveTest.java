package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.ui.AlertService;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxWeaver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocPackageCreateControllerSaveTest {

    @Mock
    private DockPackageFormMapper formMapper;
    @Mock
    private DocpackageService service;
    @Mock
    private TypeOperService typeOperService;
    @Mock
    private FxWeaver fxWeaver;
    @Mock
    private AlertService alertService;
    @Mock
    private AbstractWordGenerator wordGenerator;

    @TempDir
    private Path tempDir;

    private DocPackageCreateController controller;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Already initialized
        }
    }

    @BeforeEach
    void setUp() {
        controller = new DocPackageCreateController(
                formMapper, service, typeOperService, List.of(wordGenerator), fxWeaver, alertService
        );

        String outPuPathTemplate = tempDir.toString().replace('\\', '/') + "/{0}/pu.docx";
        ReflectionTestUtils.setField(controller, "outPuPath", outPuPathTemplate);
        ReflectionTestUtils.setField(controller, "packageNameField", new TextField("Пакет 1"));
        ReflectionTestUtils.setField(controller, "packagePathField", new TextField("new-folder"));
        ReflectionTestUtils.setField(controller, "sborEdNazv", new TextField("Деталь"));
        ReflectionTestUtils.setField(controller, "puField", new TextField("ПУ"));
        ReflectionTestUtils.setField(controller, "spuField", new TextField("СПУ"));
        ReflectionTestUtils.setField(controller, "kpField", new TextField("КП"));
        ReflectionTestUtils.setField(controller, "fmeaField", new TextField("ФМЕА"));
        ReflectionTestUtils.setField(controller, "vedInstrField", new TextField("ВИ"));
        ReflectionTestUtils.setField(controller, "operationsContainer", new VBox());
        ReflectionTestUtils.setField(controller, "assemblyUnitsContainer", new VBox());
    }

    private SborEdDto validSborEd() {
        return new SborEdDto(1L, 1L, "Деталь", "ОБОЗН-01");
    }

    private FuncDto validFunc() {
        return new FuncDto(1L, "Точить", 1L, "1", true, "");
    }

    private OperDto validOper(Long id, List<FuncDto> funcs) {
        return new OperDto(id, 1L, "010", "Инстр", "Оборудование", "Оснастка", "Токарная", "ШИФР", "10", funcs);
    }

    private DockPackageDto validDto(String path, List<SborEdDto> sborEds, List<OperDto> opers) {
        return new DockPackageDto(
                null, "Пакет 1", path, "ПУ", "СПУ", "КП", "ФМЕА", "ВИ",
                opers, sborEds
        );
    }

    @Test
    @DisplayName("Сценарий 1: Happy path создания пакета (диск + БД) - вызывает saveFullPackage, генераторы и alertInfo")
    void onSave_happyPath_savesPackageGeneratesDocsAndShowsInfo() throws Exception {
        DockPackageDto formDto = validDto("new-folder", List.of(validSborEd()), List.of(validOper(1L, List.of(validFunc()))));
        DockPackageDto savedDto = validDto("new-folder", List.of(validSborEd()), List.of(validOper(1L, List.of(validFunc()))));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);
        when(service.saveFullPackage(formDto)).thenReturn(savedDto);

        ReflectionTestUtils.invokeMethod(controller, "onSave");

        verify(service).saveFullPackage(formDto);
        verify(wordGenerator).generate(savedDto);
        verify(alertService).showInfo("Документ сформирован");
        verify(alertService, never()).showError(any());
    }

    @Test
    @DisplayName("Сценарий 2: Happy path сохранения только в БД - вызывает saveFullPackage, не вызывает генераторы")
    void onSaveToDbOnly_happyPath_savesOnlyToDb() {
        DockPackageDto formDto = validDto("new-folder", List.of(validSborEd()), List.of(validOper(1L, List.of(validFunc()))));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);

        ReflectionTestUtils.invokeMethod(controller, "onSaveToDbOnly");

        verify(service).saveFullPackage(formDto);
        verifyNoInteractions(wordGenerator);
        verify(alertService).showInfo("Пакет сохранён в базу данных");
        verify(alertService, never()).showError(any());
    }

    @Test
    @DisplayName("Сценарий 7: Валидация - пустые сборочные единицы вызывают Alert.ERROR")
    void onSave_emptySborEds_showsErrorAlert() {
        DockPackageDto formDto = validDto("new-folder", List.of(), List.of(validOper(1L, List.of(validFunc()))));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);

        ReflectionTestUtils.invokeMethod(controller, "onSave");

        verify(alertService).showError("Необходимо указать хотя бы одну сборочную единицу");
        verifyNoInteractions(service);
        verifyNoInteractions(wordGenerator);
    }

    @Test
    @DisplayName("Сценарий 8: Валидация - пустой список операций вызывает Alert.ERROR")
    void onSave_emptyOperations_showsErrorAlert() {
        DockPackageDto formDto = validDto("new-folder", List.of(validSborEd()), List.of());

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);

        ReflectionTestUtils.invokeMethod(controller, "onSave");

        verify(alertService).showError("Необходимо указать хотя бы одну операцию");
        verifyNoInteractions(service);
        verifyNoInteractions(wordGenerator);
    }

    @Test
    @DisplayName("Сценарий 9: Валидация - операция без функций вызывает Alert.ERROR")
    void onSave_operationWithoutFunctions_showsErrorAlert() {
        DockPackageDto formDto = validDto("new-folder", List.of(validSborEd()), List.of(validOper(1L, List.of())));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);

        ReflectionTestUtils.invokeMethod(controller, "onSave");

        verify(alertService).showError("Каждая операция должна содержать хотя бы одну функцию");
        verifyNoInteractions(service);
        verifyNoInteractions(wordGenerator);
    }

    @Test
    @DisplayName("Сценарий 10: Ошибка создания - если папка уже существует, генерируется ошибка и сервис не вызывается")
    void onSave_folderAlreadyExists_showsErrorAlert() throws Exception {
        Files.createDirectories(tempDir.resolve("already-existing"));
        DockPackageDto formDto = validDto("already-existing", List.of(validSborEd()), List.of(validOper(1L, List.of(validFunc()))));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);

        ReflectionTestUtils.invokeMethod(controller, "onSave");

        verify(alertService).showError(contains("Папка с оригиналами уже существует"));
        verifyNoInteractions(service);
        verifyNoInteractions(wordGenerator);
    }

    @Test
    @DisplayName("Сценарий 11: Ошибка БД/сервиса - исключение перехватывается и показывается Alert.ERROR")
    void onSave_databaseError_showsErrorAlert() {
        DockPackageDto formDto = validDto("new-folder", List.of(validSborEd()), List.of(validOper(1L, List.of(validFunc()))));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);
        when(service.saveFullPackage(formDto)).thenThrow(new RuntimeException("DB Connection Failed"));

        ReflectionTestUtils.invokeMethod(controller, "onSave");

        verify(alertService).showError("DB Connection Failed");
        verifyNoInteractions(wordGenerator);
    }
}
