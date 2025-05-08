package consumer;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class EventStoreConsumer {

    private final String brokerUrl;
    private final String topicName;
    private final String sourceSystem;
    private final EventFileWriter fileWriter;

    public EventStoreConsumer(String brokerUrl, String topicName, String sourceSystem) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.sourceSystem = sourceSystem;
        this.fileWriter = new EventFileWriter();
    }

    public void start() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.setClientID("Daniel");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createTopic(topicName);
        MessageConsumer consumer = session.createConsumer(destination);

        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage) {
                    String json = ((TextMessage) message).getText();

                    // Extraer la fecha del campo "ts"
                    String extractedDate = extractDateFromJson(json);

                    fileWriter.writeEvent(topicName, sourceSystem, extractedDate, json);
                    System.out.println("Evento recibido y almacenado para " + extractedDate);
                }
            } catch (Exception e) {
                System.err.println("Error procesando el mensaje: " + e.getMessage());
            }
        });

        System.out.println("EventStoreConsumer escuchando en " + topicName);
    }

    private String extractDateFromJson(String json) {
        try {
            int tsIndex = json.indexOf("\"ts\":\"");
            if (tsIndex == -1) return "unknown";

            int start = tsIndex + 6;
            int end = json.indexOf('"', start);
            String ts = json.substring(start, end);

            Instant instant = Instant.parse(ts);
            return DateTimeFormatter.ofPattern("yyyyMMdd")
                    .withZone(ZoneOffset.UTC)
                    .format(instant);
        } catch (Exception e) {
            System.err.println("Error extrayendo ts: " + e.getMessage());
            return "unknown";
        }
    }
}
