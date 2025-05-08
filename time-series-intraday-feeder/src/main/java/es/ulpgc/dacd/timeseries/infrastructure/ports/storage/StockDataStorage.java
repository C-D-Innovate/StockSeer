package es.ulpgc.dacd.timeseries.infrastructure.ports.storage;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;

import java.util.List;

public interface StockDataStorage {
    void saveOpeningAndClosingEvents(List<AlphaVantageEvent> data, String context);
}