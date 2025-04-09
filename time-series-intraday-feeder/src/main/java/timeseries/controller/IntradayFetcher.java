package timeseries.controller;

import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import timeseries.domain.model.StockData;
import timeseries.infrastructure.adapters.api.AlphaVantageAPI;
import timeseries.infrastructure.adapters.database.DatabaseManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IntradayFetcher {
    private final String symbol;
    private final AlphaVantageAPI apiAdapter;
    private final String dbUrl;

    public IntradayFetcher(String apiKey, String dbUrl, String symbol, Interval interval, OutputSize outputSize) {
        this.symbol = symbol;
        this.apiAdapter = new AlphaVantageAPI(apiKey, interval, outputSize);
        this.dbUrl = dbUrl;
    }

    public void startFetchingEvery(int intervalMinutes) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("→ Ejecutando fetch de datos para " + symbol);
                List<StockData> stockData = apiAdapter.fetch(symbol);

                DatabaseManager.saveAll(stockData, dbUrl);

                System.out.println("✓ Datos almacenados correctamente.\n");
            } catch (Exception e) {
                System.err.println("Error durante la ejecución del fetch: " + e.getMessage());
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cerrando scheduler...");
            scheduler.shutdown();
        }));
    }
}
