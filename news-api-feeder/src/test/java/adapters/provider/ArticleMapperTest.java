package adapters.provider;

import com.kwabenaberko.newsapilib.models.Article;
import es.ulpgc.dacd.newsapi.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.ArticleMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ArticleMapperTest {

    private final String sourceSystem = "NewsAPI";
    private final String topic = "tech";
    private final ArticleMapper mapper = new ArticleMapper(sourceSystem);

    @Test
    void shouldMapArticleCorrectly() {
        Article article = Mockito.mock(Article.class);
        Mockito.when(article.getUrl()).thenReturn("http://example.com/article");
        Mockito.when(article.getPublishedAt()).thenReturn("2024-06-20T12:00:00Z");
        Mockito.when(article.getContent()).thenReturn("This is the content");
        Mockito.when(article.getTitle()).thenReturn("Sample Title");

        ArticleEvent result = mapper.map(article, topic);

        assertNotNull(result);
        assertEquals("http://example.com/article", result.getUrl());
        assertEquals("Sample Title", result.getTitle());
        assertEquals("This is the content", result.getContent());
        assertEquals(topic, result.getTopic());
        assertEquals(sourceSystem, result.getSs());
        assertEquals(Instant.parse("2024-06-20T12:00:00Z"), result.getPublishedAt());
        assertNotNull(result.getTs());
    }

    @Test
    void shouldReturnNullIfPublishedAtIsInvalid() {
        Article article = Mockito.mock(Article.class);
        Mockito.when(article.getUrl()).thenReturn("http://example.com/bad");
        Mockito.when(article.getPublishedAt()).thenReturn("INVALID_DATE");

        ArticleEvent result = mapper.map(article, topic);

        assertNull(result);
    }
}
