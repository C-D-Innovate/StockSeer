import es.ulpgc.dacd.businessunit.application.HistoricalEventProcessor;
import es.ulpgc.dacd.businessunit.application.RealTimeEventStarter;
import es.ulpgc.dacd.businessunit.controller.EventController;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer.ActiveMQSubscriber;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer.HistoricalEventReader;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis.CalculateLabel;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis.PythonCalculateLabelRunner;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis.PythonExecutor;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datamart.DatamartStorageManager;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datalake.SQLiteManager;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.utils.ArgsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Map;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            log.error("Debes proporcionar la ruta al archivo de configuración.");
            return;
        }

        Map<String, String> config = ArgsParser.parse(args[0]);

        String dbUrl = config.get("SQLITE_DB_URL");
        String eventstorePath = config.get("EVENTSTORE_PATH");
        String brokerUrl = config.get("BROKER_URL");
        String topicsStr = config.get("TOPICS");
        String clientId = config.get("CLIENT_ID");

        if (topicsStr == null || topicsStr.isBlank()) {
            log.error("No se ha configurado la clave TOPICS en el archivo.");
            return;
        }

        PythonExecutor pythonExecutor = new PythonExecutor();
        PythonCalculateLabelRunner labelRunner = new PythonCalculateLabelRunner(pythonExecutor);
        CalculateLabel label = new CalculateLabel(labelRunner);

        try (SQLiteManager storage = new SQLiteManager(dbUrl, label)) {
            DatamartStorageManager datamartStorage = new DatamartStorageManager(dbUrl);
            EventController handler = new EventController(storage);

            HistoricalEventProcessor replayService = new HistoricalEventProcessor(
                    new HistoricalEventReader(), handler, storage
            );
            replayService.replayFromDirectory(Paths.get(eventstorePath));

            String[] topics = topicsStr.split(",");
            for (String topic : topics) {
                topic = topic.trim();
                String subscriptionName = topic.replace(".", "_") + "_subscriber";

                ActiveMQSubscriber subscriber = new ActiveMQSubscriber(
                        brokerUrl, topic, clientId + "_" + topic, subscriptionName, handler
                );

                RealTimeEventStarter realTimeService = new RealTimeEventStarter(subscriber);
                realTimeService.start();
                log.info("Suscripción activa para el tópico: {}", topic);
            }

            datamartStorage.mergeToDatamart();
            datamartStorage.updateAvgSentiment();

            log.info("Suscripciones activas. Esperando eventos en tiempo real...");
            Thread.currentThread().join();

        } catch (Exception e) {
            log.error("Error en la ejecución: {}", e.getMessage(), e);
        }
    }
}