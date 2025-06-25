package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.ActiveMQ;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.utils.ArticleEventSerializer;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.ArticleRepository;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticleEventPublisher implements ArticleRepository {
    private static final Logger LOGGER = Logger.getLogger(ArticleEventPublisher.class.getName());

    private final MessageBrokerSender jmsPublisher;
    private final String queueName;
    private final String topicName;

    public ArticleEventPublisher(String brokerUrl, String queueName, String topicName) {
        this.queueName = queueName;
        this.topicName = topicName;
        this.jmsPublisher = new MessageBrokerSender(brokerUrl);
    }

    @Override
    public boolean saveArticle(ArticleEvent article) {
        try {
            String json = ArticleEventSerializer.toJson(article);
            jmsPublisher.publishToQueue(queueName, json);
            jmsPublisher.publishToTopic(topicName, json);
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
    public void close() {
        LOGGER.info("Publisher cerrado (sin conexión persistente)");
    }
}
