import consumer.EventStoreConsumer;

public class Main {
    public static void main(String[] args) throws Exception {
        String brokerUrl = args[0];
        String topic = args[1];
        String sourceSystem = args[2];

        EventStoreConsumer consumer = new EventStoreConsumer(brokerUrl, topic, sourceSystem);
        consumer.start();
    }
}
