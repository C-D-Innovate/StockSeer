package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import com.kwabenaberko.newsapilib.NewsApiClient;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticlesHandler implements NewsApiClient.ArticlesResponseCallback {
    private static final Logger LOGGER = Logger.getLogger(ArticlesHandler.class.getName());

    private final String query;
    private final String from;
    private final String to;
    private final ArticleProcessor processor;
    private final CompletableFuture<List<ArticleEvent>> future;

    public ArticlesHandler(String query, String from, String to,
                           ArticleProcessor processor,
                           CompletableFuture<List<ArticleEvent>> future) {
        this.query = query;
        this.from = from;
        this.to = to;
        this.processor = processor;
        this.future = future;
    }

    @Override
    public void onSuccess(ArticleResponse response) {
        if (response != null && response.getArticles() != null) {
            LOGGER.info("Respuesta recibida correctamente. Procesando artículos...");
            List<ArticleEvent> events = processor.process(response);
            LOGGER.info("Se recuperaron " + events.size() + " artículos para query: " + query +
                    " desde: " + from + " hasta: " + to);
            future.complete(events);
        } else {
            LOGGER.warning("No se encontraron artículos para query: " + query);
            future.complete(List.of());
        }
    }

    @Override
    public void onFailure(Throwable throwable) {
        LOGGER.log(Level.SEVERE, "Error al recuperar artículos", throwable);
        future.completeExceptionally(throwable);
    }
}