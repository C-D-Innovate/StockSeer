package es.ulpgc.dacd.eventstorebuilder.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.eventstorebuilder.domain.model.Event;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.port.EventStorage;

import java.time.Instant;

public class EventHandler {

    private final EventStorage storage;

    public EventHandler(EventStorage storage) {
        this.storage = storage;
    }

    public void handle(String topic, String messageJson) {
        try {
            JsonObject jsonObject = parseJson(messageJson);
            Event event = buildEvent(topic, jsonObject, messageJson);
            save(event);
        } catch (Exception e) {
            System.err.println("❌ Error al procesar evento: " + e.getMessage());
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