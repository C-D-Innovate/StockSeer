package es.ulpgc.dacd.newsapi.infrastructure.adapters.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FetchDateCalculator {
    public record DateRange(String from, String to) {}

    public static DateRange yesterdayUtcRange() {
        ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC).minusDays(1);
        LocalDate date = now.toLocalDate();
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        String from = DateTimeFormatter.ISO_INSTANT.format(start);
        String to = DateTimeFormatter.ISO_INSTANT.format(end);

        return new DateRange(from, to);
    }
}

