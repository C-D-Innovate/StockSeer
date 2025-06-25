package es.ulpgc.dacd.businessunit.infrastructure.adapters.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ulpgc.dacd.businessunit.models.MarketEvent;
import es.ulpgc.dacd.businessunit.models.NewsEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class EventParser {
    private static final Logger logger = LoggerFactory.getLogger(EventParser.class);

    private final ObjectMapper mapper;

    public EventParser() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public MarketEvent parseMarketEvent(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            String symbol = root.get("symbol").asText();
            double price = root.get("high").asDouble();
            long volume = root.get("volume").asLong();
            Instant ts = Instant.parse(root.get("ts").asText());
            return new MarketEvent(symbol, price, volume, ts);
        } catch (Exception e) {
            logger.warn("[WARN] Error parseando MarketEvent: {}", e.getMessage(), e);
            return null;
        }
    }

    public NewsEvent parseNewsEvent(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            String url = root.get("url").asText();
            Instant ts = Instant.parse(root.get("ts").asText());

            String content = root.hasNonNull("content") ? root.get("content").asText() : "";
            String fullContent = root.hasNonNull("fullContent") ? root.get("fullContent").asText() : content;

            return new NewsEvent(url, content, ts, fullContent, null);
        } catch (Exception e) {
            logger.warn("[WARN] Error parseando NewsEvent: {}", e.getMessage(), e);
            return null;
        }
    }
}