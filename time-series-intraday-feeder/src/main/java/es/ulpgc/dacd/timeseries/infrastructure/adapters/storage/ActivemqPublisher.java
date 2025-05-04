package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.serialization.InstantAdapter;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.StockDataStorage;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.time.Instant;
import java.util.List;

public class ActivemqPublisher implements StockDataStorage {

    private Connection connection;
    private Session session;
    private Topic topic;
    private MessageProducer producer;
    private final Gson gson;
    private AlphaVantageEvent lastPublished;

    public ActivemqPublisher(String brokerUrl, String topicName) {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
        initializeConnection(brokerUrl, topicName);
    }

    private void initializeConnection(String brokerUrl, String topicName) {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            this.connection = connectionFactory.createConnection();
            this.connection.start();

            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.topic = session.createTopic(topicName);
            this.producer = session.createProducer(topic);

        } catch (JMSException e) {
            System.err.println("Error al inicializar la conexión con ActiveMQ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(List<AlphaVantageEvent> data, String dbUrl) {
        data.forEach(this::publish);
    }

    @Override
    public boolean isDuplicate(AlphaVantageEvent data, String dbUrl) {
        return lastPublished != null &&
                lastPublished.getSymbol().equals(data.getSymbol()) &&
                lastPublished.getTimestamp().equals(data.getTimestamp());
    }

    private void publish(AlphaVantageEvent event) {
        try {
            if (!isDuplicate(event, "")) {
                String json = gson.toJson(event);
                TextMessage message = session.createTextMessage(json);
                producer.send(topic, message);
                lastPublished = event;
                System.out.println("→ Publicado al topic: " + json);
            }
        } catch (JMSException e) {
            System.err.println("Error al publicar evento: " + e.getMessage());
        }
    }
}