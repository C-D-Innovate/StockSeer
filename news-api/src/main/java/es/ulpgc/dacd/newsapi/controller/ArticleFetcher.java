package es.ulpgc.dacd.newsapi.controller;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.PythonScriptRunner;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.NewsApiPort;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.StoragePort;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticleFetcher {
    private static final Logger LOGGER = Logger.getLogger(ArticleFetcher.class.getName());
    private final NewsApiPort newsApi;
    private final StoragePort storage;
    private final String topicName;

    public ArticleFetcher(NewsApiPort newsApi, StoragePort storage, String topicName) {
        this.newsApi   = newsApi;
        this.storage   = storage;
        this.topicName = topicName;
    }

    public CompletableFuture<Void> fetchToday(String query) {
        ZonedDateTime yesterdayUtc     = Instant.now().atZone(ZoneOffset.UTC).minusDays(1);
        LocalDate     date             = yesterdayUtc.toLocalDate();
        Instant       startOfYesterday = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant       endOfYesterday   = date.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        String from = DateTimeFormatter.ISO_INSTANT.format(startOfYesterday);
        String to   = DateTimeFormatter.ISO_INSTANT.format(endOfYesterday);

        return newsApi.fetchArticles(query, from, to)
                .thenAccept(this::processArticles);
    }

    private void processArticles(List<ArticleEvent> articles) {
        if (articles.isEmpty()) {
            System.out.println("😶‍🌫️ Vaya, nada para el rango indicado.");
            return;
        }

        int storedCount = 0;
        Instant ts = Instant.now().atZone(ZoneOffset.UTC).minusDays(1).toInstant();

        for (ArticleEvent brief : articles) {
            String fullContent;
            try {
                fullContent = PythonScriptRunner.extractFullContent(brief.getUrl());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "No pude extraer fullContent de " + brief.getUrl() +
                                ", usando contenido breve", e);
                fullContent = brief.getContent();
            }

            ArticleEvent enriched = new ArticleEvent(
                    topicName,
                    brief.getSs(),
                    ts,
                    brief.getUrl(),
                    brief.getPublishedAt(),
                    brief.getContent(),
                    brief.getTitle(),
                    fullContent
            );

            if (storage.saveArticle(enriched)) {
                storedCount++;
            }
        }

        System.out.println("✅ " + storedCount + " artículos completos almacenados.");
    }
}