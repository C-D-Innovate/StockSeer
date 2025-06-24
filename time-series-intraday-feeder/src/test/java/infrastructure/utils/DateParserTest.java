package infrastructure.utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import es.ulpgc.dacd.timeseries.infrastructure.utils.DateParser;


class DateParserTest {

    @Test
    void parse_FechaValida_ReturnsLocalDate() {
        String fecha = "2025-06-23";
        LocalDate resultado = DateParser.parse(fecha);
        assertNotNull(resultado);
        assertEquals(2025, resultado.getYear());
        assertEquals(6,   resultado.getMonthValue());
        assertEquals(23,  resultado.getDayOfMonth());
    }

    @Test
    void parse_FormatoMal_FormatoYYYYMMDD_ThrowsIllegalArgumentException() {
        String fechaMal = "23/06/2025";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> DateParser.parse(fechaMal)
        );
        assertTrue(
                ex.getMessage().contains("Formato inválido para la fecha: " + fechaMal),
                "El mensaje debe mencionar la fecha malformada"
        );
    }

    @Test
    void parse_FechaInexistente_Mes13_ThrowsIllegalArgumentException() {
        String fechaInvalida = "2025-13-01";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> DateParser.parse(fechaInvalida)
        );
        assertTrue(
                ex.getMessage().contains("Formato inválido para la fecha: " + fechaInvalida),
                "Debería tratar mes 13 como formato inválido"
        );
    }

    @Test
    void parse_Null_ThrowsNullPointerException() {
        String nula = null;
        assertThrows(
                NullPointerException.class,
                () -> DateParser.parse(nula),
                "Pasar null debe desencadenar NullPointerException"
        );
    }
}
