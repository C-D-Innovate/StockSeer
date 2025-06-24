package es.ulpgc.dacd.timeseries.infrastructure.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimestampParser {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId ZONE_ID = ZoneId.of("America/New_York");


    public Instant parse(String dateTimeString) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, TIMESTAMP_FORMATTER);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZONE_ID);
        return zonedDateTime.toInstant();
    }

    public static LocalTime parseMarketClose(String timeString) {
        try {
            return LocalTime.parse(timeString, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato inválido para MARKET_CLOSE. Usa HH:mm, ej: 13:32", e);
        }
    }
}