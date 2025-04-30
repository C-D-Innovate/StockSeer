package newsapi.infrastructure.ports.api;

import newsapi.domain.model.Articles;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NewsApiPort {
    CompletableFuture<List<Articles>> fetchArticles(String query);
}
