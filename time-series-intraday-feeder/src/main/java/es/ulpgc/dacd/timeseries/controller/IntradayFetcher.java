package es.ulpgc.dacd.timeseries.controller;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.provider.IntradayStockEventFetcher;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.OpeningClosingEventSaver;
import es.ulpgc.dacd.timeseries.infrastructure.utils.MarketCloseScheduler;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.logging.Logger;

public class IntradayFetcher {

    private static final Logger logger = Logger.getLogger(IntradayFetcher.class.getName());

    private final String symbol;
    private final IntradayStockEventFetcher dataProvider;
    private final OpeningClosingEventSaver dataStorage;
    private final String context;
    private final LocalTime marketClose;

    private final ZoneId zone = ZoneId.of("America/New_York");

    public IntradayFetcher(String symbol,
                           IntradayStockEventFetcher dataProvider,
                           OpeningClosingEventSaver dataStorage,
                           String context,
                           LocalTime marketClose) {
        this.symbol = symbol;
        this.dataProvider = dataProvider;
        this.dataStorage = dataStorage;
        this.context = context;
        this.marketClose = marketClose;
    }

    public void start() {
        logger.info("[Fetcher] Iniciando espera hasta el cierre del mercado para símbolo: " + symbol + "\n");
        MarketCloseScheduler scheduler = new MarketCloseScheduler(zone, marketClose);
        scheduler.start(this::fetchAndStore);
    }

    private void fetchAndStore() {
        try {
            logger.info("[Fetcher] Solicitando datos de AlphaVantage para: " + symbol + "\n");
            List<AlphaVantageEvent> stockData = dataProvider.fetch(symbol);

            if (stockData != null && !stockData.isEmpty()) {
                dataStorage.saveOpeningAndClosingEvents(stockData, context);
            } else {
                logger.warning("[Fetcher] No se recibieron datos del proveedor para el símbolo: " + symbol + "\n");
            }
        } catch (Exception e) {
            logger.severe("[Fetcher] Error durante el proceso de obtención o guardado de datos: " + e.getMessage() + "\n");
        }
    }
}