package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.StockDataStorage;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class ActivemqPublisher implements StockDataStorage {

    private Connection connection;
    private Session session;
    private Topic topic;
    private MessageProducer producer;
    private final Gson gson;

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
        }
    }

    @Override
    public void saveOpeningAndClosingEvents(List<AlphaVantageEvent> data, String context) {
        List<AlphaVantageEvent> filtered = MarketHoursFilter.filterExactTodayOpeningAndClosing(data);

        if (filtered.isEmpty()) {
            System.out.println("No hay eventos de apertura o cierre para publicar.");
            if (!data.isEmpty()) {
                AlphaVantageEvent latest = data.getFirst();
                System.out.println("Último evento recibido de la API:");
                System.out.println(latest);
            }
            return;
        }

        filtered.forEach(this::publish);
    }

    private void publish(AlphaVantageEvent event) {
        try {
            JsonObject jsonObject = gson.toJsonTree(event).getAsJsonObject();
            jsonObject.addProperty("topic", "AlphaVantageEvent");
            String json = gson.toJson(jsonObject);

            TextMessage message = session.createTextMessage(json);
            producer.send(topic, message);
            System.out.println("→ Publicado al topic: " + json);
        } catch (JMSException e) {
            System.err.println("Error al publicar evento: " + e.getMessage());
        }
    }

    private static class InstantAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            return Instant.parse(in.nextString());
        }
    }
}