package es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datalake.SQLiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

public class HistoricalEventReader {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalEventReader.class);

    public List<String> readUnprocessedEvents(Path filePath, SQLiteManager storage, String topic) {
        if (!Files.exists(filePath)) {
            logger.warn("Archivo no encontrado: {}", filePath);
            return Collections.emptyList();
        }

        if (isFileAlreadyProcessed(filePath, storage)) {
            logger.info("Archivo ya procesado, se omite: {} (topic: {})", filePath.getFileName(), topic);
            return Collections.emptyList();
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return reader.lines().toList();
        } catch (IOException e) {
            logger.error("Error leyendo archivo {}: {}", filePath, e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    private boolean isFileAlreadyProcessed(Path filePath, SQLiteManager storage) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String firstLine = reader.readLine();
            if (firstLine == null) return true;

            JsonObject json = JsonParser.parseString(firstLine).getAsJsonObject();

            if (json.has("topic")) {
                String topic = json.get("topic").getAsString();

                if ("Articles".equalsIgnoreCase(topic) && json.has("url")) {
                    JsonElement urlElement = json.get("url");
                    if (!urlElement.isJsonNull()) {
                        String url = urlElement.getAsString();
                        return storage.containsUrl(url);
                    }
                }

                if ("AlphaVantageEvent".equalsIgnoreCase(topic) && json.has("symbol") && json.has("ts")) {
                    String symbol = json.get("symbol").getAsString();
                    String ts = json.get("ts").getAsString();
                    return storage.containsMarket(symbol, ts);
                }
            }

            return false;

        } catch (Exception e) {
            logger.warn("No se pudo verificar el archivo {} → {}", filePath.getFileName(), e.getMessage(), e);
            return false;
        }
    }
}
