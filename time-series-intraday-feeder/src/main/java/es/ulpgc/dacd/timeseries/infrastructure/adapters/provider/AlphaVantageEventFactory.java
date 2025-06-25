package es.ulpgc.dacd.timeseries.infrastructure.adapters.provider;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import es.ulpgc.dacd.timeseries.infrastructure.utils.TimestampParser;

import java.time.Instant;

public class AlphaVantageEventFactory {

    private final TimestampParser timestampParser = new TimestampParser();

    public AlphaVantageEvent create(String symbol, StockUnit unit) {
        Instant ts = timestampParser.parse(unit.getDate());
        return new AlphaVantageEvent(
                symbol,
                ts,
                unit.getOpen(),
                unit.getHigh(),
                unit.getLow(),
                unit.getClose(),
                unit.getVolume()
        );
    }
}