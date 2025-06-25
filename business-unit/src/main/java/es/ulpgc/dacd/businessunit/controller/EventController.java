package es.ulpgc.dacd.businessunit.controller;

import es.ulpgc.dacd.businessunit.models.MarketEvent;
import es.ulpgc.dacd.businessunit.models.NewsEvent;
import es.ulpgc.dacd.businessunit.infrastructure.ports.EventStorage;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.utils.EventParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

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
                    logger.warn("Evento de mercado nulo tras parseo. Topic: {}", topic);
                }
            } else if (lowerTopic.contains("news") || lowerTopic.contains("articles")) {
                NewsEvent event = parser.parseNewsEvent(json);
                if (event != null) {
                    storage.saveNewsEvent(event);
                } else {
                    logger.warn("Evento de noticias nulo tras parseo. Topic: {}", topic);
                }
            } else {
                logger.warn("Topic desconocido: {}", topic);
            }
        } catch (Exception e) {
            logger.error("Error al manejar evento para el topic {}: {}", topic, e.getMessage(), e);
        }
    }
}
