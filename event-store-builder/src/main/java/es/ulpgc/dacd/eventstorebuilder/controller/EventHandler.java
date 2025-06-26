package es.ulpgc.dacd.eventstorebuilder.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.eventstorebuilder.model.Event;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.port.EventStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private final EventStorage storage;

    public EventHandler(EventStorage storage) {
        this.storage = storage;
    }

    public void handle(String topic, String messageJson) {
        try {
            JsonObject jsonObject = parseJson(messageJson);
            Event event = buildEvent(topic, jsonObject, messageJson);
            save(event);
            logger.info("Evento procesado: topic={}, ts={}", topic, event.getTs());
        } catch (Exception e) {
            logger.error("Error al procesar evento (topic={}): {}", topic, e.getMessage(), e);
        }
    }

    private JsonObject parseJson(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private Event buildEvent(String topic, JsonObject json, String rawJson) {
        Instant ts = Instant.parse(json.get("ts").getAsString());
        String ss = json.has("ss") ? json.get("ss").getAsString() : "unknown";
        return new Event(ts, ss, topic, rawJson);
    }

    private void save(Event event) {
        storage.save(event);
    }
}
