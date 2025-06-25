package es.ulpgc.dacd.eventstorebuilder.infrastructure.port;

import es.ulpgc.dacd.eventstorebuilder.model.Event;

public interface EventStorage {
    void save(Event event);
}