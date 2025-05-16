import es.ulpgc.dacd.businessunit.application.HistoryReplayService;
import es.ulpgc.dacd.businessunit.application.RealTimeSyncService;
import es.ulpgc.dacd.businessunit.controller.EventHandler;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer.HistoricalEventReader;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer.ActiveMQSubscriber;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.ports.out.EventStorage;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.DatamartStorage;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.SqliteEventStorage;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.utils.ArgsParser;

import java.nio.file.Paths;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("❌ Debes proporcionar la ruta al archivo de configuración.");
            return;
        }

        Map<String, String> config = ArgsParser.parse(args[0]);

        String dbUrl = config.get("SQLITE_DB_URL");
        String eventstorePath = config.get("EVENTSTORE_PATH");
        String brokerUrl = config.get("BROKER_URL");
        String topicsStr = config.get("TOPICS");
        String clientId = config.get("CLIENT_ID");

        if (topicsStr == null || topicsStr.isBlank()) {
            System.err.println("❌ No se ha configurado la clave TOPICS en el archivo.");
            return;
        }
        EventStorage storage = new SqliteEventStorage(dbUrl);
        DatamartStorage datamartStorage = new DatamartStorage(dbUrl);
        datamartStorage.resetTempTables();

        EventHandler handler = new EventHandler(storage);

        HistoryReplayService replayService = new HistoryReplayService(
                new HistoricalEventReader(), handler);

        replayService.replayFromDirectory(Paths.get(eventstorePath));

        String[] topics = topicsStr.split(",");
        for (String topic : topics) {
            topic = topic.trim();
            String subscriptionName = topic.replace(".", "_") + "_subscriber";

            ActiveMQSubscriber subscriber = new ActiveMQSubscriber(
                    brokerUrl, topic, clientId + "_" + topic, subscriptionName, handler
            );

            RealTimeSyncService realTimeService = new RealTimeSyncService(subscriber);
            realTimeService.start();
        }

        datamartStorage.mergeToDatamart();

        System.out.println("✅ Procesamiento completado.");
    }
}

