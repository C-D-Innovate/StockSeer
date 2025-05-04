package es.ulpgc.dacd.timeseries.infrastructure.ports.storage;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;

import java.util.List;

public interface StockDataStorage {

    void saveAll(List<AlphaVantageEvent> data, String dbUrl);

    boolean isDuplicate(AlphaVantageEvent data, String dbUrl);
}