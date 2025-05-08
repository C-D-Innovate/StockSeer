package es.ulpgc.dacd.timeseries;

import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.OutputSize;

import es.ulpgc.dacd.timeseries.controller.IntradayFetcher;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.provider.AlphaVantageAPI;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.SqliteManager;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.ActivemqPublisher;
import es.ulpgc.dacd.timeseries.infrastructure.ports.provider.StockDataProvider;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.StockDataStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            Map<String, String> argsValues = loadArgsFromFile(args[0]);

            StockDataProvider provider = new AlphaVantageAPI(
                    argsValues.get("API_KEY")
            );

            StockDataStorage storage;
            if (argsValues.get("STORAGE_MODE").equalsIgnoreCase("activemq")) {
                storage = new ActivemqPublisher(
                        argsValues.get("BROKER_URL"),
                        argsValues.get("TOPIC_NAME")
                );
            } else {
                storage = new SqliteManager(argsValues.get("DB_URL"));
            }

            IntradayFetcher fetcher = new IntradayFetcher(
                    argsValues.get("SYMBOL"),
                    provider,
                    storage,
                    argsValues.get("DB_URL")
            );

            fetcher.start();

        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
        }
    }

    private static Map<String, String> loadArgsFromFile(String path) throws IOException {
        File argsFile = new File(path);
        return Arrays.stream(Files.readString(argsFile.toPath()).split("\n"))
                .filter(s -> !s.trim().isEmpty())
                .map(line -> line.trim().split("="))
                .collect(Collectors.toMap(l -> l[0].trim(), l -> l[1].trim()));
    }
}
