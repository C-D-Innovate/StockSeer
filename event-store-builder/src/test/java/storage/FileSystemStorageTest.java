package storage;

import es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.storage.FileSystemStorage;
import es.ulpgc.dacd.eventstorebuilder.model.Event;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileSystemStorageTest {

    private static final String BASE_DIR = "eventstore";
    private FileSystemStorage storage;

    @BeforeAll
    void cleanBeforeAll() throws IOException {
        // Limpia el directorio antes de ejecutar los tests
        Path basePath = Paths.get(BASE_DIR);
        if (Files.exists(basePath)) {
            deleteRecursively(basePath);
        }
    }

    @BeforeEach
    void setUp() {
        storage = new FileSystemStorage();
    }

    @Test
    void testSave_shouldCreateFileAndWriteContent() throws IOException {
        // Arrange
        Instant ts = Instant.parse("2025-06-25T10:15:30.00Z");
        String ss = "source1";
        String topic = "topicA";
        String json = "{\"test\":\"value\"}";

        Event event = new Event(ts, ss, topic, json);

        // Act
        storage.save(event);

        // Assert
        String date = ts.atZone(ZoneId.of("UTC")).toLocalDate().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        Path expectedPath = Paths.get(BASE_DIR, topic, ss, date + ".events");
        assertTrue(Files.exists(expectedPath), "El archivo de eventos debería existir");

        String content = Files.readString(expectedPath);
        assertTrue(content.contains(json), "El contenido del archivo debería contener el JSON del evento");
    }

    @AfterAll
    void cleanUp() throws IOException {
        deleteRecursively(Paths.get(BASE_DIR));
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Error eliminando: " + p, e);
                    }
                });
    }
}
