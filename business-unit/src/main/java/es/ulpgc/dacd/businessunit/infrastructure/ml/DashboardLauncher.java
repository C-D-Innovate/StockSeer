package es.ulpgc.dacd.businessunit.infrastructure.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DashboardLauncher {
    private static final Logger logger = LoggerFactory.getLogger(DashboardLauncher.class);
    private final String pythonExecutable;
    private final String scriptPath;
    private final String csvPath;
    private final String modelPath;

    private Process currentDashboard = null;

    public DashboardLauncher(String pythonExecutable, String scriptPath, String csvPath, String modelPath) {

        if (pythonExecutable == null || pythonExecutable.isBlank()) {
            pythonExecutable = System.getenv("PYTHON_EXECUTABLE");
        }
        this.pythonExecutable = pythonExecutable;
        this.scriptPath = scriptPath;
        this.csvPath = csvPath;
        this.modelPath = modelPath;
    }



    public void launchDashboard() {
        if (currentDashboard != null && currentDashboard.isAlive()) {
            logger.info("Reiniciando dashboard. Terminando el proceso anterior (PID: {})", currentDashboard.pid());
            currentDashboard.destroy();
            try {
                currentDashboard.waitFor();
            } catch (InterruptedException e) {
                logger.warn("Interrupción al esperar que el dashboard anterior termine.", e);
            }
        }

        if (pythonExecutable == null || scriptPath == null || csvPath == null || modelPath == null) {
            logger.error("Uno o más argumentos del ProcessBuilder son null:");
            logger.error("   pythonExecutable: {}", pythonExecutable);
            logger.error("   scriptPath: {}", scriptPath);
            logger.error("   csvPath: {}", csvPath);
            logger.error("   modelPath: {}", modelPath);
            throw new IllegalStateException("No se puede lanzar el dashboard: argumentos null.");
        }

        logger.info("🚀 Lanzando dashboard: {}", scriptPath);
        logger.debug("🧪 Ejecutando comando: {} -m streamlit run {} -- {} {}",
                pythonExecutable, scriptPath, csvPath, modelPath);

        ProcessBuilder builder = new ProcessBuilder(
                pythonExecutable,
                "-m", "streamlit",
                "run", scriptPath,
                "--", csvPath, modelPath
        );
        builder.redirectErrorStream(true);

        try {
            currentDashboard = builder.start();
            logger.info("Dashboard iniciado correctamente (PID: {})", currentDashboard.pid());
        } catch (IOException e) {
            logger.error("Error al lanzar el dashboard", e);
        }
    }
}