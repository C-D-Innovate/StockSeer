package es.ulpgc.dacd.businessunit.infrastructure.adapters.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ArgsParser {
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
            System.err.println("Error leyendo archivo de argumentos: " + e.getMessage());
        }
        return args;
    }
}


