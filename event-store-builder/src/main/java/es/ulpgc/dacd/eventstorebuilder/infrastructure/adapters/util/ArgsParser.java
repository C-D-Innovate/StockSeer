package es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ArgsParser {

    private static final Logger logger = LoggerFactory.getLogger(ArgsParser.class);

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
            logger.error("Error leyendo archivo de argumentos '{}': {}", filePath, e.getMessage(), e);
        }
        return args;
    }
}
