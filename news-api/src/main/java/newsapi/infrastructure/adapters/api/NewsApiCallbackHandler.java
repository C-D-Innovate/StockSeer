package newsapi.infrastructure.adapters.api;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import newsapi.domain.model.Articles;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NewsApiCallbackHandler implements NewsApiClient.ArticlesResponseCallback {
    private final CompletableFuture<List<Articles>> future;
    private final String searchQuery;
    private final Logger logger;

    public NewsApiCallbackHandler(CompletableFuture<List<Articles>> future, String searchQuery, Logger logger) {
        this.future = future;
        this.searchQuery = searchQuery;
        this.logger = logger;
    }

    public void onSuccess(ArticleResponse response) {
        if (response != null && response.getArticles() != null) {
            List<Articles> articles = response.getArticles().stream()
                    .map(article -> mapArticleToArticlesData(article))
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved " + articles.size() + " articles");
            future.complete(articles);}
        else {
            logger.info("No articles found or empty response");
            future.complete(List.of());
        }
    }

    public void onFailure(Throwable throwable) {
        logger.log(Level.SEVERE, "Failed to retrieve articles", throwable);
        future.completeExceptionally(throwable);
    }

    private Articles mapArticleToArticlesData(Article article) {
        if (article == null) {
            logger.warning("Attempted to map null article");
            return null;
        }

        String url = article.getUrl();
        String content = article.getContent();
        String title = article.getTitle();
        Instant publishedAt = Instant.parse(article.getPublishedAt());

        return new Articles(url, publishedAt, content, title);
    }
}
