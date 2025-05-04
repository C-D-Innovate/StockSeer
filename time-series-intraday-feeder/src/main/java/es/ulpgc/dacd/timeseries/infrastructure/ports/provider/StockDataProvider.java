package es.ulpgc.dacd.timeseries.infrastructure.ports.provider;
import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import java.util.List;

public interface StockDataProvider {
    List<AlphaVantageEvent> fetch(String symbol);
}
