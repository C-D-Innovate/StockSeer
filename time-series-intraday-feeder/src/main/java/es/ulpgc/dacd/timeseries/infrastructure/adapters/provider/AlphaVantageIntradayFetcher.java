package es.ulpgc.dacd.timeseries.infrastructure.adapters.provider;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.provider.IntradayStockEventFetcher;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;

import java.util.List;
import java.util.logging.Logger;

public class AlphaVantageIntradayFetcher implements IntradayStockEventFetcher {

    private static final Logger logger = Logger.getLogger(AlphaVantageIntradayFetcher.class.getName());
    private final AlphaVantageResponseProcessor responseProcessor;

    public AlphaVantageIntradayFetcher(String apiKey) {
        new AlphaVantageInitializer(apiKey);
        this.responseProcessor = new AlphaVantageResponseProcessor();
    }

    @Override
    public List<AlphaVantageEvent> fetch(String symbol) {
        try {
            TimeSeriesResponse response = com.crazzyghost.alphavantage.AlphaVantage.api()
                    .timeSeries()
                    .intraday()
                    .forSymbol(symbol)
                    .interval(com.crazzyghost.alphavantage.parameters.Interval.ONE_MIN)
                    .outputSize(com.crazzyghost.alphavantage.parameters.OutputSize.FULL)
                    .fetchSync();

            return responseProcessor.process(response, symbol);
        } catch (Exception e) {
            logger.warning("Error al recuperar datos para el símbolo: " + symbol + " → " + e.getMessage()+ "\n");
            return List.of();
        }
    }
}