package by.vstu.isit.documentprocessor.services.file;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class PackageFileManager {

    public Path packageDir(String outPuPath, String path) {
        String base = outPuPath.substring(0, outPuPath.indexOf("{0}"));
        Path baseDir = Path.of(base).toAbsolutePath().normalize();
        Path candidate;
        try {
            candidate = Path.of(base, path == null ? "" : path).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Некорректный путь пакета: " + path, e);
        }
        if (candidate.equals(baseDir) || !candidate.startsWith(baseDir)) {
            throw new IllegalArgumentException(
                    "Путь пакета должен указывать на папку внутри базового каталога: " + path);
        }
        return candidate;
    }

    public List<Path> findBusyDocuments(String outPuPath, String path) {
        return findBusyDocuments(packageDir(outPuPath, path));
    }

    public List<Path> findBusyDocuments(Path dir) {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        List<Path> busy = new ArrayList<>();
        try (Stream<Path> files = Files.walk(dir)) {
            files.filter(Files::isRegularFile)
                    .filter(this::isDocumentFile)
                    .forEach(file -> {
                        if (isBusy(file)) {
                            busy.add(file);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удалось проверить документы перед сохранением: " + e.getMessage(), e);
        }
        return busy;
    }

    private boolean isDocumentFile(Path file) {
        String name = file.getFileName().toString();
        return name.toLowerCase(Locale.ROOT).endsWith(".docx");
    }

    private boolean isBusy(Path file) {
        try (FileChannel ignored = FileChannel.open(file, StandardOpenOption.WRITE)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public boolean packageDirExists(String outPuPath, String path) {
        return Files.exists(packageDir(outPuPath, path));
    }

    public void archivePackageDir(Path sourceDir, Path archiveDir) throws IOException {        if (Files.exists(archiveDir)) {
            deleteDirRecursively(archiveDir);
        }
        Files.createDirectories(archiveDir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path entry : stream) {
                if (entry.equals(archiveDir)) {
                    continue;
                }
                if (entry.getFileName().toString().startsWith("Предыдущая версия")) {
                    continue;
                }
                copyRecursively(entry, archiveDir.resolve(entry.getFileName()));
            }
        }
    }

    public void deleteDirContents(Path dir, Path exclude) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (entry.equals(exclude)) {
                    continue;
                }
                if (Files.isDirectory(entry)) {
                    deleteDirRecursively(entry);
                } else {
                    Files.delete(entry);
                }
            }
        }
    }

    public void copyRecursively(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            Files.createDirectories(target);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                for (Path entry : stream) {
                    copyRecursively(entry, target.resolve(entry.getFileName()));
                }
            }
        } else {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
        copyHiddenAttr(source, target);
    }

    public void copyHiddenAttr(Path source, Path target) {
        try {
            if ((Boolean) Files.getAttribute(source, "dos:hidden")) {
                Files.setAttribute(target, "dos:hidden", true);
            }
        } catch (Exception ignored) {
        }
    }

    public void deleteDirRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    deleteDirRecursively(entry);
                } else {
                    Files.delete(entry);
                }
            }
        }
        Files.delete(dir);
    }
}
