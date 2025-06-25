package es.ulpgc.dacd.timeseries.infrastructure.ports.provider;
import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import java.util.List;

public interface IntradayStockEventFetcher {
    List<AlphaVantageEvent> fetch(String symbol);
}
