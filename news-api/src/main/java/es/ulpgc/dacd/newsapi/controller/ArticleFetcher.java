package es.ulpgc.dacd.newsapi.controller;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.ArticleMapper;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.NewsApiPort;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.StoragePort;
import es.ulpgc.dacd.newsapi.infrastructure.utils.DuplicateUrlChecker;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class ArticleFetcher {
    private final NewsApiPort newsApi;
    private final StoragePort storage;
    private final DuplicateUrlChecker urlChecker;

    public ArticleFetcher(NewsApiPort newsApi, StoragePort storage, DuplicateUrlChecker urlChecker) {
        this.newsApi = newsApi;
        this.storage = storage;
        this.urlChecker = urlChecker;
    }

    public void fetchToday(String query) {
        ZonedDateTime yesterdayUtc = Instant.now().atZone(ZoneOffset.UTC).minusDays(1);
        ZonedDateTime startOfYesterday = yesterdayUtc.toLocalDate().atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime endOfYesterday = yesterdayUtc.toLocalDate().atTime(23, 59, 59).atZone(ZoneOffset.UTC);

        String from = DateTimeFormatter.ISO_INSTANT.format(startOfYesterday.toInstant());
        String to = DateTimeFormatter.ISO_INSTANT.format(endOfYesterday.toInstant());

        fetchArticles(query, from, to);
    }


    public void fetchArticles(String query, String from, String to) {
        newsApi.fetchArticles(query, from, to).thenAccept(articles -> {
            if (articles.isEmpty()) {
                System.out.println("No se encontraron artículos para " + from + " a " + to);
                return;
            }

            int storedCount = 0;
            Instant ts = Instant.now().atZone(ZoneOffset.UTC).minusDays(1).toInstant();

            for (ArticleEvent article : articles) {
                if (!urlChecker.isDuplicate(article.getUrl())) {
                    ArticleEvent correctedArticle = new ArticleEvent(
                            "Articles",
                            article.getSs(),
                            ts,
                            article.getUrl(),
                            article.getPublishedAt(),
                            article.getContent(),
                            article.getTitle()
                    );

                    boolean success = storage.saveArticle(correctedArticle);
                    if (success) {
                        urlChecker.markAsSeen(article.getUrl());
                        storedCount++;
                    }
                }
            }

            System.out.println(storedCount + " artículos almacenados para " + from + " a " + to);
        }).exceptionally(ex -> {
            System.err.println("Error al obtener artículos entre " + from + " y " + to + ": " + ex.getMessage());
            return null;
        });
    }
}
