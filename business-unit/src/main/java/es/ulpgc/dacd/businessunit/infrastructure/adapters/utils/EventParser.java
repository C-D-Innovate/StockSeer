package es.ulpgc.dacd.businessunit.infrastructure.adapters.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ulpgc.dacd.businessunit.domain.model.MarketEvent;
import es.ulpgc.dacd.businessunit.domain.model.NewsEvent;

import java.time.Instant;

public class EventParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public MarketEvent parseMarketEvent(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            String symbol = root.get("symbol").asText();
            double price = root.get("high").asDouble();
            long volume = root.get("volume").asLong();
            Instant ts = Instant.parse(root.get("ts").asText());
            return new MarketEvent(symbol, price, volume, ts);
        } catch (Exception e) {
            System.err.println("[WARN] Error parseando MarketEvent: " + e.getMessage());
            return null;
        }
    }

    public NewsEvent parseNewsEvent(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            String url = root.get("url").asText();
            String fullContent = root.get("content").asText();
            Instant ts = Instant.parse(root.get("ts").asText());
            return new NewsEvent(url, fullContent, ts);
        } catch (Exception e) {
            System.err.println("[WARN] Error parseando NewsEvent: " + e.getMessage());
            return null;
        }
    }
}
