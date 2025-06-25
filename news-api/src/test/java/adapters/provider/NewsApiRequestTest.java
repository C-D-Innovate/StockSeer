package adapters.provider;

import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.NewsApiRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NewsApiRequestTest {

    private static final String LANGUAGE = "en";

    @Test
    void build_withValidDates_addsZAndParsesCorrectly() {
        NewsApiRequest requestBuilder = new NewsApiRequest(LANGUAGE);

        String query = "bitcoin";
        String from = "2024-06-01T12:00:00";
        String to = "2024-06-02T15:30:00Z";

        EverythingRequest request = requestBuilder.build(query, from, to);

        assertEquals(query, request.getQ());
        assertEquals(LANGUAGE, request.getLanguage());

        assertEquals("2024-06-01T12:00:00Z", request.getFrom());
        assertEquals("2024-06-02T15:30:00Z", request.getTo());
    }

    @Test
    void build_withInvalidDate_throwsIllegalArgumentException() {
        NewsApiRequest requestBuilder = new NewsApiRequest(LANGUAGE);

        String invalidDate = "2024-06-31T12:00:00";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> requestBuilder.build("test", invalidDate, "2024-06-02T00:00:00Z"));

        assertTrue(thrown.getMessage().contains("Formato de fecha inválido"));
    }

    @Test
    void validateDate_addsZIfMissing() {
        NewsApiRequest requestBuilder = new NewsApiRequest(LANGUAGE);
        EverythingRequest req = requestBuilder.build("q", "2024-06-01T00:00:00", "2024-06-01T23:59:59");

        assertEquals("2024-06-01T00:00:00Z", req.getFrom());
        assertEquals("2024-06-01T23:59:59Z", req.getTo());
    }
}

