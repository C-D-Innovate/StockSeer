package es.ulpgc.dacd.businessunit.infrastructure.traindeploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;

public class TrainingPipeline {
    private static final Logger logger = LoggerFactory.getLogger(TrainingPipeline.class);
    private final es.ulpgc.dacd.businessunit.infrastructure.traindeploy.PythonTrainerLauncher trainer;
    private final es.ulpgc.dacd.businessunit.infrastructure.traindeploy.DashboardLauncher dashboard;
    private final String cleanDatamartPath;
    private Instant lastRun = Instant.MIN;

    public TrainingPipeline(es.ulpgc.dacd.businessunit.infrastructure.traindeploy.PythonTrainerLauncher trainer, es.ulpgc.dacd.businessunit.infrastructure.traindeploy.DashboardLauncher dashboard, String cleanDatamartPath) {
        this.trainer = trainer;
        this.dashboard = dashboard;
        this.cleanDatamartPath = cleanDatamartPath;
    }

    public void run() {
        Path path = Paths.get(cleanDatamartPath);
        if (!Files.exists(path)) {
            logger.warn("No se encontró clean_datamart.db en {}. Se esperará al siguiente ciclo.", cleanDatamartPath);
            return;
        }

        try {
            Instant lastModified = Files.getLastModifiedTime(path).toInstant();

            if (lastModified.isAfter(lastRun)) {
                logger.info("Cambios detectados en clean_datamart.db (Last Modified: {}). Iniciando pipeline...", lastModified);
                lastRun = lastModified;

                logger.info("Paso 1: Entrenando modelo con script Python...");
                boolean trained = trainer.launchTraining();

                if (trained) {
                    logger.info("Modelo entrenado correctamente. Lanzando dashboard...");
                    dashboard.launchDashboard();
                } else {
                    logger.error("El modelo no pudo entrenarse. No se lanzará el dashboard.");
                }

            } else {
                logger.debug("⏸ clean_datamart.db no ha cambiado desde el último entrenamiento.");
            }
        } catch (IOException e) {
            logger.error("Error al acceder a clean_datamart.db", e);
        }
    }
}