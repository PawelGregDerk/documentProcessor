package by.vstu.isit.documentprocessor.services.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("packageDir: запрещает пустой, абсолютный и выходящий за базу путь")
    void packageDir_rejectsDangerousPaths() {
        String pattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/PU{1}.docx";

        for (String path : List.of("", ".", "..", "../other", "C:/temp")) {
            assertThatThrownBy(() -> manager.packageDir(pattern, path))
                    .as("путь: %s", path)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("packageDir: разрешает точку в имени и вложенную папку")
    void packageDir_allowsDotInNameAndNestedFolder() {
        String pattern = tempDir.toAbsolutePath().toString().replace('\\', '/') + "/{0}/PU{1}.docx";

        assertThat(manager.packageDir(pattern, "0360.1"))
                .isEqualTo(tempDir.resolve("0360.1"));
        assertThat(manager.packageDir(pattern, "2026/0360"))
                .isEqualTo(tempDir.resolve("2026/0360"));
    }

    @Test
    @DisplayName("findBusyDocuments: обычные документы не считаются занятыми")
    void findBusyDocuments_normalDocumentsEmpty() throws Exception {
        Path dir = Files.createDirectories(tempDir.resolve("pkg"));
        Files.writeString(dir.resolve("PU0360.docx"), "doc");
        Files.createDirectories(dir.resolve("ri"));
        Files.writeString(dir.resolve("ri/005 Токарная.docx"), "doc");

        assertThat(manager.findBusyDocuments(dir)).isEmpty();
    }

    @Test
    @DisplayName("findBusyDocuments: документ без права записи считается занятым")
    void findBusyDocuments_flagsReadOnlyDocument() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                System.getProperty("os.name").toLowerCase().contains("win"));
        Path dir = Files.createDirectories(tempDir.resolve("pkg"));
        Path document = dir.resolve("PU0360.docx");
        Files.writeString(document, "doc");
        Files.setAttribute(document, "dos:readonly", true);
        try {
            assertThat(manager.findBusyDocuments(dir))
                    .extracting(path -> path.getFileName().toString())
                    .containsExactly("PU0360.docx");
        } finally {
            Files.setAttribute(document, "dos:readonly", false);
        }
    }

    @Test
    @DisplayName("findBusyDocuments: проверяет документы во вложенном архиве")
    void findBusyDocuments_checksNestedArchive() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                System.getProperty("os.name").toLowerCase().contains("win"));
        Path archive = Files.createDirectories(tempDir.resolve("pkg/Предыдущая версия old"));
        Path document = archive.resolve("PU0360.docx");
        Files.writeString(document, "doc");
        Files.setAttribute(document, "dos:readonly", true);
        try {
            assertThat(manager.findBusyDocuments(tempDir.resolve("pkg")))
                    .extracting(path -> path.getFileName().toString())
                    .containsExactly("PU0360.docx");
        } finally {
            Files.setAttribute(document, "dos:readonly", false);
        }
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
