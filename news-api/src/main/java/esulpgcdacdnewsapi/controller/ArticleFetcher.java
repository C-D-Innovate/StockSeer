package esulpgcdacdnewsapi.controller;

import esulpgcdacdnewsapi.infrastructure.ports.provider.NewsApiPort;
import esulpgcdacdnewsapi.infrastructure.ports.storage.StoragePort;
import esulpgcdacdnewsapi.domain.model.ArticleEvent;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

public class ArticleFetcher {
    private final NewsApiPort newsApi;
    private final StoragePort storage;

    public ArticleFetcher(NewsApiPort newsApi, StoragePort storage) {
        this.newsApi = newsApi;
        this.storage = storage;
    }

    public void startFetching(String query, Duration interval) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> fetchAndStore(query);
        scheduler.scheduleAtFixedRate(task, 0, interval.toHours(), TimeUnit.HOURS);
    }

    private void fetchAndStore(String query) {
        newsApi.fetchArticles(query).thenAccept(articles -> {
            if (articles.isEmpty()) {
                System.out.println("No se encontraron artículos nuevos.");
                return;
            }

            int storedCount = storage.saveArticles(articles);

            if (storedCount > 0) {
                System.out.println(storedCount + " artículos nuevos almacenados correctamente.");
            } else {
                System.out.println("No se almacenaron artículos nuevos.");
            }
        }).exceptionally(ex -> {
            System.err.println("Error al obtener artículos: " + ex.getMessage());
            return null;
        });
    }

    public void fetchHistorical(String query, int daysBack) {
        for (int i = 0; i < daysBack; i++) {
            LocalDate date = LocalDate.now().minusDays(i + 1);
            String day = date.toString(); // formato YYYY-MM-DD
            fetchOnce(query, day, day);
        }
    }

    public void fetchOnce(String query, String from, String to) {
        newsApi.fetchArticles(query, from, to).thenAccept(articles -> {
            if (articles.isEmpty()) {
                System.out.println("No se encontraron artículos para " + from);
                return;
            }

            int storedCount = storage.saveArticles(articles);

            if (storedCount > 0) {
                System.out.println(storedCount + " artículos almacenados para " + from);
            } else {
                System.out.println("No se almacenaron artículos para " + from);
            }
        }).exceptionally(ex -> {
            System.err.println("Error al obtener artículos de " + from + ": " + ex.getMessage());
            return null;
        });
    }



}