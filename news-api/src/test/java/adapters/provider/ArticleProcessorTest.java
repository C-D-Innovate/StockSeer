package adapters.provider;

import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.domain.service.ArticleEnricher;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.ArticleMapper;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.ArticleProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ArticleProcessorTest {

    private ArticleMapper mapper;
    private ArticleEnricher enricher;
    private ArticleProcessor processor;

    @BeforeEach
    void setUp() {
        mapper = mock(ArticleMapper.class);
        enricher = mock(ArticleEnricher.class);
        processor = new ArticleProcessor(mapper, enricher, "tech");
    }

    @Test
    void should_process_articles_correctly() {
        // Arrange
        Article article1 = new Article();
        article1.setTitle("Artículo 1");

        Article article2 = new Article();
        article2.setTitle("Artículo 2");

        ArticleResponse response = new ArticleResponse();
        response.setArticles(List.of(article1, article2));

        ArticleEvent mapped1 = new ArticleEvent(
                "tech",
                "source1",
                Instant.parse("2024-06-01T00:00:00Z"),
                "url1",                      // url
                Instant.parse("2024-05-31T23:59:00Z"),
                "content1",
                "title1",
                "fullContent1"
        );

        ArticleEvent mapped2 = new ArticleEvent(
                "tech",
                "source2",
                Instant.parse("2024-06-01T01:00:00Z"),
                "url2",
                Instant.parse("2024-05-31T23:59:00Z"),
                "content2",
                "title2",
                "fullContent2"
        );

        when(mapper.map(article1, "tech")).thenReturn(mapped1);
        when(mapper.map(article2, "tech")).thenReturn(mapped2);
        when(enricher.enrich(mapped1)).thenReturn(mapped1);
        when(enricher.enrich(mapped2)).thenReturn(mapped2);

        List<ArticleEvent> result = processor.process(response);

        assertEquals(2, result.size());
        assertEquals("url1", result.get(0).getUrl());
        assertEquals("url2", result.get(1).getUrl());

        verify(mapper, times(1)).map(article1, "tech");
        verify(mapper, times(1)).map(article2, "tech");
        verify(enricher, times(1)).enrich(mapped1);
        verify(enricher, times(1)).enrich(mapped2);
    }

}
