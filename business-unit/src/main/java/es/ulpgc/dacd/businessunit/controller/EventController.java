package es.ulpgc.dacd.businessunit.controller;

import es.ulpgc.dacd.businessunit.domain.model.MarketEvent;
import es.ulpgc.dacd.businessunit.domain.model.NewsEvent;
import es.ulpgc.dacd.businessunit.infrastructure.ports.EventStorage;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.utils.EventParser;

public class EventController {

    private final EventStorage storage;
    private final EventParser parser;

    public EventController(EventStorage storage) {
        this.storage = storage;
        this.parser = new EventParser();
    }

    public void handle(String topic, String json) {
        try {
            String lowerTopic = topic.toLowerCase();

            if (lowerTopic.contains("alphavantage")) {
                MarketEvent event = parser.parseMarketEvent(json);
                if (event != null) {
                    storage.saveMarketEvent(event);
                } else {
                    System.err.println("[WARN] Evento de mercado nulo tras parseo.");
                }
            } else if (lowerTopic.contains("news") || lowerTopic.contains("articles")) {
                NewsEvent event = parser.parseNewsEvent(json);
                if (event != null) {
                    storage.saveNewsEvent(event);
                } else {
                    System.err.println("[WARN] Evento de noticias nulo tras parseo.");
                }
            } else {
                System.err.println("[WARN] Topic desconocido: " + topic);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error al manejar evento: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
