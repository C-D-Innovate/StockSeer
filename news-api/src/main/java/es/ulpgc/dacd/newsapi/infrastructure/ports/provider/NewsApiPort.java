package es.ulpgc.dacd.newsapi.infrastructure.ports.provider;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NewsApiPort {
    CompletableFuture<List<ArticleEvent>> fetchArticles(String query, String from, String to);
}

