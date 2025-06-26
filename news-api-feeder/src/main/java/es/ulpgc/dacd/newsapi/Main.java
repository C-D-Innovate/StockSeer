package es.ulpgc.dacd.newsapi;

import com.kwabenaberko.newsapilib.NewsApiClient;
import es.ulpgc.dacd.newsapi.controller.ArticleController;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.NewsApiFetcher;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.activemq.ArticleEventPublisher;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.sqlite.DatabaseManager;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.utils.ArgsParser;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.ArticleEventFetcher;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.ArticleSaver;

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
            ArticleEventFetcher newsApi = new NewsApiFetcher(
                    client,
                    defaultLanguage,
                    sourceSystem,
                    topicName
            );

            ArticleSaver storage = storageTarget.equals("broker")
                    ? new ArticleEventPublisher(brokerUrl, queueName, topicName)
                    : new DatabaseManager(dbUrl);

            ArticleController fetcher = new ArticleController(newsApi, storage, topicName);

            CompletableFuture<Void> future = fetcher.fetchAndStoreYesterdayArticles(query);
            future.join();

            logger.info("Fetch completado. Cerrando almacenamiento.");
            storage.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error durante la ejecución del programa", e);
        }

        System.exit(0);
    }
}
