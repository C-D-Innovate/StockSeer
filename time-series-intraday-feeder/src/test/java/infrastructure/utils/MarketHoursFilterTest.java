package infrastructure.utils;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.MarketHoursFilter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MarketHoursFilterTest {

    private static final LocalDate TODAY = LocalDate.of(2025, 6, 24);

    @Test
    void filterExactTodayOpeningAndClosing_IncludesOpenAndClose() {
        Instant openInstant = Instant.parse("2025-06-24T13:30:00Z");
        Instant closeInstant = Instant.parse("2025-06-24T20:00:00Z");

        AlphaVantageEvent openEvent = new AlphaVantageEvent("SYM", openInstant, 0,0,0,0,0);
        AlphaVantageEvent closeEvent = new AlphaVantageEvent("SYM", closeInstant,0,0,0,0,0);

        List<AlphaVantageEvent> filtered =
                MarketHoursFilter.filterExactTodayOpeningAndClosing(
                        List.of(openEvent, closeEvent),
                        TODAY
                );

        assertEquals(2, filtered.size(), "Debe incluir los eventos de apertura y cierre");
        assertTrue(filtered.contains(openEvent), "Incluye el evento de apertura");
        assertTrue(filtered.contains(closeEvent), "Incluye el evento de cierre");
    }

    @Test
    void filterExactTodayOpeningAndClosing_ExcludesNonOpenCloseTimes() {
        Instant midInstant = Instant.parse("2025-06-24T14:00:00Z");
        AlphaVantageEvent midEvent = new AlphaVantageEvent("SYM", midInstant,0,0,0,0,0);

        List<AlphaVantageEvent> filtered =
                MarketHoursFilter.filterExactTodayOpeningAndClosing(
                        List.of(midEvent),
                        TODAY
                );

        assertTrue(filtered.isEmpty(), "No debe incluir eventos en horas distintas de apertura/cierre");
    }

    @Test
    void filterExactTodayOpeningAndClosing_ExcludesDifferentDate() {
        Instant yesterdayOpen = Instant.parse("2025-06-23T13:30:00Z");
        AlphaVantageEvent yesterdayEvent = new AlphaVantageEvent("SYM", yesterdayOpen,0,0,0,0,0);

        List<AlphaVantageEvent> filtered =
                MarketHoursFilter.filterExactTodayOpeningAndClosing(
                        List.of(yesterdayEvent),
                        TODAY
                );

        assertTrue(filtered.isEmpty(), "No debe incluir eventos de fecha distinta a la de 'today'");
    }

    @Test
    void filterExactTodayOpeningAndClosing_IncludesOpenWhenSecondsNonZero() {
        Instant fuzzyOpen = Instant.parse("2025-06-24T13:30:45Z");
        AlphaVantageEvent fuzzyEvent = new AlphaVantageEvent("SYM", fuzzyOpen,0,0,0,0,0);

        List<AlphaVantageEvent> filtered =
                MarketHoursFilter.filterExactTodayOpeningAndClosing(
                        List.of(fuzzyEvent),
                        TODAY
                );

        assertEquals(1, filtered.size(), "Debe incluir aunque tenga segundos distintos, truncados al minuto");
        assertEquals(fuzzyEvent, filtered.get(0));
    }
}