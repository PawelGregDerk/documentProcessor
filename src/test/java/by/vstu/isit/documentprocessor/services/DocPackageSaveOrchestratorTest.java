package by.vstu.isit.documentprocessor.services;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.docx.edit.AbstractDocEditor;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocPackageSaveOrchestratorTest {

    @Mock
    private DocpackageService service;
    @Mock
    private AbstractWordGenerator generator;
    @Mock
    private AbstractDocEditor editor;
    @Mock
    private PackageFileManager fileManager;

    @TempDir
    Path tempDir;

    private DocPackageSaveOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        String pattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/PU{1}.docx";
        orchestrator = new DocPackageSaveOrchestrator(
                service, List.of(generator), List.of(editor), fileManager, pattern);
    }

    @Test
    @DisplayName("createPackage: сохраняет пакет и запускает генераторы")
    void createPackage_savesAndGenerates() throws Exception {
        DockPackageDto dto = dto("pkg", oper(1L, "005"));
        DockPackageDto saved = dto("pkg", oper(1L, "005"));
        when(service.saveFullPackage(dto)).thenReturn(saved);

        DockPackageDto result = orchestrator.createPackage(dto);

        assertThat(result).isSameAs(saved);
        verify(generator).generate(saved);
    }

    @Test
    @DisplayName("createPackage: существующая папка не даёт сохранить пакет")
    void createPackage_folderExists_throws() throws Exception {
        Files.createDirectories(tempDir.resolve("pkg"));
        DockPackageDto dto = dto("pkg", oper(1L, "005"));
        when(fileManager.packageDirExists(anyString(), eq("pkg"))).thenReturn(true);
        when(fileManager.packageDir(anyString(), eq("pkg"))).thenReturn(tempDir.resolve("pkg"));

        assertThatThrownBy(() -> orchestrator.createPackage(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Папка с оригиналами уже существует");

        verify(service, never()).saveFullPackage(dto);
    }

    @Test
    @DisplayName("previewUpdate: определяет существование папки и смену порядка")
    void previewUpdate_detectsFolderAndOrderChange() throws Exception {
        Files.createDirectories(tempDir.resolve("pkg"));
        DockPackageDto original = dto("pkg", oper(1L, "005"), oper(2L, "010"));
        DockPackageDto changed = dto("pkg", oper(2L, "010"), oper(1L, "005"));
        when(service.findDtoById(1L)).thenReturn(original);
        when(fileManager.packageDirExists(anyString(), eq("pkg"))).thenReturn(true);

        DocPackageSaveOrchestrator.UpdatePreview preview =
                orchestrator.previewUpdate(changed, 1L, "pkg");

        assertThat(preview.folderExists()).isTrue();
        assertThat(preview.orderChanged()).isTrue();
        assertThat(preview.original()).isSameAs(original);
    }

    @Test
    @DisplayName("previewUpdate: занятые документы останавливают сохранение")
    void previewUpdate_busyDocuments_throws() {
        DockPackageDto original = dto("pkg", oper(1L, "005"));
        when(service.findDtoById(1L)).thenReturn(original);
        when(fileManager.packageDirExists(anyString(), eq("pkg"))).thenReturn(true);
        when(fileManager.findBusyDocuments(anyString(), eq("pkg")))
                .thenReturn(List.of(tempDir.resolve("pkg/PU.docx")));

        assertThatThrownBy(() -> orchestrator.previewUpdate(original, 1L, "pkg"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Закройте документы")
                .hasMessageContaining("PU.docx");
    }

    @Test
    @DisplayName("updatePackage: при смене порядка перегенерирует документы")
    void updatePackage_orderChanged_generates() throws Exception {
        DockPackageDto original = dto("pkg", oper(1L, "005"));
        DockPackageDto saved = dto("pkg", oper(1L, "005"));
        DockPackageDto dto = dto("pkg", oper(1L, "005"));
        when(service.updateFullPackage(dto))
                .thenReturn(new DocpackageService.UpdateResult(saved, true));
        when(fileManager.packageDir(anyString(), eq("pkg"))).thenReturn(tempDir.resolve("pkg"));

        orchestrator.updatePackage(dto, new DocPackageSaveOrchestrator.UpdatePreview(original, true, true));

        verify(fileManager).archivePackageDir(
                tempDir.resolve("pkg"), tempDir.resolve("pkg/Предыдущая версия " + original.packageName()));
        verify(fileManager).deleteDirContents(
                tempDir.resolve("pkg"), tempDir.resolve("pkg/Предыдущая версия " + original.packageName()));
        verify(generator).generate(saved);
        verify(editor, never()).edit(original, saved);
    }

    @Test
    @DisplayName("updatePackage: без смены порядка редактирует документы с архивом")
    void updatePackage_orderNotChanged_editsDocuments() throws Exception {
        DockPackageDto original = dto("pkg", oper(1L, "005"));
        DockPackageDto saved = dto("pkg", oper(1L, "005"));
        DockPackageDto dto = dto("pkg", oper(1L, "005"));
        when(service.updateFullPackage(dto))
                .thenReturn(new DocpackageService.UpdateResult(saved, false));
        when(fileManager.packageDir(anyString(), eq("pkg"))).thenReturn(tempDir.resolve("pkg"));

        orchestrator.updatePackage(dto, new DocPackageSaveOrchestrator.UpdatePreview(original, true, false));

        String archive = "pkg/Предыдущая версия " + original.packageName();
        verify(editor).setSourceFolderOverride(archive);
        verify(editor).edit(original, saved);
        verify(generator, never()).generate(saved);
    }

    @Test
    @DisplayName("updatePackage: если папки нет, создаёт документы с нуля")
    void updatePackage_folderMissing_generates() throws Exception {
        DockPackageDto original = dto("pkg", oper(1L, "005"));
        DockPackageDto saved = dto("pkg", oper(1L, "005"));
        DockPackageDto dto = dto("pkg", oper(1L, "005"));
        when(service.updateFullPackage(dto))
                .thenReturn(new DocpackageService.UpdateResult(saved, false));

        orchestrator.updatePackage(dto, new DocPackageSaveOrchestrator.UpdatePreview(original, false, false));

        verify(generator).generate(saved);
        verify(editor, never()).edit(original, saved);
        verify(fileManager, never()).archivePackageDir(
                tempDir.resolve("pkg"), tempDir.resolve("pkg/Предыдущая версия " + original.packageName()));
    }

    private DockPackageDto dto(String path, OperDto... opers) {
        return new DockPackageDto(
                1L, "P", path, "pu", "spu", "kp", "fmea", "vi",
                List.of(opers),
                List.of(new SborEdDto(1L, 1L, "Вал", "АБВГ.123")));
    }

    private OperDto oper(long id, String num) {
        return new OperDto(
                id, 1L, num, "15-132/" + num, "Станок", "Оснастка", "Операция", "Ш", "15",
                List.of(new FuncDto(1L, "Функция", id, "Параметр", true, "")));
    }
}
