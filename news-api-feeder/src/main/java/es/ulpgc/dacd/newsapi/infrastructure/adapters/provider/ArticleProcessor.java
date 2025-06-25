package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.enricher.ArticleEnricher;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ArticleProcessor {
    private static final Logger LOGGER = Logger.getLogger(ArticleProcessor.class.getName());
    private final ArticleMapper mapper;
    private final ArticleEnricher enricher;
    private final String topic;

    public ArticleProcessor(ArticleMapper mapper, ArticleEnricher enricher, String topic) {
        this.mapper = mapper;
        this.enricher = enricher;
        this.topic = topic;
    }

    public List<ArticleEvent> process(ArticleResponse response) {
        LOGGER.info("Procesando artículos recibidos...");
        return response.getArticles().stream()
                .map(article -> mapper.map(article, topic))
                .filter(e -> e != null)
                .map(enricher::enrich)
                .collect(Collectors.toList());
    }
}