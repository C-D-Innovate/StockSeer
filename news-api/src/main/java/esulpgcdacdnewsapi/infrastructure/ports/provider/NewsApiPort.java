package esulpgcdacdnewsapi.infrastructure.ports.provider;

import esulpgcdacdnewsapi.domain.model.ArticleEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NewsApiPort {
    // Consulta completa (últimos 30 días)
    CompletableFuture<List<ArticleEvent>> fetchArticles(String query);

    // Consulta acotada entre fechas (formato ISO: yyyy-MM-dd)
    CompletableFuture<List<ArticleEvent>> fetchArticles(String query, String from, String to);
}
