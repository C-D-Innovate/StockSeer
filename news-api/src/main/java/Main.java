import com.kwabenaberko.newsapilib.NewsApiClient;
import esulpgcdacdnewsapi.controller.ArticleFetcher;
import esulpgcdacdnewsapi.infrastructure.adapters.provider.NewsApiClientAdapter;
import esulpgcdacdnewsapi.infrastructure.adapters.storage.ArticleEventPublisher;
import esulpgcdacdnewsapi.infrastructure.adapters.storage.DatabaseManager;
import esulpgcdacdnewsapi.infrastructure.ports.provider.NewsApiPort;
import esulpgcdacdnewsapi.infrastructure.ports.storage.StoragePort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        Map<String, String> config = new HashMap<>();
        for (String arg : args) {
            String[] parts = arg.split("=", 2);
            if (parts.length == 2) {
                config.put(parts[0], parts[1]);
            }
        }

        String apiKey = config.get("API_KEY");
        String defaultLanguage = config.getOrDefault("DEFAULT_LANGUAGE", "en");
        String dbUrl = config.get("DB_URL");
        String brokerUrl = config.get("BROKER_URL");
        String queueName = config.get("QUEUE_NAME");
        String topicName = config.get("TOPIC_NAME");
        int fetchIntervalHours = Integer.parseInt(config.get("FETCH_INTERVAL_HOURS"));
        String sourceSystem = config.get("SOURCE_SYSTEM");
        String storageTarget = config.get("STORAGE_TARGET").toLowerCase();

        NewsApiClient client = NewsApiClientAdapter.createApiClient(apiKey);
        NewsApiPort newsApi = new NewsApiClientAdapter(client, defaultLanguage, sourceSystem);

        StoragePort storage = storageTarget.equals("broker")
                ? new ArticleEventPublisher(brokerUrl, queueName, topicName)
                : new DatabaseManager(dbUrl);

        ArticleFetcher fetcher = new ArticleFetcher(newsApi, storage);
        fetcher.fetchHistorical("Articles", 30); // ← genera 30 días hacia atrás

    }
}
