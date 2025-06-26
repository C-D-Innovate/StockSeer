import es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer.HistoricalEventProcessor;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer.RealTimeEventStarter;
import es.ulpgc.dacd.businessunit.controller.EventController;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer.ActiveMQSubscriber;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.consumer.HistoricalEventReader;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis.Classifier;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis.PythonCalculateLabelRunner;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis.PythonExecutor;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datamart.DatamartManager;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datalake.SQLiteManager;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.utils.ArgsParser;
import es.ulpgc.dacd.businessunit.infrastructure.ml.DashboardLauncher;
import es.ulpgc.dacd.businessunit.infrastructure.ml.PythonTrainerLauncher;
import es.ulpgc.dacd.businessunit.infrastructure.ml.TrainingPipeline;
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
        String cleanDatamartPath = config.get("CLEAN_DATAMART_PATH");

        String trainScriptPath = config.get("TRAIN_SCRIPT_PATH");
        String dashboardScriptPath = config.get("DASHBOARD_SCRIPT_PATH");
        String dashboardCsvPath = config.get("DASHBOARD_CSV_PATH");
        String dashboardModelPath = config.get("DASHBOARD_MODEL_PATH");
        String dbPathForPython = dbUrl.replace("jdbc:sqlite:", "");

        int trainingInterval = Integer.parseInt(config.getOrDefault("TRAINING_INTERVAL_MINUTES", "5"));

        if (topicsStr == null || topicsStr.isBlank()) {
            log.error("No se ha configurado la clave TOPICS en el archivo.");
            return;
        }

        PythonExecutor pythonExecutor = new PythonExecutor();
        PythonCalculateLabelRunner labelRunner = new PythonCalculateLabelRunner(pythonExecutor);
        Classifier label = new Classifier(labelRunner);

        try (SQLiteManager storage = new SQLiteManager(dbUrl, label)) {
            DatamartManager datamartStorage = new DatamartManager(dbUrl);
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

            PythonTrainerLauncher trainer = new PythonTrainerLauncher(
                    null, trainScriptPath, dbPathForPython, dashboardCsvPath, dashboardModelPath
            );
            DashboardLauncher dashboard = new DashboardLauncher(
                    null, dashboardScriptPath, dashboardCsvPath, dashboardModelPath
            );
            TrainingPipeline pipeline = new TrainingPipeline(trainer, dashboard, cleanDatamartPath);

            Thread trainingThread = new Thread(() -> {
                while (true) {
                    try {
                        log.info("Iniciando pipeline de entrenamiento...");
                        pipeline.run();

                        log.info("Esperando {} minutos para el siguiente entrenamiento...", trainingInterval);
                        Thread.sleep(trainingInterval * 60L * 1000L);
                    } catch (Exception e) {
                        log.error("Error en la ejecución del pipeline de entrenamiento: {}", e.getMessage(), e);
                    }
                }
            });

            trainingThread.setDaemon(true);
            trainingThread.start();

            log.info("Sistema activo. Esperando eventos en tiempo real...");
            Thread.currentThread().join();

        } catch (Exception e) {
            log.error("Error en la ejecución: {}", e.getMessage(), e);
        }
    }
}
