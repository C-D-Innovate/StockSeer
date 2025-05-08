package esulpgcdacdnewsapi.infrastructure.adapters.storage;

import jakarta.jms.*;
import esulpgcdacdnewsapi.domain.model.ArticleEvent;
import esulpgcdacdnewsapi.infrastructure.ports.storage.StoragePort;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.List;

public class ArticleEventPublisher implements StoragePort {
    private final String brokerUrl;
    private final String queueName;
    private final String topicName;


    public ArticleEventPublisher(String brokerUrl, String queueName, String topicName) {
        this.brokerUrl = brokerUrl;
        this.queueName = queueName;
        this.topicName = topicName;
    }

    // Método para publicar en la cola
    private void publishToQueue(String messageContent) throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        try (Connection connection = factory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage(messageContent);
            producer.send(message);
            System.out.println("Publicado en cola: " + message.getText());
        }
    }

    // Método para publicar en el tópico
    private void publishToTopic(String messageContent) throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        try (Connection connection = factory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage(messageContent);
            producer.send(message);
            System.out.println("Publicado en tópico: " + message.getText());
        }
    }

    @Override
    public boolean saveArticle(ArticleEvent article) {
        try {
            String articleJson = article.toJson();
            publishToQueue(articleJson);
            publishToTopic(articleJson);
            return true;
        } catch (JMSException e) {
            System.err.println("Error al publicar artículo: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int saveArticles(List<ArticleEvent> articles) {
        int successCount = 0;
        for (ArticleEvent article : articles) {
            if (saveArticle(article)) successCount++;
        }
        return successCount;
    }


    @Override
    public List<ArticleEvent> getAllArticles() {
        throw new UnsupportedOperationException("El publisher no soporta lectura de artículos");
    }

    @Override
    public void close() {
        System.out.println("Publisher cerrado (no hay conexión persistente que cerrar)");
    }
}
