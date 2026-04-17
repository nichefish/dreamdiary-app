package io.nicheblog.dreamdiary;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guard test for legacy naming residue.
 */
class JournalNamingMigrationTest {

    private static final Set<String> SCAN_EXTENSIONS = Set.of(
            ".java", ".xml", ".ts", ".js", ".scss", ".sql", ".hbs", ".ftlh", ".yml", ".yaml", ".md"
    );

    private static final List<String> SCAN_ROOTS = List.of(
            "src/main/java",
            "src/main/resources",
            "src/test/java",
            "static",
            "templates"
    );

    @Test
    void shouldNotContainLegacyJrnlToken() throws IOException {
        final List<Path> hitFiles = SCAN_ROOTS.stream()
                .map(Path::of)
                .filter(Files::exists)
                .flatMap(this::walkRegularFiles)
                .filter(this::isTargetExtension)
                .filter(this::containsLegacyToken)
                .limit(20)
                .collect(Collectors.toList());

        assertTrue(hitFiles.isEmpty(),
                "Legacy token 'jrnl' was found in files: " + hitFiles);
    }

    private Stream<Path> walkRegularFiles(final Path root) {
        try {
            return Files.walk(root)
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains("metronic\\assets\\plugins"))
                    .filter(path -> !path.toString().endsWith("JournalNamingMigrationTest.java"));
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    private boolean isTargetExtension(final Path file) {
        final String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return SCAN_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private boolean containsLegacyToken(final Path file) {
        try {
            final String content = Files.readString(file, StandardCharsets.UTF_8)
                    .toLowerCase(Locale.ROOT);
            return content.contains("jrnl");
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
