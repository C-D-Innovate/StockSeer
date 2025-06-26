package es.ulpgc.dacd.businessunit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import es.ulpgc.dacd.businessunit.models.MarketEvent;
import es.ulpgc.dacd.businessunit.models.NewsEvent;
import es.ulpgc.dacd.businessunit.infrastructure.ports.EventStorage;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.utils.EventParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final EventStorage storage;
    private final EventParser parser;

    private final Map<String, JsonNode> pendingMarketOpen = new HashMap<>();
    private final Map<String, JsonNode> pendingMarketClose = new HashMap<>();

    public EventController(EventStorage storage) {
        this.storage = storage;
        this.parser = new EventParser();
    }

    public void handle(String topic, String json) {
        try {
            String lowerTopic = topic.toLowerCase();

            if (lowerTopic.contains("stockquotes")) {
                JsonNode node = parser.readJson(json);
                if (node == null || !node.has("ts")) return;

                String ts = node.get("ts").asText();
                String date = ts.substring(0, 10);
                String hour = ts.substring(11, 16);

                if (hour.equals("13:30")) {
                    pendingMarketOpen.put(date, node);
                } else if (hour.equals("20:00")) {
                    pendingMarketClose.put(date, node);
                }

                if (pendingMarketOpen.containsKey(date) && pendingMarketClose.containsKey(date)) {
                    JsonNode openNode = pendingMarketOpen.get(date);
                    JsonNode closeNode = pendingMarketClose.get(date);

                    MarketEvent event = parser.parseMarketEvent(openNode.toString(), closeNode.toString());
                    if (event != null) {
                        storage.saveMarketEvent(event);
                        logger.info("MarketEvent guardado para fecha {}", date);
                    } else {
                        logger.warn("No se pudo crear MarketEvent para fecha {}", date);
                    }

                    pendingMarketOpen.remove(date);
                    pendingMarketClose.remove(date);
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
