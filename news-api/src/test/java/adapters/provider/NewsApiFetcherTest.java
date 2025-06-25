package adapters.provider;

import com.kwabenaberko.newsapilib.NewsApiClient;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.NewsApiFetcher;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NewsApiFetcherTest {
    @Test
    void createApiClient_withValidKey_returnsClient() {
        String validKey = "validApiKey123";
        NewsApiClient client = NewsApiFetcher.createApiClient(validKey);
        assertNotNull(client);
    }

    @Test
    void createApiClient_withNullOrBlankKey_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> NewsApiFetcher.createApiClient(null));
        assertThrows(IllegalArgumentException.class, () -> NewsApiFetcher.createApiClient(""));
        assertThrows(IllegalArgumentException.class, () -> NewsApiFetcher.createApiClient("   "));
    }
}
