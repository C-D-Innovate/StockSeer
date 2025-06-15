package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class MarketHoursFilter {

    private static final ZoneId MARKET_ZONE = ZoneId.of("America/New_York");
    private static final LocalTime OPEN_TIME = LocalTime.of(9, 30);
    private static final LocalTime CLOSE_TIME = LocalTime.of(16, 0);

    public static List<AlphaVantageEvent> filterExactTodayOpeningAndClosing(List<AlphaVantageEvent> events) {
        LocalDate today = LocalDate.of(2025, 4, 7); // CHAPUZA DE PRUEBA

        return events.stream()
                .filter(event -> {
                    ZonedDateTime eventTime = event.getTs().atZone(MARKET_ZONE);
                    LocalDate date = eventTime.toLocalDate();
                    LocalTime time = eventTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES);

                    return date.equals(today) &&
                            (time.equals(OPEN_TIME) || time.equals(CLOSE_TIME));
                })
                .collect(Collectors.toList());
    }
}
