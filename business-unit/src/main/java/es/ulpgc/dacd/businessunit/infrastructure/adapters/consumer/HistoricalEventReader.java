package es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class HistoricalEventReader {
    public List<String> readEvents(Path filePath) {
        if (!Files.exists(filePath)) {
            System.err.println("[WARN] Archivo no encontrado: " + filePath);
            return Collections.emptyList();
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return reader.lines().toList();
        } catch (IOException e) {
            System.err.println("[ERROR] Error leyendo archivo " + filePath + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
