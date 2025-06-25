package es.ulpgc.dacd.newsapi.infrastructure.ports.provider;

import es.ulpgc.dacd.newsapi.model.ArticleEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ArticleEventFetcher {
    CompletableFuture<List<ArticleEvent>> fetchArticles(String query, String from, String to);
}

