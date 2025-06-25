package es.ulpgc.dacd.timeseries.infrastructure.ports.storage;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;

import java.util.List;

public interface OpeningClosingEventSaver {
    void saveOpeningAndClosingEvents(List<AlphaVantageEvent> data, String context);
}