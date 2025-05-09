package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.NewsApiPort;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NewsApiClientAdapter implements NewsApiPort {
    private static final Logger LOGGER = Logger.getLogger(NewsApiClientAdapter.class.getName());

    private final NewsApiClient newsApiClient;
    private final NewsApiRequestBuilder requestBuilder;
    private final ArticleMapper articleMapper;

    public NewsApiClientAdapter(NewsApiClient newsApiClient, String defaultLanguage, String sourceSystem) {
        this.newsApiClient = newsApiClient;
        this.requestBuilder = new NewsApiRequestBuilder(defaultLanguage);
        this.articleMapper = new ArticleMapper(sourceSystem);
    }

    @Override
    public CompletableFuture<List<ArticleEvent>> fetchArticles(String query, String from, String to) {
        CompletableFuture<List<ArticleEvent>> future = new CompletableFuture<>();
        var request = requestBuilder.build(query, from, to);

        newsApiClient.getEverything(request, new NewsApiClient.ArticlesResponseCallback() {
            @Override
            public void onSuccess(ArticleResponse response) {
                if (response != null && response.getArticles() != null) {
                    List<ArticleEvent> events = response.getArticles().stream()
                            .map(article -> articleMapper.map(article, "Articles", to)) // Ahora usa el tópico correcto
                            .filter(e -> e != null)
                            .collect(Collectors.toList());

                    LOGGER.info("Retrieved " + events.size() + " articles for query: " + query + " from: " + from + " to: " + to);
                    future.complete(events);
                } else {
                    LOGGER.warning("No articles found for query: " + query);
                    future.complete(List.of());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOGGER.log(Level.SEVERE, "Failed to retrieve articles", throwable);
                future.completeExceptionally(throwable);
            }
        });

        return future;
    }


    public static NewsApiClient createApiClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API Key no puede ser nula o vacía");
        }
        return new NewsApiClient(apiKey);
    }
}
