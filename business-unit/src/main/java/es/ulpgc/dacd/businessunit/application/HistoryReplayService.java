package es.ulpgc.dacd.businessunit.application;

import es.ulpgc.dacd.businessunit.controller.EventController;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer.HistoricalEventReader;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.SqliteEventStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class HistoryReplayService {
    private final HistoricalEventReader reader;
    private final EventController handler;
    private final SqliteEventStorage storage;

    public HistoryReplayService(HistoricalEventReader reader, EventController handler, SqliteEventStorage storage) {
        this.reader = reader;
        this.handler = handler;
        this.storage = storage;
    }
    public void replayFromDirectory(Path basePath) {
        try (Stream<Path> files = Files.walk(basePath)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".events"))
                    .forEach(this::processEventFile);
        } catch (IOException e) {
            System.err.println("[ERROR] Error accediendo a archivos: " + e.getMessage());
        }
    }

    private void processEventFile(Path path) {
        String topic = inferTopicFromPath(path);
        List<String> lines = reader.readUnprocessedEvents(path, storage);
        for (String json : lines) {
            handler.handle(topic, json);
        }
    }

    private String inferTopicFromPath(Path path) {
        for (Path segment : path) {
            String name = segment.toString();
            if (name.equalsIgnoreCase("Articles")) return "Articles";
            if (name.equalsIgnoreCase("AlphaVantageEvent")) return "AlphaVantageEvent";
        }
        return "unknown";
    }
}
