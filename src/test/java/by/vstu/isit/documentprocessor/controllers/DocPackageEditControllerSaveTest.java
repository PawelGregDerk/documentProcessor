package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;
import by.vstu.isit.documentprocessor.services.docx.edit.AbstractDocEditor;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.ui.AlertService;
import javafx.application.Platform;
import javafx.scene.control.Button;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocPackageEditControllerSaveTest {

    @Mock
    private DockPackageFormMapper formMapper;
    @Mock
    private DockPackageGuiMapper guiMapper;
    @Mock
    private DocpackageService service;
    @Mock
    private TypeOperService typeOperService;
    @Mock
    private FxWeaver fxWeaver;
    @Mock
    private AlertService alertService;
    @Mock
    private AbstractDocEditor docEditor;
    @Mock
    private AbstractWordGenerator wordGenerator;

    @TempDir
    private Path tempDir;

    private DocPackageEditController controller;

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
        controller = new DocPackageEditController(
                formMapper, guiMapper, List.of(docEditor), List.of(wordGenerator),
                service, typeOperService, fxWeaver, alertService, new PackageFileManager()
        );

        String outPuPathTemplate = tempDir.toString().replace('\\', '/') + "/{0}/pu.docx";
        ReflectionTestUtils.setField(controller, "outPuPath", outPuPathTemplate);
        ReflectionTestUtils.setField(controller, "packageId", 1L);
        ReflectionTestUtils.setField(controller, "path", "existing-folder");
        ReflectionTestUtils.setField(controller, "packageNameField", new TextField("Пакет Редакт"));
        ReflectionTestUtils.setField(controller, "sborEdNazv", new TextField("Деталь"));
        ReflectionTestUtils.setField(controller, "puField", new TextField("ПУ"));
        ReflectionTestUtils.setField(controller, "spuField", new TextField("СПУ"));
        ReflectionTestUtils.setField(controller, "kpField", new TextField("КП"));
        ReflectionTestUtils.setField(controller, "fmeaField", new TextField("ФМЕА"));
        ReflectionTestUtils.setField(controller, "vedInstrField", new TextField("ВИ"));
        ReflectionTestUtils.setField(controller, "operationsContainer", new VBox());
        ReflectionTestUtils.setField(controller, "assemblyUnitsContainer", new VBox());
        ReflectionTestUtils.setField(controller, "saveBtn", new Button());
        ReflectionTestUtils.setField(controller, "updateDbOnlyBtn", new Button());
    }

    private SborEdDto validSborEd() {
        return new SborEdDto(1L, 1L, "Деталь", "ОБОЗН-01");
    }

    private FuncDto validFunc(Long id, Long idOper) {
        return new FuncDto(id, "Точить", idOper, "1", true, "");
    }

    private OperDto validOper(Long id, String numOper, List<FuncDto> funcs) {
        return new OperDto(id, 1L, numOper, "Инстр", "Оборудование", "Оснастка", "Токарная", "ШИФР", "10", funcs);
    }

    private DockPackageDto validDto(String path, List<OperDto> opers) {
        return new DockPackageDto(
                1L, "Пакет Редакт", path, "ПУ", "СПУ", "КП", "ФМЕА", "ВИ",
                opers, List.of(validSborEd())
        );
    }

    @Test
    @DisplayName("Сценарий 3: Happy path обновления на диске БЕЗ смены порядка - архивирует, вызывает editors, не трогает generators")
    void onSaveOrCreate_orderNotChanged_updatesViaEditors() throws Exception {
        Path packageDir = Files.createDirectories(tempDir.resolve("existing-folder"));
        Files.createFile(packageDir.resolve("old_pu.docx"));

        OperDto op1 = validOper(1L, "010", List.of(validFunc(1L, 1L)));
        OperDto op2 = validOper(2L, "020", List.of(validFunc(2L, 2L)));

        DockPackageDto originalDto = validDto("existing-folder", List.of(op1, op2));
        DockPackageDto formDto = validDto("existing-folder", List.of(op1, op2));
        DockPackageDto savedDto = validDto("existing-folder", List.of(op1, op2));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);
        when(service.findDtoById(1L)).thenReturn(originalDto);
        when(service.updateFullPackage(formDto))
                .thenReturn(new DocpackageService.UpdateResult(savedDto, false));

        ReflectionTestUtils.invokeMethod(controller, "onSaveOrCreate");

        verify(service).updateFullPackage(formDto);
        verify(docEditor).edit(originalDto, savedDto);
        verifyNoInteractions(wordGenerator);
        verify(alertService).showInfo(contains("Документы сохранены в папку existing-folder"));
        verify(guiMapper).toGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Небезопасный путь: предупреждение и создание вместо обновления")
    void setPath_unsafe_showsErrorAndEnablesCreate() {
        controller.setPath("..");

        verify(alertService).showError(contains("внутри базового каталога"));
        assertThat(controller.getSaveBtn().getText()).isEqualTo("Создать пакет на диске");
        assertThat(controller.getUpdateDbOnlyBtn().isDisable()).isFalse();
    }

    @Test
    @DisplayName("Открытый документ: предупреждение и база не меняется")
    void onSaveOrCreate_busyDocument_showsErrorAndDoesNotUpdate() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                System.getProperty("os.name").toLowerCase().contains("win"));
        Path packageDir = Files.createDirectories(tempDir.resolve("existing-folder"));
        Path locked = packageDir.resolve("old_pu.docx");
        Files.writeString(locked, "doc");
        Files.setAttribute(locked, "dos:readonly", true);
        try {
            OperDto oper = validOper(1L, "010", List.of(validFunc(1L, 1L)));
            DockPackageDto dto = validDto("existing-folder", List.of(oper));
            when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(dto);
            when(service.findDtoById(1L)).thenReturn(dto);

            ReflectionTestUtils.invokeMethod(controller, "onSaveOrCreate");

            verify(service, never()).updateFullPackage(any());
            verify(alertService).showError(contains("Закройте документы"));
        } finally {
            Files.setAttribute(locked, "dos:readonly", false);
        }
    }

    @Test
    @DisplayName("Сценарий 4: Happy path обновления на диске СО сменой порядка - подтверждение пользователем, вызывает generators")
    void onSaveOrCreate_orderChangedConfirmed_generatesDocs() throws Exception {
        Path packageDir = Files.createDirectories(tempDir.resolve("existing-folder"));
        Files.createFile(packageDir.resolve("old_pu.docx"));

        OperDto op1 = validOper(1L, "010", List.of(validFunc(1L, 1L)));
        OperDto op2 = validOper(2L, "020", List.of(validFunc(2L, 2L)));

        DockPackageDto originalDto = validDto("existing-folder", List.of(op1, op2));
        // Порядок изменён: op2, затем op1
        DockPackageDto formDto = validDto("existing-folder", List.of(op2, op1));
        DockPackageDto savedDto = validDto("existing-folder", List.of(op2, op1));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);
        when(service.findDtoById(1L)).thenReturn(originalDto);
        when(alertService.confirm(any(), any(), any())).thenReturn(true);
        when(service.updateFullPackage(formDto))
                .thenReturn(new DocpackageService.UpdateResult(savedDto, true));

        ReflectionTestUtils.invokeMethod(controller, "onSaveOrCreate");

        verify(alertService).confirm(
                eq("Изменение порядка операций"),
                eq("Последовательность операций изменена"),
                contains("Рукописные изменения")
        );
        verify(service).updateFullPackage(formDto);
        verify(wordGenerator).generate(savedDto);
        verifyNoInteractions(docEditor);
        verify(alertService).showInfo(contains("Документы сохранены в папку existing-folder"));
    }

    @Test
    @DisplayName("Сценарий 5: Happy path обновления пакета при отсутствии папки - полная генерация с нуля")
    void onSaveOrCreate_folderDoesNotExist_createsFromScratch() throws Exception {
        // Папка existing-folder не создаётся на диске
        OperDto op1 = validOper(1L, "010", List.of(validFunc(1L, 1L)));

        DockPackageDto originalDto = validDto("existing-folder", List.of(op1));
        DockPackageDto formDto = validDto("existing-folder", List.of(op1));
        DockPackageDto savedDto = validDto("existing-folder", List.of(op1));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);
        when(service.findDtoById(1L)).thenReturn(originalDto);
        when(service.updateFullPackage(formDto))
                .thenReturn(new DocpackageService.UpdateResult(savedDto, false));

        ReflectionTestUtils.invokeMethod(controller, "onSaveOrCreate");

        verify(service).updateFullPackage(formDto);
        verify(wordGenerator).generate(savedDto);
        verifyNoInteractions(docEditor);
        verify(alertService).showInfo("Документ сформирован");
    }

    @Test
    @DisplayName("Сценарий 6: Happy path обновления только в БД - обновляет БД, не вызывает editors и generators")
    void onUpdateDbOnly_happyPath_updatesDatabaseOnly() {
        OperDto op1 = validOper(1L, "010", List.of(validFunc(1L, 1L)));
        DockPackageDto formDto = validDto("existing-folder", List.of(op1));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);
        when(service.findDtoById(1L)).thenReturn(formDto);

        ReflectionTestUtils.invokeMethod(controller, "onUpdateDbOnly");

        verify(service).updateFullPackage(formDto);
        verifyNoInteractions(docEditor);
        verifyNoInteractions(wordGenerator);
        verify(alertService).showInfo("Данные обновлены в базе данных");
    }

    @Test
    @DisplayName("Сценарий 12: Ошибки редактора Word - сбой внутри Try.run вызывает Alert.ERROR")
    void onSaveOrCreate_editorThrowsException_showsErrorAlert() throws Exception {
        Path packageDir = Files.createDirectories(tempDir.resolve("existing-folder"));
        Files.createFile(packageDir.resolve("old_pu.docx"));

        OperDto op1 = validOper(1L, "010", List.of(validFunc(1L, 1L)));
        DockPackageDto originalDto = validDto("existing-folder", List.of(op1));
        DockPackageDto formDto = validDto("existing-folder", List.of(op1));
        DockPackageDto savedDto = validDto("existing-folder", List.of(op1));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);
        when(service.findDtoById(1L)).thenReturn(originalDto);
        when(service.updateFullPackage(formDto))
                .thenReturn(new DocpackageService.UpdateResult(savedDto, false));

        doThrow(new RuntimeException("Spire.Doc corrupt format exception"))
                .when(docEditor).edit(any(), any());

        ReflectionTestUtils.invokeMethod(controller, "onSaveOrCreate");

        verify(alertService).showError(contains("Spire.Doc corrupt format exception"));
    }

    @Test
    @DisplayName("Сценарий 13: Файловый I/O: сбой архивирования - перехватывается в catch и вызывает Alert.ERROR")
    void onSaveOrCreate_archiveIoError_showsErrorAlert() throws Exception {
        // Создаем файл вместо каталога, чтобы при вызове Files.createDirectories / DirectoryStream возникло IOException
        Files.createFile(tempDir.resolve("existing-folder"));

        OperDto op1 = validOper(1L, "010", List.of(validFunc(1L, 1L)));
        DockPackageDto originalDto = validDto("existing-folder", List.of(op1));
        DockPackageDto formDto = validDto("existing-folder", List.of(op1));
        DockPackageDto savedDto = validDto("existing-folder", List.of(op1));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);
        when(service.findDtoById(1L)).thenReturn(originalDto);
        when(service.updateFullPackage(formDto))
                .thenReturn(new DocpackageService.UpdateResult(savedDto, false));

        ReflectionTestUtils.invokeMethod(controller, "onSaveOrCreate");

        verify(alertService).showError(contains("Ошибка сохранения:"));
        verifyNoInteractions(docEditor);
    }

    @Test
    @DisplayName("Сценарий 14: Отмена при смене порядка операций - пользователь нажал Отмена, сервис не вызывается")
    void onSaveOrCreate_orderChangedCancelled_abortsWithoutCallingService() throws Exception {
        Files.createDirectories(tempDir.resolve("existing-folder"));

        OperDto op1 = validOper(1L, "010", List.of(validFunc(1L, 1L)));
        OperDto op2 = validOper(2L, "020", List.of(validFunc(2L, 2L)));

        DockPackageDto originalDto = validDto("existing-folder", List.of(op1, op2));
        DockPackageDto formDto = validDto("existing-folder", List.of(op2, op1));

        when(formMapper.fromGui(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(formDto);
        when(service.findDtoById(1L)).thenReturn(originalDto);
        when(alertService.confirm(any(), any(), any())).thenReturn(false);

        ReflectionTestUtils.invokeMethod(controller, "onSaveOrCreate");

        verify(alertService).confirm(any(), any(), any());
        verify(service, never()).updateFullPackage(any());
        verifyNoInteractions(docEditor);
        verifyNoInteractions(wordGenerator);
    }
}
