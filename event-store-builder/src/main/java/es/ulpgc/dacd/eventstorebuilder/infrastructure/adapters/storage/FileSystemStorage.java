package es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.storage;

import es.ulpgc.dacd.eventstorebuilder.domain.model.Event;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.port.EventStorage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FileSystemStorage implements EventStorage {

    private static final String BASE_DIR = "eventstore";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("UTC"));

    @Override
    public void save(Event event) {
        Path path = buildEventFilePath(event);
        ensureDirectoryExists(path);
        appendEventToFile(path, event.getJson());
    }

    private Path buildEventFilePath(Event event) {
        String date = DATE_FORMAT.format(event.getTs());
        return Paths.get(BASE_DIR, event.getTopic(), event.getSs(), date + ".events");
    }

    private void ensureDirectoryExists(Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            System.err.println("❌ Error al crear directorios: " + path.getParent());
        }
    }

    private void appendEventToFile(Path path, String json) {
        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(json);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("❌ Error al escribir evento en archivo: " + path);
        }
    }
}