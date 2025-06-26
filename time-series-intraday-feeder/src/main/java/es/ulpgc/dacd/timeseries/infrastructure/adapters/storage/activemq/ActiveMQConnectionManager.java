package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ActiveMQConnectionManager {
    private final Connection connection;
    private final Session session;
    private final Topic topicName;
    private final MessageProducer producer;

    public ActiveMQConnectionManager(String brokerUrl, String topic) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            this.connection = factory.createConnection();
            this.connection.start();

            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.topicName = session.createTopic(topic);
            this.producer = session.createProducer(topicName);

        } catch (JMSException e) {
            throw new RuntimeException("No se pudo inicializar la conexión con ActiveMQ", e);
        }
    }

    public Session session() {
        return session;
    }

    public Topic topic() {
        return topicName;
    }

    public MessageProducer producer() {
        return producer;
    }
}