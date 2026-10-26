package by.vstu.isit.documentprocessor.services.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PackageFileManagerTest {

    private final PackageFileManager manager = new PackageFileManager();

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("packageDirExists: проверяет наличие каталога по шаблону")
    void packageDirExists_checksFolder() throws Exception {
        String pattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/PU{1}.docx";
        Files.createDirectories(tempDir.resolve("pkg"));

        assertThat(manager.packageDirExists(pattern, "pkg")).isTrue();
        assertThat(manager.packageDirExists(pattern, "missing")).isFalse();
        assertThat(manager.packageDir(pattern, "pkg")).isEqualTo(tempDir.resolve("pkg"));
    }

    @Test
    @DisplayName("archivePackageDir: копирует содержимое и пропускает предыдущие версии")
    void archivePackageDir_copiesContentsAndSkipsPreviousVersions() throws Exception {
        Path source = Files.createDirectories(tempDir.resolve("source"));
        Files.writeString(source.resolve("doc.txt"), "doc");
        Path sub = Files.createDirectories(source.resolve("sub"));
        Files.writeString(sub.resolve("inner.txt"), "inner");
        Path oldVersion = Files.createDirectories(source.resolve("Предыдущая версия old"));
        Files.writeString(oldVersion.resolve("old.txt"), "old");

        Path archive = Files.createDirectories(source.resolve("Предыдущая версия new"));
        Files.writeString(archive.resolve("stale.txt"), "stale");

        manager.archivePackageDir(source, archive);

        assertThat(archive.resolve("doc.txt")).exists();
        assertThat(archive.resolve("sub/inner.txt")).exists();
        assertThat(archive.resolve("stale.txt")).doesNotExist();
        assertThat(archive.resolve("Предыдущая версия old")).doesNotExist();
    }

    @Test
    @DisplayName("deleteDirContents: удаляет всё, кроме исключённого каталога")
    void deleteDirContents_keepsExcludedDirectory() throws Exception {
        Path dir = Files.createDirectories(tempDir.resolve("dir"));
        Path keep = Files.createDirectories(dir.resolve("keep"));
        Files.writeString(keep.resolve("keep.txt"), "keep");
        Files.writeString(dir.resolve("remove.txt"), "remove");

        manager.deleteDirContents(dir, keep);

        assertThat(keep).exists();
        assertThat(keep.resolve("keep.txt")).exists();
        assertThat(dir.resolve("remove.txt")).doesNotExist();
    }

    @Test
    @DisplayName("copyRecursively: копирует вложенные файлы")
    void copyRecursively_copiesNestedFiles() throws Exception {
        Path source = Files.createDirectories(tempDir.resolve("source"));
        Path nested = Files.createDirectories(source.resolve("nested"));
        Files.writeString(nested.resolve("file.txt"), "data");
        Path target = tempDir.resolve("target");

        manager.copyRecursively(source, target);

        assertThat(target.resolve("nested/file.txt")).exists();
    }

    @Test
    @DisplayName("deleteDirRecursively: удаляет каталог со вложенными файлами")
    void deleteDirRecursively_removesTree() throws Exception {
        Path dir = Files.createDirectories(tempDir.resolve("dir"));
        Path nested = Files.createDirectories(dir.resolve("nested"));
        Files.writeString(nested.resolve("file.txt"), "data");

        manager.deleteDirRecursively(dir);

        assertThat(dir).doesNotExist();
    }
}
