package es.ulpgc.dacd.newsapi.domain.service;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.PythonScriptRunner;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticleEnricher {
    private static final Logger LOGGER = Logger.getLogger(ArticleEnricher.class.getName());
    private final String topicName;

    public ArticleEnricher(String topicName) {
        this.topicName = topicName;
    }

    public ArticleEvent enrich(ArticleEvent brief) {
        String fullContent;
        try {
            fullContent = PythonScriptRunner.extractFullContent(brief.getUrl());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "No se pudo extraer fullContent de " + brief.getUrl() +
                            ", usando contenido breve", e);
            fullContent = brief.getContent();
        }

        return new ArticleEvent(
                topicName,
                brief.getSs(),
                Instant.now().atZone(ZoneOffset.UTC).minusDays(1).toInstant(),
                brief.getUrl(),
                brief.getPublishedAt(),
                brief.getContent(),
                brief.getTitle(),
                fullContent
        );
    }
}
