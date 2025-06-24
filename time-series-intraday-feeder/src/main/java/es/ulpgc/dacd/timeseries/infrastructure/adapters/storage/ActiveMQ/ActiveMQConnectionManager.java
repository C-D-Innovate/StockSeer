package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.ActiveMQ;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ActiveMQConnectionManager {
    private final Connection connection;
    private final Session session;
    private final Topic topic;
    private final MessageProducer producer;

    public ActiveMQConnectionManager(String brokerUrl, String topicName) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            this.connection = factory.createConnection();
            this.connection.start();

            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.topic = session.createTopic(topicName);
            this.producer = session.createProducer(topic);

        } catch (JMSException e) {
            throw new RuntimeException("No se pudo inicializar la conexión con ActiveMQ", e);
        }
    }

    public Session session() {
        return session;
    }

    public Topic topic() {
        return topic;
    }

    public MessageProducer producer() {
        return producer;
    }
}