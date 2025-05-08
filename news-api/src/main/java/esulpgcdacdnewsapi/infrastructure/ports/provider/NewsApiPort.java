package esulpgcdacdnewsapi.infrastructure.ports.provider;

import esulpgcdacdnewsapi.domain.model.ArticleEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NewsApiPort {
    CompletableFuture<List<ArticleEvent>> fetchArticles(String query);
}

