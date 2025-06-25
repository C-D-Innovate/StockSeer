package model;

import es.ulpgc.dacd.eventstorebuilder.model.Event;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void testEventConstructor_andGetters() {
        // Arrange
        Instant expectedTs = Instant.parse("2025-06-24T12:34:56Z");
        String expectedSs = "source-system";
        String expectedTopic = "Articles";
        String expectedJson = """
                {
                    "ts": "2025-06-24T12:34:56Z",
                    "ss": "source-system",
                    "url": "https://example.com/article"
                }
                """;

        Event event = new Event(expectedTs, expectedSs, expectedTopic, expectedJson);

        assertEquals(expectedTs, event.getTs());
        assertEquals(expectedSs, event.getSs());
        assertEquals(expectedTopic, event.getTopic());
        assertEquals(expectedJson, event.getJson());
    }

    @Test
    void testEventConstructor_withNullSS_shouldHandleNull() {

        Instant expectedTs = Instant.parse("2025-06-24T12:34:56Z");
        String expectedSs = null;
        String expectedTopic = "AlphaVantageEvent";
        String expectedJson = """
                {
                    "ts": "2025-06-24T12:34:56Z",
                    "symbol": "TSLA"
                }
                """;


        Event event = new Event(expectedTs, expectedSs, expectedTopic, expectedJson);

        assertEquals(expectedTs, event.getTs());
        assertNull(event.getSs());
        assertEquals(expectedTopic, event.getTopic());
        assertEquals(expectedJson, event.getJson());
    }

    @Test
    void testEventConstructor_withMissingData_shouldNotCrash() {

        Instant expectedTs = Instant.parse("2025-06-24T12:34:56Z");
        String expectedSs = "source-system";
        String expectedTopic = "Articles";
        String expectedJson = "{ \"ts\": \"2025-06-24T12:34:56Z\" }";

        Event event = new Event(expectedTs, expectedSs, expectedTopic, expectedJson);

        assertEquals(expectedTs, event.getTs());
        assertEquals(expectedSs, event.getSs());
        assertEquals(expectedTopic, event.getTopic());
        assertEquals(expectedJson, event.getJson());
    }
}

