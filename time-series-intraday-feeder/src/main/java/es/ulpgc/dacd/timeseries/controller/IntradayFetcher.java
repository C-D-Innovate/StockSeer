package es.ulpgc.dacd.timeseries.controller;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.util.TimeUtils;
import es.ulpgc.dacd.timeseries.infrastructure.ports.provider.StockDataProvider;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.StockDataStorage;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IntradayFetcher {

    private final String symbol;
    private final StockDataProvider dataProvider;
    private final StockDataStorage dataStorage;
    private final String context;

    private final ZoneId zone = ZoneId.of("America/New_York");
    private final LocalTime marketClose = LocalTime.of(20, 5);

    public IntradayFetcher(String symbol, StockDataProvider dataProvider, StockDataStorage dataStorage, String context) {
        this.symbol = symbol;
        this.dataProvider = dataProvider;
        this.dataStorage = dataStorage;
        this.context = context;
    }

    public void start() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LocalTime now = LocalTime.now(zone).withSecond(0);

                if (TimeUtils.isWithinOneMinuteOf(now, marketClose)) {
                    fetchAndStore();
                    timer.cancel();
                } else {
                    System.out.println("[INFO] Esperando cierre del mercado. Hora actual: " + now);
                }
            }
        }, 0, 60_000);
    }

    private void fetchAndStore() {
        try {
            System.out.println("[INFO] Solicitando datos para: " + symbol);
            List<AlphaVantageEvent> stockData = dataProvider.fetch(symbol);

            if (stockData != null && !stockData.isEmpty()) {
                dataStorage.saveOpeningAndClosingEvents(stockData, context);
                System.out.println("[INFO] Datos procesados correctamente.\n");
            } else {
                System.err.println("[WARN] No se recibieron datos del proveedor.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error durante la ejecución del fetch: " + e.getMessage());
        }
    }
}