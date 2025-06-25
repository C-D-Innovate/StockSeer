package es.ulpgc.dacd.newsapi.infrastructure.adapters.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArgsParser {
    private static final Logger logger = Logger.getLogger(ArgsParser.class.getName());

    public static Map<String, String> parse(String filePath) {
        Map<String, String> args = new HashMap<>();
        try {
            for (String line : Files.readAllLines(Path.of(filePath))) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    args.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error leyendo archivo de argumentos: " + e.getMessage(), e);
        }
        return args;
    }
}
