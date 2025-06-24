package infrastructure.utils;

import es.ulpgc.dacd.timeseries.infrastructure.utils.TimestampParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimestampParserTest {

    private TimestampParser parser;

    @BeforeEach
    void init() {
        parser = new TimestampParser();
    }

    @Test
    void parse_Valido_ConvierteCorrectamenteAZonaNewYork() {
        String input = "2025-06-24 15:30:00";
        Instant esperado = ZonedDateTime.of(
                2025, 6, 24, 15, 30, 0, 0,
                ZoneId.of("America/New_York")
        ).toInstant();

        Instant resultado = parser.parse(input);
        assertEquals(esperado, resultado, "Debe convertir usando la zona America/New_York");
    }

    @Test
    void parse_FormatoIncorrecto_LanzaDateTimeParseException() {
        String mal = "24-06-2025 15:30:00";
        assertThrows(
                DateTimeParseException.class,
                () -> parser.parse(mal),
                "Con formato distinto a yyyy-MM-dd HH:mm:ss debe lanzar DateTimeParseException"
        );
    }

    @Test
    void parseMarketClose_Valido_RetornaLocalTime() {
        assertEquals(LocalTime.of(16, 0), TimestampParser.parseMarketClose("16:00"));
        assertEquals(LocalTime.of(9, 30), TimestampParser.parseMarketClose("09:30"));
    }

    @Test
    void parseMarketClose_Invalido_LanzaIllegalArgumentExceptionConMensaje() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TimestampParser.parseMarketClose("4 PM")
        );
        assertTrue(
                ex.getMessage().contains("Formato inválido para MARKET_CLOSE"),
                "El mensaje debe indicar que el formato es inválido"
        );
        assertTrue(
                ex.getMessage().contains("HH:mm"),
                "El mensaje debe sugerir el patrón HH:mm"
        );
    }
}