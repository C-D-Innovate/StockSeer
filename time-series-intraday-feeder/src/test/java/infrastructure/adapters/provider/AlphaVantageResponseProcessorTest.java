package infrastructure.adapters.provider;

import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.provider.AlphaVantageResponseProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlphaVantageResponseProcessorTest {

    private AlphaVantageResponseProcessor processor;

    @Mock
    private TimeSeriesResponse response;

    @Mock
    private StockUnit unit1, unit2;

    @BeforeEach
    void setUp() {
        processor = new AlphaVantageResponseProcessor();
    }

    @Test
    void process_NullResponse_ReturnsEmptyList() {
        List<AlphaVantageEvent> events = processor.process(null, "SYM");
        assertNotNull(events);
        assertTrue(events.isEmpty(), "Debe retornar lista vacía si la respuesta es null");
    }

    @Test
    void process_NullStockUnits_ReturnsEmptyList() {
        when(response.getStockUnits()).thenReturn(null);
        List<AlphaVantageEvent> events = processor.process(response, "SYM");
        assertNotNull(events);
        assertTrue(events.isEmpty(), "Debe retornar lista vacía si getStockUnits() es null");
    }

    @Test
    void process_EmptyStockUnits_ReturnsEmptyList() {
        when(response.getStockUnits()).thenReturn(Collections.emptyList());
        List<AlphaVantageEvent> events = processor.process(response, "SYM");
        assertNotNull(events);
        assertTrue(events.isEmpty(), "Debe retornar lista vacía si no hay unidades");
    }

    @Test
    void process_MultipleUnits_ReturnsMappedEvents() {
        // Preparo dos unidades de ejemplo
        when(unit1.getDate()).thenReturn("2025-06-24 09:30:00");
        when(unit1.getOpen()).thenReturn(10.0);
        when(unit1.getHigh()).thenReturn(11.0);
        when(unit1.getLow()).thenReturn(9.5);
        when(unit1.getClose()).thenReturn(10.5);
        when(unit1.getVolume()).thenReturn(1000L);

        when(unit2.getDate()).thenReturn("2025-06-24 16:00:00");
        when(unit2.getOpen()).thenReturn(20.0);
        when(unit2.getHigh()).thenReturn(21.0);
        when(unit2.getLow()).thenReturn(19.5);
        when(unit2.getClose()).thenReturn(20.5);
        when(unit2.getVolume()).thenReturn(2000L);

        when(response.getStockUnits()).thenReturn(List.of(unit1, unit2));

        String symbol = "FOO";
        List<AlphaVantageEvent> events = processor.process(response, symbol);

        assertEquals(2, events.size(), "Debe mapear dos unidades a dos eventos");

        AlphaVantageEvent e1 = events.get(0);
        Instant expectedTs1 = ZonedDateTime.of(
                2025, 6, 24, 9, 30, 0, 0,
                ZoneId.of("America/New_York")
        ).toInstant();
        assertEquals(symbol, e1.getSymbol());
        assertEquals(expectedTs1, e1.getTs());
        assertEquals(10.0, e1.getOpen());
        assertEquals(11.0, e1.getHigh());
        assertEquals(9.5,  e1.getLow());
        assertEquals(10.5, e1.getClose());
        assertEquals(1000L, e1.getVolume());

        AlphaVantageEvent e2 = events.get(1);
        Instant expectedTs2 = ZonedDateTime.of(
                2025, 6, 24, 16, 0, 0, 0,
                ZoneId.of("America/New_York")
        ).toInstant();
        assertEquals(symbol, e2.getSymbol());
        assertEquals(expectedTs2, e2.getTs());
        assertEquals(20.0, e2.getOpen());
        assertEquals(21.0, e2.getHigh());
        assertEquals(19.5,  e2.getLow());
        assertEquals(20.5, e2.getClose());
        assertEquals(2000L, e2.getVolume());
    }
}