package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.models.Article;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticleMapper {
    private static final Logger LOGGER = Logger.getLogger(ArticleMapper.class.getName());
    private final String sourceSystem;

    public ArticleMapper(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public ArticleEvent map(Article article, String topic) {
        try {
            Instant publishedAt = Instant.parse(article.getPublishedAt());
            Instant ts = Instant.now().atZone(ZoneOffset.UTC).minusDays(1).toInstant();

            return new ArticleEvent(
                    topic,
                    sourceSystem,
                    ts,
                    article.getUrl(),
                    publishedAt,
                    article.getContent(),
                    article.getTitle(),
                    null
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error mapeando article: " + article.getUrl(), e);
            return null;
        }
    }
}
