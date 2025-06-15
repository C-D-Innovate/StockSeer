package es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.SqliteEventStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

public class HistoricalEventReader {

    public List<String> readUnprocessedEvents(Path filePath, SqliteEventStorage storage) {
        if (!Files.exists(filePath)) {
            System.err.println("[WARN] Archivo no encontrado: " + filePath);
            return Collections.emptyList();
        }

        if (isFileAlreadyProcessed(filePath, storage)) {
            System.out.println("Archivo ya procesado, se omite: " + filePath.getFileName());
            return Collections.emptyList();
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return reader.lines().toList();
        } catch (IOException e) {
            System.err.println("[ERROR] Error leyendo archivo " + filePath + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean isFileAlreadyProcessed(Path filePath, SqliteEventStorage storage) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String firstLine = reader.readLine();
            if (firstLine == null) return true;

            JsonObject json = JsonParser.parseString(firstLine).getAsJsonObject();

            if (json.has("topic") && "Articles".equals(json.get("topic").getAsString()) && json.has("url")) {
                JsonElement urlElement = json.get("url");
                if (!urlElement.isJsonNull()) {
                    String url = urlElement.getAsString();
                    return storage.containsUrl(url);
                }
            }
            return true;

        } catch (Exception e) {
            System.err.println("[WARN] No se pudo verificar el archivo: " + filePath.getFileName() + " → " + e.getMessage());
            return true;
        }
    }
}
