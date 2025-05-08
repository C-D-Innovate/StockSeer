package esulpgcdacdnewsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import esulpgcdacdnewsapi.domain.model.ArticleEvent;
import esulpgcdacdnewsapi.infrastructure.ports.provider.NewsApiPort;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NewsApiClientAdapter implements NewsApiPort {
    private static final Logger LOGGER = Logger.getLogger(NewsApiClientAdapter.class.getName());
    private final NewsApiClient newsApiClient;
    private final String defaultLanguage;
    private final String sourceSystem;

    public NewsApiClientAdapter(NewsApiClient newsApiClient, String defaultLanguage, String sourceSystem) {
        this.newsApiClient = newsApiClient;
        this.defaultLanguage = defaultLanguage;
        this.sourceSystem = sourceSystem;
    }

    @Override
    public CompletableFuture<List<ArticleEvent>> fetchArticles(String query) {
        CompletableFuture<List<ArticleEvent>> future = new CompletableFuture<>();

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(30);

        EverythingRequest request = new EverythingRequest.Builder()
                .q(query)
                .language(defaultLanguage)
                .from(fromDate.toString())
                .to(today.toString())
                .build();

        newsApiClient.getEverything(request, new NewsApiClient.ArticlesResponseCallback() {
            @Override
            public void onSuccess(ArticleResponse response) {
                if (response != null && response.getArticles() != null) {
                    List<ArticleEvent> events = response.getArticles().stream()
                            .map(article -> mapToEvent(article, query))
                            .collect(Collectors.toList());
                    LOGGER.info("Retrieved " + events.size() + " articles for query: " + query);
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

    private ArticleEvent mapToEvent(Article article, String topic) {
        try {
            return new ArticleEvent(
                    topic,
                    sourceSystem,
                    Instant.now(), // ts del evento
                    article.getUrl(),
                    Instant.parse(article.getPublishedAt()),
                    article.getContent(),
                    article.getTitle()
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error mapping article: " + article.getUrl(), e);
            return null;
        }
    }

    public static NewsApiClient createApiClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API Key no puede ser nula o vacía");
        }
        return new NewsApiClient(apiKey);
    }
}
