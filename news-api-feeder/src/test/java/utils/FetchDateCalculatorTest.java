package utils;

import static org.junit.jupiter.api.Assertions.*;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.utils.FetchDateCalculator;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

class FetchDateCalculatorTest {

    @Test
    void yesterdayUtcRange_shouldReturnCorrectUtcRangeForYesterday() {
        // Act
        FetchDateCalculator.DateRange range = FetchDateCalculator.yesterdayUtcRange();
        Instant fromInstant = Instant.parse(range.from());
        Instant toInstant = Instant.parse(range.to());
        LocalDate yesterdayDate = Instant.now().atZone(ZoneOffset.UTC).minusDays(1).toLocalDate();
        Instant expectedFrom = yesterdayDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant expectedTo = yesterdayDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();
        assertEquals(expectedFrom, fromInstant, "From instant should be start of yesterday UTC");
        assertEquals(expectedTo, toInstant, "To instant should be end of yesterday UTC");
    }
}

