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

    public MarketEvent parseMarketEvent(String jsonOpenLine, String jsonCloseLine) {
        try {

            JsonNode openNode = mapper.readTree(jsonOpenLine);
            String tsOpenString = openNode.get("ts").asText();
            if (!tsOpenString.endsWith("T13:30:00Z")) return null;

            String symbol = openNode.get("symbol").asText();
            double open = openNode.get("open").asDouble();
            Instant open_ts = Instant.parse(tsOpenString);

            JsonNode closeNode = mapper.readTree(jsonCloseLine);
            String tsCloseString = closeNode.get("ts").asText();
            if (!tsCloseString.endsWith("T20:00:00Z")) return null;

            long volume = closeNode.get("volume").asLong();
            double close = closeNode.get("close").asDouble();
            Instant close_ts = Instant.parse(tsCloseString);

            return new MarketEvent(symbol, volume, open_ts, open, close_ts, close);

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

    public JsonNode readJson(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            logger.warn("Error leyendo JSON: {}", e.getMessage(), e);
            return null;
        }
    }

}
