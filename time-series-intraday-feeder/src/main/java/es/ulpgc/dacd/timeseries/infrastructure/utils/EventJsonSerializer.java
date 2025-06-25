package es.ulpgc.dacd.timeseries.infrastructure.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;

import java.io.IOException;
import java.time.Instant;
import java.util.logging.Logger;

public class EventJsonSerializer {
    private static final Logger logger = Logger.getLogger(EventJsonSerializer.class.getName());
    private final Gson gson;

    public EventJsonSerializer() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
    }

    public String serialize(AlphaVantageEvent event) {
        JsonObject json = gson.toJsonTree(event).getAsJsonObject();
        return gson.toJson(json);
    }

    private static class InstantAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            return Instant.parse(in.nextString());
        }
    }
}