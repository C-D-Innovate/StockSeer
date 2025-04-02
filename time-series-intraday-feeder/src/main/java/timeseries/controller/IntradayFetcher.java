package timeseries.controller;

import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import timeseries.domain.model.StockData;
import timeseries.domain.model.StockDataRepository;
import timeseries.domain.usecase.StoreStockDataUseCase;
import timeseries.domain.usecase.FetchStockDataUseCase;
import timeseries.infrastructure.adapters.api.AlphaVantageAPIAdapter;
import timeseries.infrastructure.adapters.database.StockDataRepositoryImpl;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IntradayFetcher {

    private final FetchStockDataUseCase fetchUseCase;
    private final StoreStockDataUseCase storeUseCase;
    private final String symbol;

    public IntradayFetcher(String apiKey, String dbUrl, String symbol, Interval interval, OutputSize outputSize) {
        this.symbol = symbol;
        this.fetchUseCase = new AlphaVantageAPIAdapter(apiKey, interval, outputSize);
        StockDataRepository repository = new StockDataRepositoryImpl(dbUrl);
        this.storeUseCase = new StoreStockDataUseCase(repository);
    }

    public void startFetchingEvery(int intervalMinutes) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("→ Ejecutando fetch de datos para " + symbol);
                List<StockData> stockData = fetchUseCase.fetch(symbol);
                storeUseCase.store(stockData);
                System.out.println("✓ Datos almacenados correctamente.\n");
            } catch (Exception e) {
                System.err.println("⚠️ Error durante la ejecución del fetch: " + e.getMessage());
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);

        // Hook para cerrar el scheduler al cerrar la app
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cerrando scheduler...");
            scheduler.shutdown();
        }));
    }
}

