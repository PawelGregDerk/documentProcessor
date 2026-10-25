package by.vstu.isit.documentprocessor.services.file;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class PackageFileManager {

    public void archivePackageDir(Path sourceDir, Path archiveDir) throws IOException {
        if (Files.exists(archiveDir)) {
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
