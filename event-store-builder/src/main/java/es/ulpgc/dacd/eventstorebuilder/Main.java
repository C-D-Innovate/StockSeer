package es.ulpgc.dacd.eventstorebuilder;

import es.ulpgc.dacd.eventstorebuilder.controller.EventHandler;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.consumer.ActiveMQSubscriber;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.storage.FileSystemStorage;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters.util.ArgsParser;
import es.ulpgc.dacd.eventstorebuilder.infrastructure.port.EventStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("❌ Debes proporcionar la ruta al archivo de configuración.");
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

            subscriber.start();
        }
    }
}
