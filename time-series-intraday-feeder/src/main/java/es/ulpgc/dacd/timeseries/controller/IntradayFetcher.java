package es.ulpgc.dacd.timeseries.controller;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.provider.StockDataProvider;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.StockDataStorage;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IntradayFetcher {

    private final String symbol;
    private final StockDataProvider dataProvider;
    private final StockDataStorage dataStorage;
    private final String dbUrl;
    private ScheduledExecutorService scheduler;

    public IntradayFetcher(String symbol, StockDataProvider dataProvider, StockDataStorage dataStorage, String dbUrl) {
        this.symbol = symbol;
        this.dataProvider = dataProvider;
        this.dataStorage = dataStorage;
        this.dbUrl = dbUrl;
    }

    public void startFetchingEvery(int intervalMinutes) {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("→ Ejecutando fetch de datos para " + symbol);
                List<AlphaVantageEvent> stockData = dataProvider.fetch(symbol);

                if (stockData != null && !stockData.isEmpty()) {
                    dataStorage.saveAll(stockData, dbUrl);
                    System.out.println("✓ Datos almacenados correctamente.\n");
                } else {
                    System.err.println("No se obtuvieron datos para el símbolo: " + symbol + "\n");
                }
            } catch (Exception e) {
                System.err.println("Error durante la ejecución del fetch: " + e.getMessage());
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("Scheduler detenido correctamente.");
        }
    }
}