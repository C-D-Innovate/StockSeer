package es.ulpgc.dacd.newsapi.controller;

import es.ulpgc.dacd.newsapi.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.enricher.ArticleEnricher;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.utils.FetchDateCalculator;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.ArticleEventFetcher;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.ArticleSaver;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ArticleController {
    private static final Logger LOGGER = Logger.getLogger(ArticleController.class.getName());
    private final ArticleEventFetcher newsApi;
    private final ArticleSaver storage;
    private final ArticleEnricher enricher;

    public ArticleController(ArticleEventFetcher newsApi, ArticleSaver storage, String topicName) {
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
