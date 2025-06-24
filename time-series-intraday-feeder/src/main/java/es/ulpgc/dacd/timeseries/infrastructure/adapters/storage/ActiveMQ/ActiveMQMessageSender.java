package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.ActiveMQ;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.utils.EventJsonSerializer;

import javax.jms.*;
import java.util.List;
import java.util.logging.Logger;

public class ActiveMQMessageSender {
    private static final Logger logger = Logger.getLogger(ActiveMQMessageSender.class.getName());

    private final Session session;
    private final Topic topic;
    private final MessageProducer producer;
    private final EventJsonSerializer serializer;

    public ActiveMQMessageSender(ActiveMQConnectionManager connection, EventJsonSerializer serializer) {
        this.session = connection.session();
        this.topic = connection.topic();
        this.producer = connection.producer();
        this.serializer = serializer;
    }

    public void send(List<AlphaVantageEvent> events) {
        events.forEach(this::sendEvent);
    }

    private void sendEvent(AlphaVantageEvent event) {
        try {
            String json = serializer.serialize(event);
            TextMessage message = session.createTextMessage(json);
            producer.send(topic, message);
            logger.info("Evento publicado: " + json + "\n");
        } catch (JMSException e) {
            logger.severe("Error al publicar evento: " + e.getMessage()+ "\n");
        }
    }
}