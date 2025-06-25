package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.ActiveMQ;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.logging.Logger;

public class MessageBrokerSender {
    private static final Logger LOGGER = Logger.getLogger(MessageBrokerSender.class.getName());
    private final ConnectionFactory connectionFactory;

    public MessageBrokerSender(String brokerUrl) {
        this.connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    }

    public void publishToQueue(String queueName, String message) throws JMSException {
        publish(queueName, message, true);
    }

    public void publishToTopic(String topicName, String message) throws JMSException {
        publish(topicName, message, false);
    }

    private void publish(String destinationName, String messageContent, boolean isQueue) throws JMSException {
        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = isQueue ?
                    session.createQueue(destinationName) :
                    session.createTopic(destinationName);
            MessageProducer producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage(messageContent);
            producer.send(message);
            LOGGER.info("Publicado en " + (isQueue ? "cola" : "tópico") + ": " + message.getText());
        }
    }
}
