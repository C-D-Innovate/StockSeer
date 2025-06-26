package es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer;

import es.ulpgc.dacd.businessunit.controller.EventController;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datalake.SQLiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class HistoricalEventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HistoricalEventProcessor.class);

    private final HistoricalEventReader reader;
    private final EventController handler;
    private final SQLiteManager storage;

    public HistoricalEventProcessor(HistoricalEventReader reader, EventController handler, SQLiteManager storage) {
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
            logger.error("Error accediendo a archivos: {}", e.getMessage(), e);
        }
    }

    private void processEventFile(Path path) {
        String topic = inferTopicFromPath(path);
        List<String> lines = reader.readUnprocessedEvents(path, storage, topic);
        if (!lines.isEmpty()) {
            logger.info("Procesando archivo: {} (topic: {})", path.getFileName(), topic);
        }
        for (String json : lines) {
            handler.handle(topic, json);
        }
    }


    private String inferTopicFromPath(Path path) {
        for (Path segment : path) {
            String name = segment.toString();
            if (name.equalsIgnoreCase("Articles")) return "Articles";
            if (name.equalsIgnoreCase("StockQuotes")) return "StockQuotes";
        }
        return "unknown";
    }
}
