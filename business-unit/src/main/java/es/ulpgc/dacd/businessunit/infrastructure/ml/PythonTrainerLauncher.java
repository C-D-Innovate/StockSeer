package es.ulpgc.dacd.businessunit.infrastructure.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class PythonTrainerLauncher {
    private static final Logger logger = LoggerFactory.getLogger(PythonTrainerLauncher.class);
    private final String pythonExecutable;
    private final String scriptPath;
    private final String dbPath;
    private final String csvPath;
    private final String modelPath;

    public PythonTrainerLauncher(String pythonExecutable, String scriptPath, String dbPath, String csvPath, String modelPath) {

        if (pythonExecutable == null || pythonExecutable.isBlank()) {
            pythonExecutable = System.getenv("PYTHON_EXECUTABLE");
        }
        this.pythonExecutable = pythonExecutable;
        this.scriptPath = scriptPath;
        this.dbPath = dbPath;
        this.csvPath = csvPath;
        this.modelPath = modelPath;
    }

    public boolean launchTraining() {
        logger.info("Ejecutando script de entrenamiento con:");
        logger.info("   Python: {}", pythonExecutable);
        logger.info("   Script: {}", scriptPath);
        logger.info("   DB Path: {}", dbPath);
        logger.info("   CSV Path: {}", csvPath);
        logger.info("   Modelo Path: {}", modelPath);

        if (pythonExecutable == null || scriptPath == null || dbPath == null || csvPath == null || modelPath == null) {
            logger.error("Uno o más argumentos son null.");
            throw new IllegalArgumentException("Parámetros nulos al intentar lanzar entrenamiento.");
        }

        if (!new File(scriptPath).exists()) {
            logger.error("El script de entrenamiento no existe: {}", scriptPath);
            return false;
        }

        ProcessBuilder builder = new ProcessBuilder(
                pythonExecutable,
                scriptPath,
                dbPath,
                csvPath,
                modelPath
        );
        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();

            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[PYTHON] {}", line);
                }
            }

            int exitCode = process.waitFor();
            logger.info("Código de salida del script: {}", exitCode);
            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            logger.error("⚠Error al ejecutar el script de entrenamiento", e);
            return false;
        }
    }

}