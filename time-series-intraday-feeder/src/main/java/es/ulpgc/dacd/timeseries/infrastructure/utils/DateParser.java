package es.ulpgc.dacd.timeseries.infrastructure.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateParser {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static LocalDate parse(String dateStr) {
        try {
            return LocalDate.parse(dateStr, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Formato inválido para la fecha: " + dateStr + ". Debe ser yyyy-MM-dd, por ejemplo: 2025-06-23",
                    e
            );
        }
    }
}
