package es.ulpgc.dacd.businessunit.controller;

import es.ulpgc.dacd.businessunit.domain.model.MarketEvent;
import es.ulpgc.dacd.businessunit.domain.model.NewsEvent;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.ports.out.EventStorage;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.utils.EventParser;

public class EventHandler {

    private final EventStorage storage;
    private final EventParser parser;

    public EventHandler(EventStorage storage) {
        this.storage = storage;
        this.parser = new EventParser();
    }

    public void handle(String topic, String json) {
        try {
            if (topic.toLowerCase().contains("alphavantage")) {
                MarketEvent event = parser.parseMarketEvent(json);
                if (event != null) storage.saveMarketEvent(event);
            } else if (topic.toLowerCase().contains("news") || topic.toLowerCase().contains("articles")) {
                NewsEvent event = parser.parseNewsEvent(json);
                if (event != null) storage.saveNewsEvent(event);
            } else {
                System.err.println("❓ Topic desconocido: " + topic);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al manejar evento: " + e.getMessage());
        }
    }

}
