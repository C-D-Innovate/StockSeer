package es.ulpgc.dacd.timeseries.infrastructure.utils;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class MarketHoursFilter {

    private static final ZoneId MARKET_ZONE = ZoneId.of("America/New_York");
    private static final LocalTime OPEN_TIME = LocalTime.of(9, 30);
    private static final LocalTime CLOSE_TIME = LocalTime.of(16, 0);

    public static List<AlphaVantageEvent> filterExactTodayOpeningAndClosing(List<AlphaVantageEvent> events, LocalDate today) {
        return events.stream()
                .filter(event -> isEventAtExactOpenOrCloseToday(event, today))
                .collect(Collectors.toList());
    }

    private static boolean isEventAtExactOpenOrCloseToday(AlphaVantageEvent event, LocalDate today) {
        ZonedDateTime eventTime = event.getTs().atZone(MARKET_ZONE);
        LocalDate eventDate = eventTime.toLocalDate();
        LocalTime eventTimeRounded = eventTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES);

        return eventDate.equals(today) &&
                (eventTimeRounded.equals(OPEN_TIME) || eventTimeRounded.equals(CLOSE_TIME));
    }
}