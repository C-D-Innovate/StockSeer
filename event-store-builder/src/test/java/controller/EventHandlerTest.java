package controller;

import es.ulpgc.dacd.eventstorebuilder.controller.EventHandler;

import es.ulpgc.dacd.eventstorebuilder.model.Event;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.port.EventStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventHandlerTest {

    private EventStorage storage;
    private EventHandler handler;

    @BeforeEach
    void setUp() {
        storage = mock(EventStorage.class);
        handler = new EventHandler(storage);
    }

    @Test
    void testHandle_validJson_shouldSaveEvent() {
        // Arrange
        String topic = "Articles";
        String ts = "2025-06-24T12:34:56Z";
        String json = String.format("""
                {
                    "ts": "%s",
                    "ss": "source-system",
                    "url": "https://example.com/article"
                }
                """, ts);


        handler.handle(topic, json);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(storage, times(1)).save(captor.capture());
        Event saved = captor.getValue();

        assertEquals(Instant.parse(ts), saved.getTs());
        assertEquals("source-system", saved.getSs());
        assertEquals(topic, saved.getTopic());
    }

    @Test
    void testHandle_missingSS_shouldDefaultToUnknown() {
        String topic = "AlphaVantageEvent";
        String ts = "2025-06-24T12:34:56Z";
        String json = String.format("""
                {
                    "ts": "%s",
                    "symbol": "TSLA"
                }
                """, ts);

        handler.handle(topic, json);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(storage).save(captor.capture());

        Event saved = captor.getValue();
        assertEquals("unknown", saved.getSs());
    }

    @Test
    void testHandle_invalidJson_shouldNotCrash() {
        String topic = "Articles";
        String invalidJson = "not a json";

        assertDoesNotThrow(() -> handler.handle(topic, invalidJson));
        verify(storage, never()).save(any());
    }

    @Test
    void testHandle_missingTimestamp_shouldNotCrash() {
        String topic = "Articles";
        String json = """
                {
                    "ss": "source"
                }
                """;

        assertDoesNotThrow(() -> handler.handle(topic, json));
        verify(storage, never()).save(any());
    }

    @Test
    void testHandle_invalidTimestamp_shouldNotCrash() {
        String topic = "Articles";
        String json = """
                {
                    "ts": "not-a-valid-ts",
                    "ss": "source"
                }
                """;

        assertDoesNotThrow(() -> handler.handle(topic, json));
        verify(storage, never()).save(any());
    }
}
