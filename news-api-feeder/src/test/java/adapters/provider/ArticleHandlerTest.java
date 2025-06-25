package adapters.provider;

import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import es.ulpgc.dacd.newsapi.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.ArticleProcessor;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.ArticlesHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArticlesHandlerTest {

    private ArticleProcessor processor;
    private CompletableFuture<List<ArticleEvent>> future;
    private ArticlesHandler handler;

    private static final String QUERY = "test-query";
    private static final String FROM = "2024-06-01";
    private static final String TO = "2024-06-02";

    @BeforeEach
    void setUp() {
        processor = mock(ArticleProcessor.class);
        future = new CompletableFuture<>();
        handler = new ArticlesHandler(QUERY, FROM, TO, processor, future);
    }

    @Test
    void onSuccess_withValidResponse_completesFutureWithProcessedEvents() throws Exception {

        ArticleResponse response = mock(ArticleResponse.class);
        List<ArticleEvent> processedEvents = List.of(mock(ArticleEvent.class), mock(ArticleEvent.class));

        when(response.getArticles()).thenReturn(List.of(mock(com.kwabenaberko.newsapilib.models.Article.class)));
        when(processor.process(response)).thenReturn(processedEvents);

        handler.onSuccess(response);

        assertTrue(future.isDone());
        assertEquals(processedEvents, future.get());

        verify(processor, times(1)).process(response);
    }

    @Test
    void onSuccess_withNullResponse_completesFutureWithEmptyList() throws Exception {

        handler.onSuccess(null);

        assertTrue(future.isDone());
        assertTrue(future.get().isEmpty());

        verifyNoInteractions(processor);
    }

    @Test
    void onSuccess_withNullArticles_completesFutureWithEmptyList() throws Exception {

        ArticleResponse response = mock(ArticleResponse.class);
        when(response.getArticles()).thenReturn(null);

        handler.onSuccess(response);
        assertTrue(future.isDone());
        assertTrue(future.get().isEmpty());

        verifyNoInteractions(processor);
    }
}

