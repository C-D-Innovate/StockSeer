package es.ulpgc.dacd.newsapi;

import com.kwabenaberko.newsapilib.NewsApiClient;
import es.ulpgc.dacd.newsapi.controller.ArticleFetchController;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.NewsApiFetcher;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.ActiveMQ.ArticleEventPublisher;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.SQLite.DatabaseManager;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.ActiveMQ.JmsConfig;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.utils.ArgsParser;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.NewsApiPort;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.StoragePort;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.severe("Debes proporcionar la ruta al archivo de configuración.");
            return;
        }

        try {
            Map<String, String> config = ArgsParser.parse(args[0]);

            String apiKey          = config.get("API_KEY");
            String defaultLanguage = config.get("DEFAULT_LANGUAGE");
            String dbUrl           = config.get("DB_URL");
            String brokerUrl       = config.get("BROKER_URL");
            String queueName       = config.get("QUEUE_NAME");
            String topicName       = config.get("TOPIC_NAME");
            String storageTarget   = config.get("STORAGE_TARGET").toLowerCase();
            String sourceSystem    = config.get("SOURCE_SYSTEM");
            String query           = config.get("QUERY");

            logger.info("Iniciando fetch de noticias para el tópico: " + topicName);

            NewsApiClient client = NewsApiFetcher.createApiClient(apiKey);
            NewsApiPort newsApi = new NewsApiFetcher(
                    client,
                    defaultLanguage,
                    sourceSystem,
                    topicName
            );

            StoragePort storage = storageTarget.equals("broker")
                    ? new ArticleEventPublisher(new JmsConfig(brokerUrl, queueName, topicName))
                    : new DatabaseManager(dbUrl);

            ArticleFetchController fetcher = new ArticleFetchController(newsApi, storage, topicName);

            CompletableFuture<Void> future = fetcher.fetchToday(query);
            future.join();

            logger.info("Fetch completado. Cerrando almacenamiento.");
            storage.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error durante la ejecución del programa", e);
        }

        System.exit(0);
    }
}
