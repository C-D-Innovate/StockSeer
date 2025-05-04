package newsapi.controller;

import newsapi.domain.model.Articles;
import newsapi.infrastructure.ports.api.NewsApiPort;
import newsapi.infrastructure.ports.storage.StoragePort;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ArticleFetcher {
    private final ScheduledExecutorService scheduler;
    private final NewsApiPort apiPort;
    private final StoragePort dbPort;
    private final String query;

    public ArticleFetcher(NewsApiPort apiPort, StoragePort dbPort, String query) {
        this.apiPort = apiPort;
        this.dbPort = dbPort;
        this.query = query;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void startFetchingEvery(int fetchIntervalHours) {
        scheduler.scheduleAtFixedRate(this::fetchAndSaveArticles, 0, fetchIntervalHours, TimeUnit.HOURS);
    }

    private void fetchAndSaveArticles() {
        try {
            System.out.println("Fetching articles with query: " + query);
            apiPort.fetchArticles(query)
                    .thenAccept(this::handleFetchedArticles)
                    .exceptionally(this::handleError);
        } catch (Exception e) {
            System.err.println("Fetch error: " + e.getMessage());
        }
    }

    private void handleFetchedArticles(List<Articles> articles) {
        int savedCount = saveArticles(articles);
        System.out.println("Saved " + savedCount + " new articles.");
    }

    private Void handleError(Throwable ex) {
        System.err.println("Error: " + ex.getMessage());
        return null;
    }

    private int saveArticles(List<Articles> articles) {
        if (articles == null || articles.isEmpty()) {
            System.err.println("No articles to store");
            return 0;
        }
        return dbPort.saveArticles(articles) ? articles.size() : 0;
    }

    public void stop() {
        scheduler.shutdown();
        dbPort.close();
    }
}
