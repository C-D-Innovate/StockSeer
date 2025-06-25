package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.domain.service.ArticleEnricher;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.ArticleProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class NewsApiFetcher implements ArticleProvider {
    private static final Logger LOGGER = Logger.getLogger(NewsApiFetcher.class.getName());

    private final NewsApiClient newsApiClient;
    private final NewsApiRequest requestBuilder;
    private final ArticleProcessor articleProcessor;

    public NewsApiFetcher(NewsApiClient client, String lang, String source, String topic) {
        this.newsApiClient = client;
        this.requestBuilder = new NewsApiRequest(lang);
        ArticleMapper mapper = new ArticleMapper(source);
        ArticleEnricher enricher = new ArticleEnricher(topic);
        this.articleProcessor = new ArticleProcessor(mapper, enricher, topic);
    }

    @Override
    public CompletableFuture<List<ArticleEvent>> fetchArticles(String query, String from, String to) {
        LOGGER.info("Iniciando fetch para query: " + query + " desde: " + from + " hasta: " + to);
        CompletableFuture<List<ArticleEvent>> future = new CompletableFuture<>();
        EverythingRequest request = requestBuilder.build(query, from, to);
        newsApiClient.getEverything(
                request,
                new ArticlesHandler(query, from, to, articleProcessor, future)
        );
        return future;
    }

    public static NewsApiClient createApiClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API Key no puede ser nula o vacía");
        }
        return new NewsApiClient(apiKey);
    }
}
