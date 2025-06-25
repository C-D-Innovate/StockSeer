package infrastructure.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.utils.EventJsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EventJsonSerializerTest {

    private EventJsonSerializer serializer;
    private AlphaVantageEvent sampleEvent;

    @BeforeEach
    void setUp() {
        serializer = new EventJsonSerializer();
        sampleEvent = new AlphaVantageEvent(
                "GOOG",
                Instant.parse("2025-06-24T14:30:00Z"),
                2500.12,
                2510.34,
                2490.56,
                2505.78,
                1_234_567L
        );
    }

    @Test
    void serialize_IncluyeTodosLosCamposYTema() {
        String jsonStr = serializer.serialize(sampleEvent);
        JsonObject obj = JsonParser.parseString(jsonStr).getAsJsonObject();

        assertEquals("GOOG",                  obj.get("symbol").getAsString());
        assertEquals("AlphaVantage",         obj.get("ss").getAsString());
        assertEquals("2025-06-24T14:30:00Z", obj.get("ts").getAsString());
        assertEquals(2500.12,                 obj.get("open").getAsDouble());
        assertEquals(2510.34,                 obj.get("high").getAsDouble());
        assertEquals(2490.56,                 obj.get("low").getAsDouble());
        assertEquals(2505.78,                 obj.get("close").getAsDouble());
        assertEquals(1_234_567L,              obj.get("volume").getAsLong());

        assertTrue(obj.has("topic"), "Debe incluir la propiedad 'topic'");
        assertEquals("AlphaVantageEvent", obj.get("topic").getAsString());
    }

    @Test
    void serialize_TimestampEnFormatoIso8601() {
        String jsonStr = serializer.serialize(sampleEvent);
        JsonObject obj = JsonParser.parseString(jsonStr).getAsJsonObject();

        String tsValue = obj.get("ts").getAsString();
        assertEquals(sampleEvent.getTs().toString(), tsValue,
                "El campo 'ts' debe serializarse como Instant.toString()");
    }
}