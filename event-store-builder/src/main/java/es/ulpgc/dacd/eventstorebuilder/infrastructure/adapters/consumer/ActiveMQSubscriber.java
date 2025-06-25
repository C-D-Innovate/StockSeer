package es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.consumer;

import es.ulpgc.dacd.eventstorebuilder.controller.EventHandler;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class ActiveMQSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(ActiveMQSubscriber.class);

    private final String brokerUrl;
    private final String topicName;
    private final String clientId;
    private final String subscriptionName;
    private final EventHandler handler;

    public ActiveMQSubscriber(String brokerUrl, String topicName, String clientId, String subscriptionName, EventHandler handler) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.clientId = clientId;
        this.subscriptionName = subscriptionName;
        this.handler = handler;
    }

    public void start() {
        try {
            Connection connection = createConnection();
            Session session = createSession(connection);
            MessageConsumer consumer = createDurableSubscriber(session);

            setupListener(consumer);
            logger.info("Suscripción a ActiveMQ iniciada: topic={}, clientId={}, subscription={}", topicName, clientId, subscriptionName);

        } catch (JMSException e) {
            logger.error("Error inicializando suscripción a ActiveMQ: {}", e.getMessage(), e);
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.setClientID(clientId);
        connection.start();
        return connection;
    }

    private Session createSession(Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private MessageConsumer createDurableSubscriber(Session session) throws JMSException {
        Topic topic = session.createTopic(topicName);
        return session.createDurableSubscriber(topic, subscriptionName);
    }

    private void setupListener(MessageConsumer consumer) throws JMSException {
        consumer.setMessageListener(message -> {
            if (message instanceof TextMessage textMessage) {
                try {
                    String json = textMessage.getText();
                    handler.handle(topicName, json);
                } catch (JMSException e) {
                    logger.error("Error al leer mensaje JMS: {}", e.getMessage(), e);
                }
            } else {
                logger.warn("Mensaje recibido no es de tipo TextMessage");
            }
        });
    }
}
