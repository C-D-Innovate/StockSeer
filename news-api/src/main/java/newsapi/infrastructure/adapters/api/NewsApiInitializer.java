package newsapi.infrastructure.adapters.api;

import com.kwabenaberko.newsapilib.NewsApiClient;

public class NewsApiInitializer {
    public static NewsApiClient createApiClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API Key no puede ser nula o vacía");
        }
        return new NewsApiClient(apiKey);
    }
}