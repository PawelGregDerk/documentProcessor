package by.vstu.isit.documentprocessor.services;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.docx.edit.AbstractDocEditor;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.services.file.PackageFileManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DocPackageSaveOrchestrator {

    private final DocpackageService service;
    private final List<AbstractWordGenerator> generators;
    private final List<AbstractDocEditor> editors;
    private final PackageFileManager fileManager;
    private final String outPuPath;

    public DocPackageSaveOrchestrator(DocpackageService service,
                                      List<AbstractWordGenerator> generators,
                                      List<AbstractDocEditor> editors,
                                      PackageFileManager fileManager,
                                      String outPuPath) {
        this.service = service;
        this.generators = generators;
        this.editors = editors;
        this.fileManager = fileManager;
        this.outPuPath = outPuPath;
    }

    public DockPackageDto createPackage(DockPackageDto dto) throws Exception {
        String base = basePath();
        if (new File(base + dto.path()).exists()) {
            throw new IllegalStateException("Папка с оригиналами уже существует: " + base + dto.path());
        }

        DockPackageDto saved = service.saveFullPackage(dto);
        generate(saved);
        return saved;
    }

    public UpdatePreview previewUpdate(DockPackageDto dto, Long packageId, String path) {
        DockPackageDto original = service.findDtoById(packageId);
        boolean folderExists = Files.exists(packageDir(path));
        boolean orderChanged = folderExists && checkOrderChanged(dto, original);
        return new UpdatePreview(original, folderExists, orderChanged);
    }

    public UpdateOutcome updatePackage(DockPackageDto dto, UpdatePreview preview) throws Exception {
        var result = service.updateFullPackage(dto);
        var saved = result.dto();

        if (preview.folderExists()) {
            Path packageDir = packageDir(dto.path());
            String archiveRelPath = dto.path() + "/Предыдущая версия " + preview.original().packageName();
            Path archiveDir = packageDir.resolve("Предыдущая версия " + preview.original().packageName());

            fileManager.archivePackageDir(packageDir, archiveDir);
            fileManager.deleteDirContents(packageDir, archiveDir);

            if (result.orderChanged()) {
                generate(saved);
            } else {
                edit(preview.original(), saved, archiveRelPath);
            }
        } else {
            generate(saved);
        }

        return new UpdateOutcome(saved, preview.folderExists(), result.orderChanged());
    }

    private void generate(DockPackageDto dto) throws Exception {
        for (AbstractWordGenerator generator : generators) {
            generator.generate(dto);
        }
    }

    private void edit(DockPackageDto original, DockPackageDto saved, String archiveRelPath) throws Exception {
        for (AbstractDocEditor editor : editors) {
            editor.setSourceFolderOverride(archiveRelPath);
            editor.edit(original, saved);
        }
    }

    private boolean checkOrderChanged(DockPackageDto dto, DockPackageDto original) {
        var oldIds = original.opers().stream().map(OperDto::id).toList();
        for (int i = 0; i < dto.opers().size(); i++) {
            Long id = dto.opers().get(i).id();
            if (id != null) {
                int oldIdx = oldIds.indexOf(id);
                if (oldIdx != i) {
                    return true;
                }
            }
        }
        return false;
    }

    private Path packageDir(String path) {
        return Path.of(basePath(), path);
    }

    private String basePath() {
        return outPuPath.substring(0, outPuPath.indexOf("{0}"));
    }

    public record UpdatePreview(DockPackageDto original, boolean folderExists, boolean orderChanged) {
    }

    public record UpdateOutcome(DockPackageDto saved, boolean folderExists, boolean orderChanged) {
    }
}
