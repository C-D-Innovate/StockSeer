package es.ulpgc.dacd.eventstorebuilder;

import es.ulpgc.dacd.eventstorebuilder.controller.EventHandler;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.consumer.ActiveMQSubscriber;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.storage.FileSystemStorage;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.util.ArgsParser;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.port.EventStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("Debes proporcionar la ruta al archivo de configuración.");
            return;
        }

        Map<String, String> config = ArgsParser.parse(args[0]);

        String brokerUrl = config.get("BROKER_URL");
        String clientId = config.get("CLIENT_ID");
        String[] topics = config.get("TOPICS").split(",");

        for (String topic : topics) {
            EventStorage storage = new FileSystemStorage();
            EventHandler handler = new EventHandler(storage);

            String subscriptionName = topic.replace(".", "_") + "_subscriber";

            ActiveMQSubscriber subscriber = new ActiveMQSubscriber(
                    brokerUrl, topic, clientId + "_" + topic, subscriptionName, handler
            );

            logger.info("Iniciando suscriptor para el tópico '{}'", topic);
            subscriber.start();
        }
    }
}
