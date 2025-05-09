package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEventSerializer;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.StoragePort;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticleEventPublisher implements StoragePort {
    private static final Logger LOGGER = Logger.getLogger(ArticleEventPublisher.class.getName());

    private final JmsPublisher jmsPublisher;
    private final JmsConfig config;

    public ArticleEventPublisher(JmsConfig config) {
        this.config = config;
        this.jmsPublisher = new JmsPublisher(config.brokerUrl);
    }

    @Override
    public boolean saveArticle(ArticleEvent article) {
        try {
            String json = ArticleEventSerializer.toJson(article);
            jmsPublisher.publishToQueue(config.queueName, json);
            jmsPublisher.publishToTopic(config.topicName, json);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publicando artículo", e);
            return false;
        }
    }

    @Override
    public int saveArticles(List<ArticleEvent> articles) {
        return (int) articles.stream().filter(this::saveArticle).count();
    }

    @Override
    public List<ArticleEvent> getAllArticles() {
        throw new UnsupportedOperationException("El publisher no soporta lectura");
    }

    @Override
    public void close() {
        LOGGER.info("Publisher cerrado (sin conexión persistente)");
    }
}
