package es.ulpgc.dacd.newsapi.infrastructure.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DuplicateUrlChecker {
    private final Set<String> seenUrls = new HashSet<>();
    private final File storageFile;

    public DuplicateUrlChecker(String resourceFilePath) {
        this.storageFile = new File(resourceFilePath);
        loadUrlsFromFile();
    }

    private void loadUrlsFromFile() {
        if (!storageFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                seenUrls.add(line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de URLs", e);
        }
    }

    public boolean isDuplicate(String url) {
        return seenUrls.contains(url);
    }

    public void markAsSeen(String url) {
        if (seenUrls.add(url)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile, true))) {
                writer.write(url);
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException("Error al escribir URL en archivo", e);
            }
        }
    }
}

