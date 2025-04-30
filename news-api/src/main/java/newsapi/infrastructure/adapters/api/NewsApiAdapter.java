package newsapi.infrastructure.adapters.api;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import newsapi.domain.model.Articles;
import newsapi.infrastructure.ports.api.NewsApiPort;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class NewsApiAdapter implements NewsApiPort {
    private static final Logger LOGGER = Logger.getLogger(NewsApiAdapter.class.getName());
    private final NewsApiClient newsApiClient;
    private final String defaultLanguage;

    public NewsApiAdapter(NewsApiClient newsApiClient, String defaultLanguage) {
        this.newsApiClient = newsApiClient;
        this.defaultLanguage = defaultLanguage;
    }

    @Override
    public CompletableFuture<List<Articles>> fetchArticles(String query) {
        return getArticlesAsync(query);
    }

    public CompletableFuture<List<Articles>> getArticlesAsync(String query) {
        CompletableFuture<List<Articles>> future = new CompletableFuture<>();
        String searchQuery = (query == null || query.trim().isEmpty()) ? "news" : query;

        EverythingRequest request = new EverythingRequest.Builder()
                .q(searchQuery)
                .language(defaultLanguage)
                .build();

        newsApiClient.getEverything(request, new NewsApiCallbackHandler(future, searchQuery, LOGGER));

        return future;
    }
}
