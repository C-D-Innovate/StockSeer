import com.kwabenaberko.newsapilib.NewsApiClient;
import es.ulpgc.dacd.newsapi.controller.ArticleFetcher;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.NewsApiClientAdapter;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.ArticleEventPublisher;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.DatabaseManager;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.JmsConfig;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.NewsApiPort;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.StoragePort;
import es.ulpgc.dacd.newsapi.infrastructure.utils.DuplicateUrlChecker;

import java.time.Instant;
import java.time.LocalDate;
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
        String defaultLanguage = config.get("DEFAULT_LANGUAGE");
        String dbUrl = config.get("DB_URL");
        String brokerUrl = config.get("BROKER_URL");
        String queueName = config.get("QUEUE_NAME");
        String topicName = config.get("TOPIC_NAME");
        String storageTarget = config.get("STORAGE_TARGET").toLowerCase();
        String sourceSystem = config.get("SOURCE_SYSTEM");
        String query = config.get("QUERY");
        String duplicateUrl = config.get("DUPLICATE_URL");

        NewsApiClient client = NewsApiClientAdapter.createApiClient(apiKey);
        NewsApiPort newsApi = new NewsApiClientAdapter(client, defaultLanguage, sourceSystem, topicName);
        JmsConfig jmsConfig = new JmsConfig(brokerUrl, queueName, topicName);
        DuplicateUrlChecker urlChecker = new DuplicateUrlChecker(duplicateUrl);

        StoragePort storage = storageTarget.equals("broker")
                ? new ArticleEventPublisher(jmsConfig)
                : new DatabaseManager(dbUrl);

        ArticleFetcher fetcher = new ArticleFetcher(newsApi, storage, urlChecker);
        fetcher.fetchToday(query);
        Thread.sleep(5000);
        storage.close();
    }
}