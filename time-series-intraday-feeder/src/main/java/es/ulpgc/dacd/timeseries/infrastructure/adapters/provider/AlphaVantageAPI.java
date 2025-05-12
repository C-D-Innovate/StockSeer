package es.ulpgc.dacd.timeseries.infrastructure.adapters.provider;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.provider.StockDataProvider;
import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AlphaVantageAPI implements StockDataProvider {

    private final Interval interval;
    private final OutputSize outputSize;

    public AlphaVantageAPI(String apiKey) {
        this(apiKey, Interval.ONE_MIN, OutputSize.FULL);
    }

    public AlphaVantageAPI(String apiKey, Interval interval, OutputSize outputSize) {
        initializeAlphaVantage(apiKey);
        this.interval = interval;
        this.outputSize = outputSize;
    }

    private void initializeAlphaVantage(String apiKey) {
        Config cfg = Config.builder()
                .key(apiKey)
                .timeOut(10)
                .build();
        AlphaVantage.api().init(cfg);
    }

    @Override
    public List<AlphaVantageEvent> fetch(String symbol) {
        TimeSeriesResponse response = fetchTimeSeriesData(symbol);
        return processResponse(response, symbol);
    }

    private TimeSeriesResponse fetchTimeSeriesData(String symbol) {
        try {
            return AlphaVantage.api()
                    .timeSeries()
                    .intraday()
                    .forSymbol(symbol)
                    .interval(interval)
                    .outputSize(outputSize)
                    .fetchSync();
        } catch (Exception e) {
            System.err.println("Error al obtener los datos de la API para el símbolo: " + symbol);
            return null;
        }
    }

    private List<AlphaVantageEvent> processResponse(TimeSeriesResponse response, String symbol) {
        List<AlphaVantageEvent> events = new ArrayList<>();
        if (response != null && response.getStockUnits() != null) {
            for (StockUnit unit : response.getStockUnits()) {
                events.add(createAlphaVantageEvent(symbol, unit));
            }
        }
        return events;
    }

    private AlphaVantageEvent createAlphaVantageEvent(String symbol, StockUnit unit) {
        Instant timestamp = parseTimestamp(unit.getDate());
        return new AlphaVantageEvent(
                symbol,
                timestamp,
                unit.getOpen(),
                unit.getHigh(),
                unit.getLow(),
                unit.getClose(),
                unit.getVolume()
        );
    }

    private Instant parseTimestamp(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("America/New_York"));
        return zonedDateTime.toInstant();
    }

}
