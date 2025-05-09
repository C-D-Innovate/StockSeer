package es.ulpgc.dacd.newsapi.controller;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.ArticleMapper;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.ArticleEventPublisher;
import es.ulpgc.dacd.newsapi.infrastructure.ports.provider.NewsApiPort;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.StoragePort;
import es.ulpgc.dacd.newsapi.infrastructure.utils.DuplicateUrlChecker;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;

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
        ZonedDateTime nowUtc = Instant.now().atZone(ZoneOffset.UTC);
        ZonedDateTime startOfYesterday = nowUtc.minusDays(1).toLocalDate().atStartOfDay(ZoneOffset.UTC);

        String from = startOfYesterday.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String to = nowUtc.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        fetchOnce(query, from, to);
    }

    public void fetchOnce(String query, String from, String to) {
        newsApi.fetchArticles(query, from, to).thenAccept(articles -> {
            if (articles.isEmpty()) {
                System.out.println("No se encontraron artículos para " + from + " a " + to);
                return;
            }

            int storedCount = 0;
            Instant ts = new ArticleMapper("NewsApiFeeder").parseDate(to);


            for (ArticleEvent article : articles) {
                if (!urlChecker.isDuplicate(article.getUrl())) {
                    ArticleEvent correctedArticle = new ArticleEvent(
                            "Articles",
                            article.getSs(),
                            ts, // Ahora usamos `ts` con `parseDate()`
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


    public List<ArticleEvent> fetchHistoricalDay(String query, LocalDate date) throws Exception {
        String from = date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return newsApi.fetchArticles(query, from, to).get();
    }
}
