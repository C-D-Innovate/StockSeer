package es.ulpgc.dacd.newsapi.controller;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.domain.service.ArticleEnricher;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.utils.FetchDateCalculator;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.NewsApiPort;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.StoragePort;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ArticleFetchController {
    private static final Logger LOGGER = Logger.getLogger(ArticleFetchController.class.getName());
    private final NewsApiPort newsApi;
    private final StoragePort storage;
    private final ArticleEnricher enricher;

    public ArticleFetchController(NewsApiPort newsApi, StoragePort storage, String topicName) {
        this.newsApi = newsApi;
        this.storage = storage;
        this.enricher = new ArticleEnricher(topicName);
    }

    public CompletableFuture<Void> fetchAndStoreYesterdayArticles(String query) {
        var range = FetchDateCalculator.yesterdayUtcRange();
        return newsApi.fetchArticles(query, range.from(), range.to())
                .thenAccept(this::processAndStoreArticles);
    }

    private void processAndStoreArticles(List<ArticleEvent> articles) {
        if (articles.isEmpty()) {
            LOGGER.info("Nada para el rango indicado.");
            return;
        }

        int storedCount = 0;
        for (ArticleEvent brief : articles) {
            ArticleEvent enriched = enricher.enrich(brief);
            if (storage.saveArticle(enriched)) {
                storedCount++;
            }
        }

        LOGGER.info("Proceso finalizado. " + storedCount + " artículos completos almacenados.");
    }
}
