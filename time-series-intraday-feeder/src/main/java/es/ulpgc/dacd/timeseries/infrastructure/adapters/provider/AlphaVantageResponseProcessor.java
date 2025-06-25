package es.ulpgc.dacd.timeseries.infrastructure.adapters.provider;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;

import java.util.ArrayList;
import java.util.List;

public class AlphaVantageResponseProcessor {

    private final AlphaVantageEventFactory eventFactory = new AlphaVantageEventFactory();

    public List<AlphaVantageEvent> process(TimeSeriesResponse response, String symbol) {
        List<AlphaVantageEvent> events = new ArrayList<>();
        if (response != null && response.getStockUnits() != null) {
            for (StockUnit unit : response.getStockUnits()) {
                events.add(eventFactory.create(symbol, unit));
            }
        }
        return events;
    }
}